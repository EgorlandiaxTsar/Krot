import { Routes } from "@angular/router";

export const routes: Routes = [
    {
        path: 'auth',
        loadComponent: () => import("@/features/auth/auth").then(m => m.Auth)
    },
    {
        path: 'terminal',
        loadComponent: () => import("@/features/terminal/terminal").then(m => m.Terminal)
    },
    {
        path: '',
        redirectTo: 'auth',
        pathMatch: 'full'
    }
];
