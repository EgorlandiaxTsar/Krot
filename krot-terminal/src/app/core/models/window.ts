import {Application} from "@/core/models/application";

export type WindowViewState = 'fullscreen' | 'hidden' | 'visible'

export interface Window {
    app: Application,
    size: Size,
    position: Position,
    viewState: WindowViewState
    previousViewState: WindowViewState
}

export interface Size {
    width: number,
    height: number,
}

export interface Position {
    x: number,
    y: number,
    z: number,
}
