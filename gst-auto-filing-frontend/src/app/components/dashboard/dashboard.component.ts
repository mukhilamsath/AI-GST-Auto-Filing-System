import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ApiService, DashboardSummary } from '../../services/api.service';
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

  public barChartData: ChartConfiguration<'bar'>['data'] = {
    labels: ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun'],
    datasets: [
      { data: [6500, 5900, 8000, 8100, 5600, 5500], label: 'GST Collected (₹)' }
    ]
  };

  public barChartOptions: ChartConfiguration<'bar'>['options'] = {
    responsive: true,
  };

  constructor(private apiService: ApiService) {}

  ngOnInit() {
    this.apiService.getDashboardSummary().subscribe(data => {
      this.summary = data;
    });
  }
}
