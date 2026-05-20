import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ApiService, Invoice } from '../../services/api.service';
import { MatCardModule } from '@angular/material/card';
import { MatTableModule, MatTableDataSource } from '@angular/material/table';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatIconModule } from '@angular/material/icon';

@Component({
  selector: 'app-invoice-details',
  standalone: true,
  imports: [CommonModule, MatCardModule, MatTableModule, MatFormFieldModule, MatInputModule, MatIconModule],
  templateUrl: './invoice-details.component.html',
  styleUrls: ['./invoice-details.component.css']
})
export class InvoiceDetailsComponent implements OnInit {
  displayedColumns: string[] = ['invoiceNumber', 'vendorName', 'invoiceDate', 'totalAmount', 'status'];
  dataSource = new MatTableDataSource<Invoice>([]);

  constructor(private apiService: ApiService) {}

  ngOnInit() {
    this.apiService.getInvoices().subscribe(data => {
      this.dataSource.data = data;
    });
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
      default: return '';
    }
  }
}
