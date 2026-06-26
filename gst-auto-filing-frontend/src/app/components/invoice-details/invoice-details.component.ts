import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ApiService, Invoice } from '../../services/api.service';
import { MatCardModule } from '@angular/material/card';
import { MatTableModule, MatTableDataSource } from '@angular/material/table';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatIconModule } from '@angular/material/icon';
import { Subject, timer } from 'rxjs';
import { takeUntil, switchMap } from 'rxjs/operators';

@Component({
  selector: 'app-invoice-details',
  standalone: true,
  imports: [CommonModule, MatCardModule, MatTableModule, MatFormFieldModule, MatInputModule, MatIconModule],
  templateUrl: './invoice-details.component.html',
  styleUrls: ['./invoice-details.component.css']
})
export class InvoiceDetailsComponent implements OnInit, OnDestroy {
  displayedColumns: string[] = ['invoiceNumber', 'vendorName', 'invoiceDate', 'totalAmount', 'status'];
  dataSource = new MatTableDataSource<Invoice>([]);
  private destroy$ = new Subject<void>();

  constructor(private apiService: ApiService) {}

  ngOnInit() {
    // Poll invoices every 3 seconds to dynamically update background statuses (PROCESSING -> PROCESSED/FAILED)
    timer(0, 3000)
      .pipe(
        switchMap(() => this.apiService.getInvoices()),
        takeUntil(this.destroy$)
      )
      .subscribe({
        next: (data) => {
          this.dataSource.data = data;
        },
        error: (err) => {
          console.error('Failed to poll invoices', err);
        }
      });
  }

  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
  }

  applyFilter(event: Event) {
    const filterValue = (event.target as HTMLInputElement).value;
    this.dataSource.filter = filterValue.trim().toLowerCase();
  }

  getStatusClass(status: string): string {
    switch(status) {
      case 'VALID': return 'status-valid';
      case 'WARNING': return 'status-warning';
      case 'ERROR': return 'status-error';
      case 'PROCESSING': return 'status-processing';
      case 'PROCESSED': return 'status-processed';
      case 'FAILED': return 'status-failed';
      default: return '';
    }
  }
}
