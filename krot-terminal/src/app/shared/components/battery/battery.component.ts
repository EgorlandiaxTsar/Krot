import {Component, computed, input} from '@angular/core';
import {NgClass} from "@angular/common";
import {NgIcon, provideIcons} from "@ng-icons/core";
import {lucideZap} from "@ng-icons/lucide";

@Component({
    selector: 'app-battery',
    standalone: true,
    imports: [
        NgClass,
        NgIcon
    ],
    viewProviders: [provideIcons({lucideZap})],
    templateUrl: './battery.component.html',
    styleUrl: './battery.component.css',
})
export class BatteryComponent {
    charge = input(100);
    charging = input(false);
    size = input<'sm' | 'md' | 'lg'>('md')

    protected validCharge = computed(() => Math.max(0, Math.min(100, this.charge() || 0)));
    protected colorClass = computed(() => {
        if (this.validCharge() <= 10) return 'bg-destructive animate-pulse';
        if (this.validCharge() <= 30) return 'bg-amber-500';
        return 'bg-white';
    });
    protected sizeClass = computed(() => {
        switch (this.size()) {
            case "lg":
                return "w-16 h-8"
            case "md":
                return "w-12 h-6"
            case "sm":
                return "w-10 h-6"
        }
    })
}
