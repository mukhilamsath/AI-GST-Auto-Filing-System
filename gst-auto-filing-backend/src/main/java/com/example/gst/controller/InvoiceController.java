package com.example.gst.controller;

import com.example.gst.dto.InvoiceDto;
import com.example.gst.service.InvoiceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/invoices")
public class InvoiceController {

    @Autowired
    private InvoiceService invoiceService;

    @PostMapping("/upload")
    public ResponseEntity<InvoiceDto> uploadInvoice(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(invoiceService.processUploadedInvoice(file));
    }

    @GetMapping
    public ResponseEntity<List<InvoiceDto>> getAllInvoices() {
        return ResponseEntity.ok(invoiceService.getAllInvoices());
    }
}
