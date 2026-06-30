import {AfterViewInit, Component, inject, signal} from '@angular/core';
import {ZardButtonComponent} from "@/shared/components/button";
import {NgIcon, provideIcons} from "@ng-icons/core";
import {lucideCircleUserRound, lucideCog, lucideLayoutGrid, lucideRadio} from "@ng-icons/lucide";
import {BatteryComponent} from "@/shared/components/battery/battery.component";
import {Channel, invoke} from "@tauri-apps/api/core";
import {BackendAddress, BatteryStatus} from "@/core/models/common";
import {WindowService} from "@/core/services/window.service";
import {ZardTooltipDirective} from "@/shared/components/tooltip";
import {FrameComponent} from "@/shared/components/frame/frame.component";

@Component({
    selector: 'app-terminal',
    imports: [
        ZardButtonComponent,
        NgIcon,
        BatteryComponent,
        ZardTooltipDirective,
        FrameComponent,
    ],
    viewProviders: [provideIcons({lucideCircleUserRound, lucideRadio, lucideLayoutGrid, lucideCog})],
    templateUrl: './terminal.component.html',
    styleUrl: './terminal.component.css',
})
export class TerminalComponent implements AfterViewInit {
    protected usernameServerPair = signal('')
    protected batteryStatus = signal<BatteryStatus>({
        charge: 100,
        isCharging: true,
    })
    protected time = signal<string>('21:48:15')
    protected windowService = inject(WindowService)

    async ngAfterViewInit(): Promise<void> {
        await this.loadUsernameServerPair()
        await this.initBatteryChannel()
        await this.initTimeChannel()
    }

    private async loadUsernameServerPair() {
        try {
            const server = await invoke<BackendAddress>('get_server_address')
            const username = await invoke<string>('get_current_user')
            this.usernameServerPair.set(`${username}@${server.ip}:${server.port}`)
        } catch (error: any) {
            console.error(`Failed to load username and/or server: ${error}`)
        }
    }

    private async initBatteryChannel() {
        const channel = new Channel<BatteryStatus>()
        channel.onmessage = (_: BatteryStatus) => this.batteryStatus.set(_)
        try {
            await invoke('stream_battery_status', {channel})
        } catch (error: any) {
            console.error(`Failed to initialize OS battery status stream: ${error}`)
        }
    }

    private async initTimeChannel() {
        const channel = new Channel<number>()
        channel.onmessage = (_: number) => this.time.set(new Date(_ * 1000).toISOString().substring(11, 19))
        try {
            await invoke('stream_time', {channel})
        } catch (error: any) {
            console.error(`Failed to initialize OS time stream: ${error}`)
        }
    }
}
