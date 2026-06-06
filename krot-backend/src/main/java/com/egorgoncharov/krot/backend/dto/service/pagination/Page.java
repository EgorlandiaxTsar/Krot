package com.egorgoncharov.krot.backend.dto.service.pagination;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
public class Page<T> {
    @Setter
    @JsonIgnore
    private List<T> items;
    @JsonProperty("limit")
    private int limit;
    @JsonProperty("items")
    private long itemsCount;
    @JsonProperty("pages")
    private int pagesCount;
    @JsonProperty("page")
    private int page;
    @JsonProperty("from")
    private long fromIndex;
    @JsonProperty("to")
    private long toIndex;
    @JsonProperty("start")
    private boolean isStart;
    @JsonProperty("end")
    private boolean isEnd;

    public Page(List<T> items, long itemsCount, int limit, int page) {
        this.items = items;
        this.itemsCount = itemsCount;
        this.limit = limit;
        this.page = page;
        this.compute(itemsCount, limit, page);
    }

    public void setPage(int page) {
        if (page < 0 || page >= pagesCount) throw new IndexOutOfBoundsException("Page index is out of bounds");
        this.page = page;
        compute(itemsCount, limit, page);
    }

    public void setItemsCount(long itemsCount) {
        this.itemsCount = itemsCount;
        compute(itemsCount, limit, page);
    }

    public void setLimit(int limit) {
        this.limit = limit;
        compute(itemsCount, limit, page);
    }

    private void compute(long total, int limit, int page) {
        if (limit <= 0) throw new IllegalArgumentException("Limit cannot be zero");
        this.pagesCount = Math.max(1, (int) Math.ceil((double) total / limit));
        this.fromIndex = (long) page * limit;
        this.toIndex = Math.min(fromIndex + limit, total);
        this.isStart = page == 0;
        this.isEnd = page == pagesCount - 1;
    }
}
