import {Routes} from "@angular/router";

export const routes: Routes = [
    {
        path: 'auth',
        loadComponent: () => import("@/features/auth/auth.component").then(m => m.AuthComponent)
    },
    {
        path: 'terminal',
        loadComponent: () => import("@/features/terminal/terminal.component").then(m => m.TerminalComponent)
    },
    {
        path: '',
        redirectTo: 'auth',
        pathMatch: 'full'
    }
];
