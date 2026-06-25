import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { ReviewService } from '../../services/review.service';
import { ApiService } from '../../services/api.service';

@Component({
  selector: 'app-review-task',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './review-task.component.html',
  styleUrls: ['./review-task.component.css']
})
export class ReviewTaskComponent implements OnInit {
  invoiceId!: number;
  invoice: any = null;
  loading = true;
  submitting = false;

  correctedFields: { [key: string]: string } = {};

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private reviewService: ReviewService,
    private apiService: ApiService
  ) {}

  ngOnInit(): void {
    this.route.params.subscribe(params => {
      if (params['id']) {
        this.invoiceId = +params['id'];
        this.loadInvoice();
      }
    });
  }

  loadInvoice(): void {
    this.loading = true;
    this.apiService.getInvoices().subscribe({
      next: (data: any[]) => {
        this.invoice = data.find(i => i.id === this.invoiceId);
        if (this.invoice) {
          // Initialize corrected fields
          this.correctedFields = {
            invoiceNumber: this.invoice.invoiceNumber,
            vendorName: this.invoice.vendorName,
            gstin: this.invoice.gstin,
            taxableAmount: this.invoice.taxableAmount,
            totalAmount: this.invoice.totalAmount
          };
        }
        this.loading = false;
      },
      error: () => this.loading = false
    });
  }

  submitReview(): void {
    this.submitting = true;
    
    // Find only changed fields
    const changes: { [key: string]: string } = {};
    for (const key in this.correctedFields) {
      if (this.correctedFields[key] != this.invoice[key]) {
        changes[key] = this.correctedFields[key];
      }
    }

    this.reviewService.submitReview(this.invoiceId, changes).subscribe({
      next: () => {
        this.submitting = false;
        this.router.navigate(['/review-queue']);
      },
      error: (err) => {
        console.error('Failed to submit review', err);
        this.submitting = false;
      }
    });
  }

  cancel(): void {
    this.router.navigate(['/review-queue']);
  }
}
