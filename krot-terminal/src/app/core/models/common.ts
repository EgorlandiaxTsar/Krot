export interface BackendAddress {
    ip: string,
    port: number,
    secured: boolean,
}

export interface BatteryStatus {
    charge: number,
    isCharging: boolean,
}
