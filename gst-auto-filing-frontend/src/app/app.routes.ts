import { Routes } from '@angular/router';
import { LoginComponent } from './components/login/login.component';
import { RegisterComponent } from './components/register/register.component';
import { LayoutComponent } from './components/layout/layout.component';
import { DashboardComponent } from './components/dashboard/dashboard.component';
import { UploadInvoiceComponent } from './components/upload-invoice/upload-invoice.component';
import { InvoiceDetailsComponent } from './components/invoice-details/invoice-details.component';
import { ValidationErrorsComponent } from './components/validation-errors/validation-errors.component';
import { ReviewQueueComponent } from './components/review-queue/review-queue.component';
import { ReviewTaskComponent } from './components/review-task/review-task.component';
import { UsersComponent } from './components/users/users.component';
import { DashboardAnalyticsComponent } from './components/dashboard-analytics/dashboard-analytics.component';
import { authGuard } from './guards/auth.guard';

export const routes: Routes = [
  { path: 'login', component: LoginComponent },
  { path: 'register', component: RegisterComponent },
  {
    path: '',
    component: LayoutComponent,
    canActivate: [authGuard],
    children: [
      { path: 'dashboard', component: DashboardComponent },
      { path: 'analytics', component: DashboardAnalyticsComponent },
      { 
        path: 'upload', 
        component: UploadInvoiceComponent,
        data: { roles: ['USER', 'ADMIN'] }
      },
      { 
        path: 'invoices', 
        component: InvoiceDetailsComponent,
        data: { roles: ['USER', 'REVIEWER', 'ADMIN'] }
      },
      { 
        path: 'errors', 
        component: ValidationErrorsComponent,
        data: { roles: ['USER', 'ADMIN'] }
      },
      { 
        path: 'review-queue', 
        component: ReviewQueueComponent,
        data: { roles: ['REVIEWER', 'ADMIN'] }
      },
      { 
        path: 'review-task/:id', 
        component: ReviewTaskComponent,
        data: { roles: ['REVIEWER', 'ADMIN'] }
      },
      { 
        path: 'users', 
        component: UsersComponent,
        data: { roles: ['ADMIN'] }
      },
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' }
    ]
  },
  { path: '**', redirectTo: '' }
];
