package com.example.gst.service;

import com.example.gst.dto.InvoiceDto;
import com.example.gst.entity.Invoice;
import com.example.gst.entity.ValidationError;
import com.example.gst.repository.InvoiceRepository;
import com.example.gst.repository.ValidationErrorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.annotation.Transactional;

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

    @Transactional
    public List<Invoice> createPendingInvoices(List<MultipartFile> files) {
        return files.stream().map(file -> {
            Invoice invoice = new Invoice();
            invoice.setValidationStatus("PROCESSING");
            invoice.setInvoiceNumber("PENDING-" + java.util.UUID.randomUUID().toString().substring(0, 8).toUpperCase());
            invoice.setVendorName("Processing...");
            invoice.setReviewStatus("PENDING_REVIEW");
            return invoiceRepository.save(invoice);
        }).collect(Collectors.toList());
    }

    @Transactional
    public void saveProcessedResults(Long invoiceId, Invoice tempInvoice) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new RuntimeException("Invoice not found: " + invoiceId));

        invoice.setInvoiceNumber(tempInvoice.getInvoiceNumber());
        invoice.setVendorName(tempInvoice.getVendorName());
        invoice.setGstin(tempInvoice.getGstin());
        invoice.setInvoiceDate(tempInvoice.getInvoiceDate());
        invoice.setTaxableAmount(tempInvoice.getTaxableAmount());
        invoice.setCgst(tempInvoice.getCgst());
        invoice.setSgst(tempInvoice.getSgst());
        invoice.setIgst(tempInvoice.getIgst());
        invoice.setTotalAmount(tempInvoice.getTotalAmount());
        invoice.setConfidenceScore(tempInvoice.getConfidenceScore());
        invoice.setReviewStatus(tempInvoice.getReviewStatus());

        // Run validation
        List<ValidationError> errors = validationEngine.validate(invoice);
        for (ValidationError error : errors) {
            error.setInvoiceId(invoiceId);
        }
        errorRepository.saveAll(errors);

        invoice.setValidationStatus("PROCESSED");
        invoiceRepository.save(invoice);
    }

    @Transactional
    public void saveFailedResults(Long invoiceId, String errorMessage) {
        Invoice invoice = invoiceRepository.findById(invoiceId).orElse(null);
        if (invoice != null) {
            ValidationError error = new ValidationError(invoiceId, errorMessage, "ERROR");
            errorRepository.save(error);

            invoice.setValidationStatus("FAILED");
            invoiceRepository.save(invoice);
        }
    }

    @Async("invoiceTaskExecutor")
    public void processInvoiceAsync(Long invoiceId, byte[] fileBytes, String originalFilename) {
        try {
            MultipartFile file = new com.example.gst.util.InMemoryMultipartFile(
                    "files", originalFilename, "application/octet-stream", fileBytes
            );

            // 1. Run OCR (extractText)
            String rawText = ocrService.extractText(file);

            // 2. Parse OCR text to get details
            Invoice tempInvoice = ocrService.parseInvoice(rawText, originalFilename);

            // 3. Save details, run validation, set status to PROCESSED
            saveProcessedResults(invoiceId, tempInvoice);

        } catch (Exception e) {
            // Save ValidationError and set status to FAILED
            saveFailedResults(invoiceId, "Async processing failed: " + e.getMessage());
        }
    }

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

    @org.springframework.transaction.annotation.Transactional
    public java.util.Map<String, Object> updateAndRevalidate(Long id, InvoiceDto payload) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Invoice not found"));

        invoice.setInvoiceNumber(payload.getInvoiceNumber());
        invoice.setVendorName(payload.getVendorName());
        invoice.setGstin(payload.getGstin());
        invoice.setInvoiceDate(payload.getInvoiceDate());
        invoice.setTaxableAmount(payload.getTaxableAmount());
        invoice.setCgst(payload.getCgst());
        invoice.setSgst(payload.getSgst());
        invoice.setIgst(payload.getIgst());
        invoice.setTotalAmount(payload.getTotalAmount());

        // Clear old errors
        List<ValidationError> oldErrors = errorRepository.findByInvoiceId(id);
        errorRepository.deleteAll(oldErrors);

        // Run validation
        List<ValidationError> errors = validationEngine.validateInvoice(invoice);

        // Save invoice and errors
        invoice = invoiceRepository.save(invoice);
        for (ValidationError error : errors) {
            error.setInvoiceId(invoice.getId());
        }
        errorRepository.saveAll(errors);

        InvoiceDto updatedDto = mapToDto(invoice, errors);
        boolean isValid = errors.isEmpty();

        return java.util.Map.of(
            "isValid", isValid,
            "invoice", updatedDto
        );
    }

    public InvoiceDto mapToDto(Invoice invoice, List<ValidationError> errors) {
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
