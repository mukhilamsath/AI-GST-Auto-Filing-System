import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ApiService, DashboardSummary, Invoice } from '../../services/api.service';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { BaseChartDirective } from 'ng2-charts';
import { ChartConfiguration } from 'chart.js';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, MatCardModule, MatIconModule, BaseChartDirective],
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css']
})
export class DashboardComponent implements OnInit {
  summary: DashboardSummary | null = null;
  recentInvoices: Invoice[] = [];

  public barChartData: ChartConfiguration<'bar'>['data'] = {
    labels: [],
    datasets: [
      { data: [], label: 'Invoice Totals (₹)' }
    ]
  };

  public barChartOptions: ChartConfiguration<'bar'>['options'] = {
    responsive: true,
    maintainAspectRatio: false,
    scales: {
      x: {
        title: {
          display: true,
          text: 'Invoice'
        }
      },
      y: {
        beginAtZero: true,
        title: {
          display: true,
          text: 'Total Amount (₹)'
        }
      }
    }
  };

  constructor(private apiService: ApiService) {}

  ngOnInit() {
    this.apiService.getDashboardSummary().subscribe(data => {
      this.summary = data;
    });

    this.apiService.getInvoices().subscribe(invoices => {
      const sortedInvoices = invoices.slice().sort((a, b) => {
        return new Date(b.invoiceDate).getTime() - new Date(a.invoiceDate).getTime();
      });
      this.recentInvoices = sortedInvoices.slice(0, 6);
      this.barChartData = {
        labels: this.recentInvoices.map(inv => inv.invoiceNumber || new Date(inv.invoiceDate).toLocaleDateString()),
        datasets: [
          {
            data: this.recentInvoices.map(inv => inv.totalAmount),
            label: 'Invoice Totals (₹)'
          }
        ]
      };
    });
  }
}
