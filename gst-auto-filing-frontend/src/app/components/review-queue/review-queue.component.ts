import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { ReviewService } from '../../services/review.service';

@Component({
  selector: 'app-review-queue',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './review-queue.component.html',
  styleUrls: ['./review-queue.component.css']
})
export class ReviewQueueComponent implements OnInit {
  pendingInvoices: any[] = [];
  assignedInvoices: any[] = [];
  loading = false;

  constructor(private reviewService: ReviewService, private router: Router) {}

  ngOnInit(): void {
    this.loadQueues();
  }

  loadQueues(): void {
    this.loading = true;
    this.reviewService.getPendingReviews().subscribe({
      next: (data) => {
        this.pendingInvoices = data;
        this.reviewService.getAssignedReviews().subscribe({
          next: (assignedData) => {
            this.assignedInvoices = assignedData;
            this.loading = false;
          },
          error: () => this.loading = false
        });
      },
      error: () => this.loading = false
    });
  }

  assignToMe(invoiceId: number): void {
    this.reviewService.assignReview(invoiceId).subscribe({
      next: () => {
        this.loadQueues();
      },
      error: (err) => console.error('Failed to assign', err)
    });
  }

  openReviewTask(invoiceId: number): void {
    this.router.navigate(['/review-task', invoiceId]);
  }
}
