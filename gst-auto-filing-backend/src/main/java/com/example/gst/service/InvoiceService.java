package com.example.gst.service;

import com.example.gst.dto.InvoiceDto;
import com.example.gst.entity.Invoice;
import com.example.gst.entity.ValidationError;
import com.example.gst.repository.InvoiceRepository;
import com.example.gst.repository.ValidationErrorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class InvoiceService {

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private ValidationErrorRepository errorRepository;

    @Autowired
    private OcrService ocrService;

    @Autowired
    private ValidationEngine validationEngine;

    public InvoiceDto processUploadedInvoice(MultipartFile file) {
        // 1. Extract Data via real Tesseract OCR
        Invoice invoice = ocrService.extractInvoiceData(file);
        
        // 2. Validate Data
        List<ValidationError> errors = validationEngine.validateInvoice(invoice);
        
        // 3. Save Invoice
        invoice = invoiceRepository.save(invoice);
        
        // 4. Save Errors
        for (ValidationError error : errors) {
            error.setInvoiceId(invoice.getId());
        }
        errorRepository.saveAll(errors);
        
        return mapToDto(invoice, errors);
    }

    public List<InvoiceDto> processUploadedInvoices(List<MultipartFile> files) {
        return files.stream()
                .map(this::processUploadedInvoice)
                .collect(Collectors.toList());
    }

    public List<InvoiceDto> getAllInvoices() {
        return invoiceRepository.findAll().stream().map(inv -> {
            List<ValidationError> errors = errorRepository.findByInvoiceId(inv.getId());
            return mapToDto(inv, errors);
        }).collect(Collectors.toList());
    }

    private InvoiceDto mapToDto(Invoice invoice, List<ValidationError> errors) {
        InvoiceDto dto = new InvoiceDto();
        dto.setId(invoice.getId());
        dto.setInvoiceNumber(invoice.getInvoiceNumber());
        dto.setVendorName(invoice.getVendorName());
        dto.setGstin(invoice.getGstin());
        dto.setInvoiceDate(invoice.getInvoiceDate());
        dto.setTaxableAmount(invoice.getTaxableAmount());
        dto.setCgst(invoice.getCgst());
        dto.setSgst(invoice.getSgst());
        dto.setIgst(invoice.getIgst());
        dto.setTotalAmount(invoice.getTotalAmount());
        dto.setValidationStatus(invoice.getValidationStatus());
        dto.setErrors(errors);
        return dto;
    }
}
