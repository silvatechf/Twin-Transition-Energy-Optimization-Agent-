import { ApplicationConfig, provideZoneChangeDetection } from '@angular/core';
import { provideRouter } from '@angular/router';
import { provideHttpClient } from '@angular/common/http';

// CORREÇÃO: Criar um arquivo app.routes.ts vazio se não houver rotas.
// Assumindo que você criou o arquivo app.routes.ts, ele está sendo importado.
import { routes } from './app.routes'; 
 
export const appConfig: ApplicationConfig = {
  providers: [
    provideZoneChangeDetection({ eventCoalescing: true }),
    provideRouter(routes),
    provideHttpClient() 
  ]
};