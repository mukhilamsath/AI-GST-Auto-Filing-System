import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { ApiService, Invoice } from '../../services/api.service';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';

@Component({
  selector: 'app-upload-invoice',
  standalone: true,
  imports: [CommonModule, MatCardModule, MatIconModule, MatButtonModule, MatProgressBarModule, MatSnackBarModule],
  templateUrl: './upload-invoice.component.html',
  styleUrls: ['./upload-invoice.component.css']
})
export class UploadInvoiceComponent {
  isDragOver = false;
  selectedFiles: File[] = [];
  isUploading = false;

  constructor(
    private apiService: ApiService,
    private router: Router,
    private snackBar: MatSnackBar
  ) {}

  onDragOver(event: DragEvent) {
    event.preventDefault();
    this.isDragOver = true;
  }

  onDragLeave(event: DragEvent) {
    event.preventDefault();
    this.isDragOver = false;
  }

  onDrop(event: DragEvent) {
    event.preventDefault();
    this.isDragOver = false;
    
    if (event.dataTransfer?.files && event.dataTransfer.files.length > 0) {
      this.selectedFiles = [...this.selectedFiles, ...(Array.from(event.dataTransfer.files) as File[])];
    }
  }

  onFileSelected(event: any) {
    if (event.target.files && event.target.files.length > 0) {
      this.selectedFiles = [...this.selectedFiles, ...(Array.from(event.target.files) as File[])];
    }
  }

  clearFile(event: Event, index: number) {
    event.stopPropagation();
    this.selectedFiles.splice(index, 1);
  }

  uploadFiles() {
    if (this.selectedFiles.length === 0) return;
    
    this.isUploading = true;
    
    // Simulate some OCR processing delay
    setTimeout(() => {
      this.apiService.uploadInvoices(this.selectedFiles).subscribe({
        next: (invoices) => {
          this.isUploading = false;
          this.snackBar.open(`${invoices.length} invoices processed successfully!`, 'Close', { duration: 3000 });
          this.router.navigate(['/invoices']);
        },
        error: (err) => {
          this.isUploading = false;
          this.snackBar.open('Error processing invoices', 'Close', { duration: 3000 });
        }
      });
    }, 1500);
  }
}
