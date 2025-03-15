import { Routes } from '@angular/router';

const routes: Routes = [
  {
    path: 'authority',
    data: { pageTitle: 'caseApp.adminAuthority.home.title' },
    loadChildren: () => import('./admin/authority/authority.routes'),
  },
  {
    path: 'aluno',
    data: { pageTitle: 'caseApp.aluno.home.title' },
    loadChildren: () => import('./aluno/aluno.routes'),
  },
  {
    path: 'meta',
    data: { pageTitle: 'caseApp.meta.home.title' },
    loadChildren: () => import('./meta/meta.routes'),
  },
  {
    path: 'simulado',
    data: { pageTitle: 'caseApp.simulado.home.title' },
    loadChildren: () => import('./simulado/simulado.routes'),
  },
  /* jhipster-needle-add-entity-route - JHipster will add entity modules routes here */
];

export default routes;
