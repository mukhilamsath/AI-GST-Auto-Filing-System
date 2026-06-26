package com.example.gst.service;

import com.example.gst.entity.Invoice;
import com.example.gst.entity.ValidationError;
import com.example.gst.repository.InvoiceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ValidationEngine {

    @Autowired
    private InvoiceRepository invoiceRepository;

    public List<ValidationError> validate(Invoice invoice) {
        return validateInvoice(invoice);
    }

    public List<ValidationError> validateInvoice(Invoice invoice) {
        List<ValidationError> errors = new ArrayList<>();
        boolean hasError = false;
        boolean hasWarning = false;

        // 1. Check duplicate invoice number
        boolean isDuplicate = invoice.getId() != null 
                ? invoiceRepository.existsByInvoiceNumberAndIdNot(invoice.getInvoiceNumber(), invoice.getId())
                : invoiceRepository.existsByInvoiceNumber(invoice.getInvoiceNumber());
        
        if (isDuplicate) {
            errors.add(new ValidationError(null, "Duplicate Invoice Number: " + invoice.getInvoiceNumber(), "ERROR"));
            hasError = true;
        }

        // 2. Validate GST Calculation
        double calculatedTotal = invoice.getTaxableAmount() + invoice.getCgst() + invoice.getSgst() + invoice.getIgst();
        if (Math.abs(calculatedTotal - invoice.getTotalAmount()) > 1.0) { // allowing small float differences
            errors.add(new ValidationError(null, "GST Calculation Mismatch: Taxable + CGST + SGST + IGST doesn't match Total", "ERROR"));
            hasError = true;
        }

        // 3. Validate GSTIN format (basic mock check)
        if (invoice.getGstin() == null || invoice.getGstin().length() != 15) {
            errors.add(new ValidationError(null, "Invalid GSTIN format", "WARNING"));
            hasWarning = true;
        }

        if (hasError) {
            invoice.setValidationStatus("ERROR");
        } else if (hasWarning) {
            invoice.setValidationStatus("WARNING");
        } else {
            invoice.setValidationStatus("VALID");
        }

        return errors;
    }
}
