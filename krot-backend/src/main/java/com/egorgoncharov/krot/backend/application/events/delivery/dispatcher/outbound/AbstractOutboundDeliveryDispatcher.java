package com.egorgoncharov.krot.backend.application.events.delivery.dispatcher.outbound;

import com.egorgoncharov.krot.backend.Result;
import com.egorgoncharov.krot.backend.api.model.request.EntityReflection;
import com.egorgoncharov.krot.backend.api.model.response.ApiEvent;
import com.egorgoncharov.krot.backend.application.context.model.EventContext;
import com.egorgoncharov.krot.backend.application.events.delivery.dispatcher.AbstractDeliveryDispatcher;
import com.egorgoncharov.krot.backend.application.guard.view.ViewGuard;
import com.egorgoncharov.krot.backend.connection.ConnectionMetadata;
import com.egorgoncharov.krot.backend.connection.ConnectionRegistry;
import com.egorgoncharov.krot.backend.database.Identifiable;
import com.egorgoncharov.krot.backend.database.relational.entity.RoleEntity;
import com.egorgoncharov.krot.backend.database.relational.entity.UserEntity;

import java.util.BitSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

/* While AbstractDeliveryDispatcher should be used to stream application-wide events inside the application
 * itself, this class should be used to stream these events outside the application (e.g. by WebSockets),
 * since it contains a preprocessing pipeline to bucket events based on connected users roles and remove all
 * sensitive data from the entities update events, such as passwords and keys. */
public abstract class AbstractOutboundDeliveryDispatcher<T extends Identifiable<I>, I, A, C extends EntityReflection<T>> extends AbstractDeliveryDispatcher<T, I, A> implements OutboundDeliveryResolver<T, I, A> {
    protected abstract ConnectionRegistry connectionRegistry();

    protected abstract ViewGuard<T, I, A> viewGuard();

    protected abstract C entityReflectionConverter();

    private record Bucket<T extends Identifiable<I>, I, A>(ApiEvent<T, I, A> event, CopyOnWriteArrayList<UUID> receivers) {
    }

    @Override
    public void dispatch(EventContext<T, I, A> context) {
        Map<UUID, ConnectionMetadata> connectedUsers = connectionRegistry().getConnections();
        Map<BitSet, Bucket<T, I, A>> buckets = new ConcurrentHashMap<>();
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            connectedUsers.forEach((userId, metadata) -> executor.submit(() -> {
                UserEntity principal = UserEntity.builder()
                        .id(userId)
                        .role(RoleEntity.builder()
                                .id(metadata.getRoleId())
                                .grade(metadata.getRoleGrade())
                                .authorities(metadata.getAuthorities().stream().toList())
                                .build()
                        )
                        .build();
                Result<List<Boolean>> result = viewGuard().canView(EventContext.<T, I, A>builder()
                        .targets(context.targets())
                        .event(context.getEvent())
                        .principal(principal) // Overwriting principal explicitly
                        .additionalContext(context.additionalContext()).build()
                ).await().indefinitely(); // In this case it's OK to use indefinitely long await, since view guards generally never run any I/O
                BitSet mask;
                if (result.getCode() != 200 || result.getResult().isEmpty()) {
                    // TODO: Place a warning here, this should not happen
                    mask = new BitSet(context.targets().size());
                    mask.set(0, mask.size(), false);
                } else {
                    List<Boolean> viewPermissions = result.getResult().get();
                    mask = IntStream.range(0, viewPermissions.size()).filter(viewPermissions::get).collect(BitSet::new, BitSet::set, BitSet::or);
                }
                Bucket<T, I, A> existingBucket = buckets.get(mask);
                existingBucket = existingBucket == null ? new Bucket<T, I, A>(transformEventContext(EventContext.<T, I, A>builder()
                        .targets(IntStream
                                .range(0, context.targets().size())
                                .filter(mask::get)
                                .mapToObj(i -> context.targets().get(i))
                                .toList()
                        )
                        .event(context.getEvent())
                        .principal(context.principal())
                        .additionalContext(context.additionalContext())
                        .build()), new CopyOnWriteArrayList<>()
                ) : existingBucket;
                existingBucket.receivers.add(userId);
                buckets.put(mask, existingBucket);
            }));
        }
        buckets.forEach((bitset, bucket) -> send(bucket.event, bucket.receivers));
    }

    protected ApiEvent<T, I, A> transformEventContext(EventContext<T, I, A> context) {
        return new ApiEvent<>(
                context.getEvent(),
                context.additionalContext(),
                context.targets().stream().map(target -> entityReflectionConverter().from(target)).toList()
        );
    }
}
