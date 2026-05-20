import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ApiService, Invoice } from '../../services/api.service';
import { MatCardModule } from '@angular/material/card';
import { MatTableModule, MatTableDataSource } from '@angular/material/table';

export interface FlatValidationError {
  invoiceNumber: string;
  errorMessage: string;
  severity: string;
}

@Component({
  selector: 'app-validation-errors',
  standalone: true,
  imports: [CommonModule, MatCardModule, MatTableModule],
  templateUrl: './validation-errors.component.html',
  styleUrls: ['./validation-errors.component.css']
})
export class ValidationErrorsComponent implements OnInit {
  displayedColumns: string[] = ['invoiceNumber', 'errorMessage', 'severity'];
  dataSource = new MatTableDataSource<FlatValidationError>([]);

  constructor(private apiService: ApiService) {}

  ngOnInit() {
    this.apiService.getInvoices().subscribe(invoices => {
      const flatErrors: FlatValidationError[] = [];
      invoices.forEach(inv => {
        if (inv.errors && inv.errors.length > 0) {
          inv.errors.forEach(err => {
            flatErrors.push({
              invoiceNumber: inv.invoiceNumber,
              errorMessage: err.errorMessage,
              severity: err.severity
            });
          });
        }
      });
      this.dataSource.data = flatErrors;
    });
  }

  getSeverityClass(severity: string): string {
    switch(severity) {
      case 'WARNING': return 'severity-warning';
      case 'ERROR': return 'severity-error';
      default: return '';
    }
  }
}
