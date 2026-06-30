import {Account} from "@/features/account/account";
import {Devices} from "@/features/devices/devices";
import {Applications} from "@/features/applications/applications";
import {Admin} from "@/features/admin/admin";
import {Size} from "@/core/models/window";
import {Type} from "@angular/core";

export interface Application {
    id: string,
    name: string,
    icon: string,
    component: Type<any>,
    defaultSize: Size,
    minSize: Size,
}

export class AccountApplication implements Application {
    id = 'krot.account';
    name = 'Account';
    icon = 'lucideCircleUserRound';
    component = Account;
    defaultSize = {width: 500, height: 800};
    minSize = {width: 300, height: 250};
}

export class DevicesApplication implements Application {
    id = 'krot.devices';
    name = 'Devices';
    icon = 'lucideRadio';
    component = Devices;
    defaultSize = {width: 1080, height: 720};
    minSize = {width: 300, height: 250};
}

export class AppsApplication implements Application {
    id = 'krot.apps';
    name = 'Applications';
    icon = 'lucideLayoutGrid';
    component = Applications;
    defaultSize = {width: 1080, height: 720};
    minSize = {width: 300, height: 250};
}

export class AdminApplication implements Application {
    id = 'krot.admin';
    name = 'Admin';
    icon = 'lucideCog';
    component = Admin;
    defaultSize = {width: 1080, height: 720};
    minSize = {width: 300, height: 250};
}
