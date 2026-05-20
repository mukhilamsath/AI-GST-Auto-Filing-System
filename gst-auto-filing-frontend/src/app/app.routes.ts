import { Routes } from '@angular/router';
import { LoginComponent } from './components/login/login.component';
import { LayoutComponent } from './components/layout/layout.component';
import { DashboardComponent } from './components/dashboard/dashboard.component';
import { UploadInvoiceComponent } from './components/upload-invoice/upload-invoice.component';
import { InvoiceDetailsComponent } from './components/invoice-details/invoice-details.component';
import { ValidationErrorsComponent } from './components/validation-errors/validation-errors.component';
import { authGuard } from './guards/auth.guard';

export const routes: Routes = [
  { path: 'login', component: LoginComponent },
  {
    path: '',
    component: LayoutComponent,
    canActivate: [authGuard],
    children: [
      { path: 'dashboard', component: DashboardComponent },
      { path: 'upload', component: UploadInvoiceComponent },
      { path: 'invoices', component: InvoiceDetailsComponent },
      { path: 'errors', component: ValidationErrorsComponent },
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' }
    ]
  },
  { path: '**', redirectTo: '' }
];
