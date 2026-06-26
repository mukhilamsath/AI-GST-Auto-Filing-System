package com.example.gst.service;

import com.example.gst.dto.AnalyticsSummaryDto;
import com.example.gst.dto.MonthlyTaxLiabilityDto;
import com.example.gst.dto.ValidationErrorDistributionDto;
import com.example.gst.entity.Invoice;
import com.example.gst.entity.ValidationError;
import com.example.gst.repository.InvoiceRepository;
import com.example.gst.repository.ValidationErrorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class AnalyticsService {

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private ValidationErrorRepository validationErrorRepository;

    public AnalyticsSummaryDto getSummary() {
        List<Invoice> invoices = invoiceRepository.findAll();
        
        // 1. Group Monthly Tax Liabilities
        Map<String, MonthlyTaxLiabilityDto> taxByMonth = new TreeMap<>(); // sorted month keys
        for (Invoice inv : invoices) {
            if (inv.getInvoiceDate() == null) continue;
            
            // Format YYYY-MM
            String monthKey = inv.getInvoiceDate().getYear() + "-" + String.format("%02d", inv.getInvoiceDate().getMonthValue());
            
            MonthlyTaxLiabilityDto dto = taxByMonth.computeIfAbsent(monthKey, k -> new MonthlyTaxLiabilityDto(k, 0.0, 0.0, 0.0));
            
            double cgst = inv.getCgst() != null ? inv.getCgst() : 0.0;
            double sgst = inv.getSgst() != null ? inv.getSgst() : 0.0;
            double igst = inv.getIgst() != null ? inv.getIgst() : 0.0;
            
            dto.setCgst(Math.round((dto.getCgst() + cgst) * 100.0) / 100.0);
            dto.setSgst(Math.round((dto.getSgst() + sgst) * 100.0) / 100.0);
            dto.setIgst(Math.round((dto.getIgst() + igst) * 100.0) / 100.0);
        }

        // 2. Group & Categorize Validation Errors
        List<ValidationError> errors = validationErrorRepository.findAll();
        Map<String, Long> errorCounts = errors.stream()
                .collect(Collectors.groupingBy(
                        err -> categorizeError(err.getErrorMessage()),
                        Collectors.counting()
                ));

        List<ValidationErrorDistributionDto> errorDistribution = errorCounts.entrySet().stream()
                .map(entry -> new ValidationErrorDistributionDto(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());

        // Make sure standard error categories are initialized even if count is 0, so the frontend UI charts load them nicely
        String[] standardTypes = {"DUPLICATE_INVOICE", "CALCULATION_MISMATCH", "MISSING_GSTIN", "OTHER"};
        for (String type : standardTypes) {
            if (errorDistribution.stream().noneMatch(d -> d.getErrorType().equals(type))) {
                errorDistribution.add(new ValidationErrorDistributionDto(type, 0L));
            }
        }

        return new AnalyticsSummaryDto(
                new ArrayList<>(taxByMonth.values()),
                errorDistribution
        );
    }

    private String categorizeError(String errorMessage) {
        if (errorMessage == null) {
            return "OTHER";
        }
        if (errorMessage.contains("Duplicate Invoice Number")) {
            return "DUPLICATE_INVOICE";
        } else if (errorMessage.contains("GST Calculation Mismatch")) {
            return "CALCULATION_MISMATCH";
        } else if (errorMessage.contains("GSTIN") || errorMessage.contains("gstin")) {
            return "MISSING_GSTIN";
        }
        return "OTHER";
    }
}
