import {Injectable, signal} from "@angular/core";
import {
    AccountApplication,
    AdminApplication,
    Application,
    AppsApplication,
    DevicesApplication
} from "@/core/models/application";
import {Size, Window} from "@/core/models/window";

@Injectable({providedIn: 'root'})
export class WindowService {
    /* TODO:
    *   - Provide a high-level API to access external applications list
    * */

    private static readonly BASE_Z: number = 10
    private static readonly MIN_VISIBLE_X_PCT: number = 0.15
    private static readonly MIN_VISIBLE_Y_PCT: number = 0.3

    readonly systemApplications: Application[] = [
        new AccountApplication(),
        new DevicesApplication(),
        new AppsApplication(),
        new AdminApplication(),
    ]

    windows = signal<Window[]>([])
    private externalApplications = signal<Application[]>([]) // TODO: For future
    private currentZ: number = WindowService.BASE_Z

    launch(id: string) {
        const app = this.systemApplications.find(app => app.id === id) || this.externalApplications().find(app => app.id === id)
        if (!app) return
        const existing = this.windows().find(window => window.app.id === id)
        if (existing) {
            if (existing.viewState === 'hidden') {
                if (existing.previousViewState !== 'fullscreen') this.show(id)
                else this.maximize(id)
            }
            this.focus(id)
            return
        }
        this.currentZ++
        this.windows.update(windows => [...windows, {
            app: app,
            size: app.defaultSize,
            position: {
                x: (innerWidth - app.defaultSize.width) / 2 + (25 * (this.currentZ % 11)),
                y: (innerHeight - app.defaultSize.height) / 2 + (25 * (this.currentZ % 11)),
                z: this.currentZ
            },
            viewState: 'visible',
            previousViewState: 'visible',
        }])
    }

    close(id: string) {
        this.windows.update(windows => windows.filter(window => window.app.id !== id))
    }

    show(id: string) {
        this.windows.update(windows => windows.map(window => window.app.id === id ? {
            ...window,
            viewState: 'visible',
            previousViewState: window.viewState
        } : window))
    }

    hide(id: string) {
        this.windows.update(windows => windows.map(window => window.app.id === id ? {
            ...window,
            viewState: 'hidden',
            previousViewState: window.viewState
        } : window))
    }

    maximize(id: string) {
        this.windows.update(windows => windows.map(window => window.app.id === id ? {
            ...window,
            viewState: 'fullscreen',
            previousViewState: window.viewState
        } : window))
    }

    focus(id: string) {
        const target = this.windows().find(window => window.app.id === id)
        if (!target || target.position.z === this.currentZ) return
        this.currentZ++
        this.windows.update(windows => windows.map(window => window.app.id == id ? {
            ...window,
            position: {...window.position, z: this.currentZ}
        } : window))
    }

    move(id: string, x: number, y: number) {
        this.windows.update(windows => windows.map(window => {
            const minX = -(window.size.width * (1 - WindowService.MIN_VISIBLE_X_PCT))
            const maxX = innerWidth - window.size.width * WindowService.MIN_VISIBLE_X_PCT
            const maxY = innerHeight - window.size.height * WindowService.MIN_VISIBLE_Y_PCT
            const clampedX = Math.min(maxX, Math.max(minX, x))
            const clampedY = Math.min(maxY, Math.max(0, y))
            return window.app.id === id ? {
                ...window,
                position: {...window.position, x: clampedX, y: clampedY}
            } : window
        }))
    }

    resize(id: string, size: Size) {
        this.windows.update(windows => windows.map(window => window.app.id === id ? {
            ...window,
            size: {
                width: Math.max(window.app.minSize.width, size.width),
                height: Math.max(window.app.minSize.height, size.height),
            }
        } : window))
    }

    isOpened(id: string) {
        return this.windows().find(window => window.app.id === id) !== undefined
    }

    isFocused(id: string) {
        return this.windows().find(window => window.app.id === id && window.position.z === this.currentZ) !== undefined
    }

    async loadExternalApplications() {
        // TODO: For future
    }
}
