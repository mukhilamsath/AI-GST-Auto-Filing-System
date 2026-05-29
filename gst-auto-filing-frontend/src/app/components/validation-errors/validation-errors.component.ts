import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ApiService, Invoice } from '../../services/api.service';
import { finalize } from 'rxjs/operators';

export interface EditableInvoice extends Invoice {
  isEditing?: boolean;
  isSaving?: boolean;
}

@Component({
  selector: 'app-validation-errors',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './validation-errors.component.html',
  styleUrls: ['./validation-errors.component.css']
})
export class ValidationErrorsComponent implements OnInit {
  invoices: EditableInvoice[] = [];
  backupInvoices: Record<number, Invoice> = {};
  isLoading = true;

  constructor(private apiService: ApiService) {}

  ngOnInit(): void {
    this.fetchErrorInvoices();
  }

  fetchErrorInvoices(): void {
    this.isLoading = true;
    this.apiService.getInvoices().pipe(
      finalize(() => this.isLoading = false)
    ).subscribe({
      next: (invoices) => {
        // Filter out only invoices with errors or non-VALID status
        this.invoices = invoices
          .filter(inv => inv.validationStatus !== 'VALID' || (inv.errors && inv.errors.length > 0))
          .map(inv => ({ ...inv, isEditing: false, isSaving: false }));
      },
      error: (err) => {
        console.error('Failed to fetch invoices', err);
      }
    });
  }

  startEdit(invoice: EditableInvoice): void {
    if (invoice.isEditing) return;
    
    // Create a deep clone for backup
    this.backupInvoices[invoice.id] = JSON.parse(JSON.stringify(invoice));
    invoice.isEditing = true;
  }

  cancelEdit(invoice: EditableInvoice): void {
    if (!invoice.isEditing) return;

    // Revert to backup
    const backup = this.backupInvoices[invoice.id];
    if (backup) {
      Object.assign(invoice, backup);
      delete this.backupInvoices[invoice.id];
    }
    invoice.isEditing = false;
  }

  saveEdit(invoice: EditableInvoice): void {
    if (!invoice.isEditing || invoice.isSaving) return;

    invoice.isSaving = true;

    // Prepare payload for update
    const payload: Partial<Invoice> = {
      invoiceNumber: invoice.invoiceNumber,
      vendorName: invoice.vendorName,
      gstin: invoice.gstin,
      invoiceDate: invoice.invoiceDate,
      taxableAmount: invoice.taxableAmount,
      cgst: invoice.cgst,
      sgst: invoice.sgst,
      igst: invoice.igst,
      totalAmount: invoice.totalAmount
    };

    this.apiService.updateAndRevalidateInvoice(invoice.id, payload).subscribe({
      next: (response) => {
        invoice.isSaving = false;
        if (response.isValid) {
          // Remove from grid dynamically if validation is successful
          this.invoices = this.invoices.filter(inv => inv.id !== invoice.id);
        } else {
          // Update errors and exit edit mode to review, or stay in edit mode
          Object.assign(invoice, response.invoice);
          invoice.isEditing = true; // Stay in edit mode to fix remaining errors
          
          // Re-clone backup with new state in case they cancel next
          this.backupInvoices[invoice.id] = JSON.parse(JSON.stringify(response.invoice));
        }
      },
      error: (err) => {
        console.error('Error saving invoice', err);
        invoice.isSaving = false;
        // Optionally show an error toast here
      }
    });
  }

  handleKeyDown(event: KeyboardEvent, invoice: EditableInvoice): void {
    if (event.key === 'Enter') {
      // Prevent default to avoid form submission if wrapped in a form
      event.preventDefault();
      this.saveEdit(invoice);
    } else if (event.key === 'Escape') {
      this.cancelEdit(invoice);
    }
  }

  getInvoiceErrors(invoice: EditableInvoice): string {
    if (!invoice.errors || invoice.errors.length === 0) return 'No errors listed.';
    return invoice.errors.map(e => e.errorMessage).join(' | ');
  }
}
