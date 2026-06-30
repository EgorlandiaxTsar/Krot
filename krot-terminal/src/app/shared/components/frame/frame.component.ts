import {Window} from "@/core/models/window";
import {Component, inject, input} from "@angular/core";
import {WindowService} from "@/core/services/window.service";
import {NgClass, NgComponentOutlet, NgStyle} from "@angular/common";
import {ZardButtonComponent} from "@/shared/components/button";
import {ZardTooltipDirective} from "@/shared/components/tooltip";

type ResizeDirection = 'ne' | 'nw' | 'se' | 'sw'

@Component({
    selector: 'app-frame',
    standalone: true,
    imports: [
        NgClass,
        NgStyle,
        ZardButtonComponent,
        ZardTooltipDirective,
        NgComponentOutlet
    ],
    templateUrl: './frame.component.html',
    styleUrl: './frame.component.css',
})
export class FrameComponent {
    private static readonly RESIZE_MULTIPLIERS: { [key: string]: { x: number, y: number } } = {
        'se': {x: 1, y: 1},
        'sw': {x: -1, y: 1},
        'ne': {x: 1, y: -1},
        'nw': {x: -1, y: -1},
    }

    windowInstance = input.required<Window>({alias: 'window'})
    private windowService = inject(WindowService)
    private isDragging = false
    private isResizing = false;
    private resizeDirection!: ResizeDirection;
    private startMouseX: number = 0
    private startMouseY: number = 0
    private startWindowX: number = 0
    private startWindowY: number = 0
    private startWidth = 0;
    private startHeight = 0;

    protected get viewState() {
        return this.windowInstance().viewState
    }

    protected get position() {
        return this.windowInstance().position
    }

    protected get size() {
        return this.windowInstance().size
    }

    protected get name() {
        return this.windowInstance().app.name
    }

    protected focus() {
        this.windowService.focus(this.windowInstance().app.id)
    }

    protected minimize() {
        this.windowService.hide(this.windowInstance().app.id)
    }

    protected maximize() {
        switch (this.windowInstance().viewState) {
            case 'fullscreen': {
                this.windowService.show(this.windowInstance().app.id)
                break
            }
            case 'visible': {
                this.windowService.maximize(this.windowInstance().app.id)
                break
            }
        }
    }

    protected close() {
        console.log('close called')
        this.windowService.close(this.windowInstance().app.id)
    }

    protected isFocused() {
        return this.windowService.isFocused(this.windowInstance().app.id)
    }

    protected drag(event: PointerEvent) {
        if (!this.isDragging) return
        event.preventDefault()
        const dx = event.clientX - this.startMouseX
        const dy = event.clientY - this.startMouseY
        this.windowService.move(
            this.windowInstance().app.id,
            this.startWindowX + dx,
            this.startWindowY + dy,
        )
    }

    protected dragStart(event: PointerEvent) {
        if (this.windowInstance().viewState !== 'visible') return
        event.preventDefault()
        this.focus()
        this.isDragging = true
        this.startMouseX = event.clientX
        this.startMouseY = event.clientY
        this.startWindowX = this.position.x
        this.startWindowY = this.position.y;
        (event.currentTarget as HTMLElement).setPointerCapture(event.pointerId)
    }

    protected dragEnd(event: PointerEvent) {
        if (this.windowInstance().viewState !== 'visible') return
        event.preventDefault()
        this.isDragging = false;
        (event.currentTarget as HTMLElement).releasePointerCapture(event.pointerId)
    }

    protected resize(event: PointerEvent) {
        if (!this.isResizing || !this.resizeDirection) return
        event.preventDefault()
        event.stopPropagation()
        const dx = event.clientX - this.startMouseX
        const dy = event.clientY - this.startMouseY
        const xMult = FrameComponent.RESIZE_MULTIPLIERS[this.resizeDirection].x
        const yMult = FrameComponent.RESIZE_MULTIPLIERS[this.resizeDirection].y
        const width = Math.max(this.windowInstance().app.minSize.width, this.startWidth + dx * xMult)
        const height = Math.max(this.windowInstance().app.minSize.height, this.startHeight + dy * yMult)
        this.windowService.resize(
            this.windowInstance().app.id,
            {
                width: width,
                height: height,
            }
        )
        const minWidthReached = width === this.windowInstance().app.minSize.width
        const minHeightReached = height === this.windowInstance().app.minSize.height
        this.windowService.move(
            this.windowInstance().app.id,
            minWidthReached ? this.windowInstance().position.x : this.startWindowX + dx * Math.max(0, -xMult),
            minHeightReached ? this.windowInstance().position.y : this.startWindowY + dy * Math.max(0, -yMult)
        )
    }

    protected resizeStart(event: PointerEvent, direction: ResizeDirection) {
        if (this.windowInstance().viewState !== 'visible') return
        event.preventDefault()
        event.stopPropagation()
        this.focus()
        this.isResizing = true
        this.resizeDirection = direction
        this.startMouseX = event.clientX
        this.startMouseY = event.clientY
        this.startWidth = this.size.width
        this.startHeight = this.size.height
        this.startWindowX = this.position.x
        this.startWindowY = this.position.y;
        (event.currentTarget as HTMLElement).setPointerCapture(event.pointerId)
    }

    protected resizeEnd(event: PointerEvent) {
        if (this.windowInstance().viewState !== 'visible') return
        event.preventDefault()
        event.stopPropagation()
        this.isResizing = false;
        (event.currentTarget as HTMLElement).releasePointerCapture(event.pointerId)
    }
}
