import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface AuditTrail {
  id: number;
  invoiceId: number;
  reviewerId: number;
  fieldName: string;
  oldValue: string;
  newValue: string;
  timestamp: string;
}

@Injectable({
  providedIn: 'root'
})
export class ReviewService {
  private apiUrl = `${environment.apiUrl}/reviews`;

  constructor(private http: HttpClient) {}

  getPendingReviews(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/queue`);
  }

  getAssignedReviews(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/assigned`);
  }

  assignReview(invoiceId: number): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/assign/${invoiceId}`, {});
  }

  submitReview(invoiceId: number, correctedFields: { [key: string]: string }): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/submit/${invoiceId}`, { correctedFields });
  }

  getAuditTrail(invoiceId: number): Observable<AuditTrail[]> {
    return this.http.get<AuditTrail[]>(`${this.apiUrl}/audit/${invoiceId}`);
  }
}
