import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface ValidationError {
  id: number;
  errorMessage: string;
  severity: string;
}

export interface Invoice {
  id: number;
  invoiceNumber: string;
  vendorName: string;
  gstin: string;
  invoiceDate: string;
  taxableAmount: number;
  cgst: number;
  sgst: number;
  igst: number;
  totalAmount: number;
  validationStatus: string;
  errors: ValidationError[];
}

export interface DashboardSummary {
  totalInvoices: number;
  totalGstCollected: number;
  validationErrorsCount: number;
  validInvoicesCount: number;
}

export interface MonthlyTaxLiability {
  month: string;
  cgst: number;
  sgst: number;
  igst: number;
}

export interface ValidationErrorDistribution {
  errorType: string;
  count: number;
}

export interface AnalyticsSummary {
  taxLiabilities: MonthlyTaxLiability[];
  errorDistribution: ValidationErrorDistribution[];
}

@Injectable({
  providedIn: 'root'
})
export class ApiService {
  constructor(private http: HttpClient) {}

  uploadInvoices(files: File[]): Observable<Invoice[]> {
    const formData = new FormData();
    files.forEach(file => {
      formData.append('files', file);
    });
    return this.http.post<Invoice[]>(`${environment.apiUrl}/invoices/upload`, formData);
  }

  getInvoices(): Observable<Invoice[]> {
    return this.http.get<Invoice[]>(`${environment.apiUrl}/invoices`);
  }

  getDashboardSummary(): Observable<DashboardSummary> {
    return this.http.get<DashboardSummary>(`${environment.apiUrl}/dashboard/summary`);
  }

  getAnalyticsSummary(): Observable<AnalyticsSummary> {
    return this.http.get<AnalyticsSummary>(`${environment.apiUrl}/analytics/summary`);
  }

  updateAndRevalidateInvoice(id: number, payload: Partial<Invoice>): Observable<{ isValid: boolean; invoice: Invoice }> {
    return this.http.put<{ isValid: boolean; invoice: Invoice }>(`${environment.apiUrl}/invoices/${id}/revalidate`, payload);
  }

  getUsers(): Observable<any[]> {
    return this.http.get<any[]>(`${environment.apiUrl}/users`);
  }

  createUser(user: any): Observable<any> {
    return this.http.post<any>(`${environment.apiUrl}/users`, user);
  }

  updateUserRole(id: number, role: string): Observable<any> {
    return this.http.put<any>(`${environment.apiUrl}/users/${id}/role`, { role });
  }

  deleteUser(id: number): Observable<any> {
    return this.http.delete<any>(`${environment.apiUrl}/users/${id}`);
  }
}
