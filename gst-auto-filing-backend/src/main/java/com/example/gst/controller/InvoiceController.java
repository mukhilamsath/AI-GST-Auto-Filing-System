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
        return ResponseEntity.ok(invoiceService.processUploadedInvoices(files));
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
