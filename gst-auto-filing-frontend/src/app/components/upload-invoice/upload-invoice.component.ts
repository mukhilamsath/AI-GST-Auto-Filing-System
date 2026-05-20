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
  selectedFile: File | null = null;
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
      this.selectedFile = event.dataTransfer.files[0];
    }
  }

  onFileSelected(event: any) {
    if (event.target.files && event.target.files.length > 0) {
      this.selectedFile = event.target.files[0];
    }
  }

  clearFile(event: Event) {
    event.stopPropagation();
    this.selectedFile = null;
  }

  uploadFile() {
    if (!this.selectedFile) return;
    
    this.isUploading = true;
    
    // Simulate some OCR processing delay
    setTimeout(() => {
      this.apiService.uploadInvoice(this.selectedFile!).subscribe({
        next: (invoice) => {
          this.isUploading = false;
          this.snackBar.open('Invoice processed successfully!', 'Close', { duration: 3000 });
          this.router.navigate(['/invoices']);
        },
        error: (err) => {
          this.isUploading = false;
          this.snackBar.open('Error processing invoice', 'Close', { duration: 3000 });
        }
      });
    }, 1500);
  }
}
