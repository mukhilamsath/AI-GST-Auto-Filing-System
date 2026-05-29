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
                .mapToDouble(inv -> inv.getCgst() + inv.getSgst() + inv.getIgst())
                .sum();
                
        long validInvoices = invoices.stream()
                .filter(inv -> "VALID".equals(inv.getValidationStatus()))
                .count();
                
        long errorCount = invoices.stream()
                .filter(inv -> !"VALID".equals(inv.getValidationStatus()))
                .count();

        DashboardSummaryDto summary = new DashboardSummaryDto();
        summary.setTotalInvoices(totalInvoices);
        summary.setTotalGstCollected(Math.round(totalGstCollected * 100.0) / 100.0);
        summary.setValidInvoicesCount(validInvoices);
        summary.setValidationErrorsCount(errorCount);
        
        return summary;
    }
}
