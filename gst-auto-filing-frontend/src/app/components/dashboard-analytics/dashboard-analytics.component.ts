import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ApiService, AnalyticsSummary } from '../../services/api.service';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { BaseChartDirective } from 'ng2-charts';
import { ChartConfiguration } from 'chart.js';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';

@Component({
  selector: 'app-dashboard-analytics',
  standalone: true,
  imports: [CommonModule, MatCardModule, MatIconModule, BaseChartDirective],
  templateUrl: './dashboard-analytics.component.html',
  styleUrls: ['./dashboard-analytics.component.css']
})
export class DashboardAnalyticsComponent implements OnInit, OnDestroy {
  private destroy$ = new Subject<void>();
  public isLoading = true;
  public summary: AnalyticsSummary | null = null;

  // Chart configuration for Monthly Taxes (Stacked Bar Chart)
  public barChartData: ChartConfiguration<'bar'>['data'] = {
    labels: [],
    datasets: []
  };

  public barChartOptions: ChartConfiguration<'bar'>['options'] = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: {
        position: 'top',
      },
      tooltip: {
        mode: 'index',
        intersect: false,
      }
    },
    scales: {
      x: {
        stacked: true,
        title: {
          display: true,
          text: 'Month'
        }
      },
      y: {
        stacked: true,
        beginAtZero: true,
        title: {
          display: true,
          text: 'Amount (₹)'
        }
      }
    }
  };

  // Chart configuration for Validation Error Types (Doughnut Chart)
  public doughnutChartData: ChartConfiguration<'doughnut'>['data'] = {
    labels: [],
    datasets: []
  };

  public doughnutChartOptions: ChartConfiguration<'doughnut'>['options'] = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: {
        position: 'right',
      }
    }
  };

  constructor(private apiService: ApiService) {}

  ngOnInit(): void {
    this.apiService.getAnalyticsSummary()
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (data) => {
          this.summary = data;
          this.isLoading = false;
          this.buildCharts(data);
        },
        error: (err) => {
          console.error('Failed to load analytics summary', err);
          this.isLoading = false;
        }
      });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  private buildCharts(data: AnalyticsSummary): void {
    // 1. Stacked Bar Chart (Monthly Tax Breakdowns)
    const months = data.taxLiabilities.map(t => t.month);
    const cgstData = data.taxLiabilities.map(t => t.cgst);
    const sgstData = data.taxLiabilities.map(t => t.sgst);
    const igstData = data.taxLiabilities.map(t => t.igst);

    this.barChartData = {
      labels: months,
      datasets: [
        { data: cgstData, label: 'CGST', backgroundColor: '#42a5f5', hoverBackgroundColor: '#1e88e5' },
        { data: sgstData, label: 'SGST', backgroundColor: '#66bb6a', hoverBackgroundColor: '#43a047' },
        { data: igstData, label: 'IGST', backgroundColor: '#ffca28', hoverBackgroundColor: '#ffb300' }
      ]
    };

    // 2. Doughnut Chart (Validation Errors distribution)
    const errLabels = data.errorDistribution.map(d => this.formatErrorLabel(d.errorType));
    const errCounts = data.errorDistribution.map(d => d.count);

    this.doughnutChartData = {
      labels: errLabels,
      datasets: [
        {
          data: errCounts,
          backgroundColor: ['#ef5350', '#ab47bc', '#78909c', '#d4e157'],
          hoverBackgroundColor: ['#e53935', '#8e24aa', '#546e7a', '#c0ca33']
        }
      ]
    };
  }

  private formatErrorLabel(type: string): string {
    switch (type) {
      case 'DUPLICATE_INVOICE': return 'Duplicate Invoice';
      case 'CALCULATION_MISMATCH': return 'Calculation Mismatch';
      case 'MISSING_GSTIN': return 'Missing/Invalid GSTIN';
      default: return 'Other Errors';
    }
  }
}
