package com.example.gst.controller;

import com.example.gst.dto.InvoiceDto;
import com.example.gst.service.InvoiceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/invoices")
public class InvoiceController {

    @Autowired
    private InvoiceService invoiceService;

    @PostMapping("/upload")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<List<InvoiceDto>> uploadInvoices(@RequestParam("files") List<MultipartFile> files) {
        List<com.example.gst.entity.Invoice> pendingInvoices = invoiceService.createPendingInvoices(files);
        
        for (int i = 0; i < files.size(); i++) {
            MultipartFile file = files.get(i);
            com.example.gst.entity.Invoice invoice = pendingInvoices.get(i);
            try {
                byte[] bytes = file.getBytes();
                invoiceService.processInvoiceAsync(invoice.getId(), bytes, file.getOriginalFilename());
            } catch (java.io.IOException e) {
                invoiceService.saveFailedResults(invoice.getId(), "Failed to read upload file bytes: " + e.getMessage());
            }
        }
        
        List<InvoiceDto> dtos = pendingInvoices.stream()
                .map(inv -> invoiceService.mapToDto(inv, java.util.List.of()))
                .collect(java.util.stream.Collectors.toList());
                
        return ResponseEntity.accepted().body(dtos);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'REVIEWER', 'ADMIN')")
    public ResponseEntity<List<InvoiceDto>> getAllInvoices() {
        return ResponseEntity.ok(invoiceService.getAllInvoices());
    }

    @PutMapping("/{id}/revalidate")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<?> revalidateInvoice(@PathVariable Long id, @RequestBody InvoiceDto payload) {
        try {
            return ResponseEntity.ok(invoiceService.updateAndRevalidate(id, payload));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(java.util.Map.of("message", e.getMessage()));
        }
    }
}
