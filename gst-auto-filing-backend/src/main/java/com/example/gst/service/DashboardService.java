package com.example.gst.service;

import com.example.gst.dto.DashboardSummaryDto;
import com.example.gst.entity.Invoice;
import com.example.gst.repository.InvoiceRepository;
import com.example.gst.repository.ValidationErrorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DashboardService {

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private ValidationErrorRepository validationErrorRepository;

    public DashboardSummaryDto getSummary() {
        List<Invoice> invoices = invoiceRepository.findAll();
        
        long totalInvoices = invoices.size();
        double totalGstCollected = invoices.stream()
                .mapToDouble(inv -> {
                    double cgst = inv.getCgst() != null ? inv.getCgst() : 0.0;
                    double sgst = inv.getSgst() != null ? inv.getSgst() : 0.0;
                    double igst = inv.getIgst() != null ? inv.getIgst() : 0.0;
                    return cgst + sgst + igst;
                })
                .sum();
                
        long validInvoices = invoices.stream()
                .filter(inv -> {
                    if ("VALID".equals(inv.getValidationStatus())) {
                        return true;
                    }
                    if ("PROCESSED".equals(inv.getValidationStatus())) {
                        return validationErrorRepository.findByInvoiceId(inv.getId()).isEmpty();
                    }
                    return false;
                })
                .count();
                
        long errorCount = invoices.stream()
                .filter(inv -> {
                    if ("FAILED".equals(inv.getValidationStatus())) {
                        return true;
                    }
                    if ("PROCESSED".equals(inv.getValidationStatus())) {
                        return !validationErrorRepository.findByInvoiceId(inv.getId()).isEmpty();
                    }
                    return !"VALID".equals(inv.getValidationStatus()) && !"PROCESSING".equals(inv.getValidationStatus());
                })
                .count();

        DashboardSummaryDto summary = new DashboardSummaryDto();
        summary.setTotalInvoices(totalInvoices);
        summary.setTotalGstCollected(Math.round(totalGstCollected * 100.0) / 100.0);
        summary.setValidInvoicesCount(validInvoices);
        summary.setValidationErrorsCount(errorCount);
        
        return summary;
    }
}
