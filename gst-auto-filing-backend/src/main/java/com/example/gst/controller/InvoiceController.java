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
    public ResponseEntity<List<InvoiceDto>> uploadInvoices(@RequestParam("files") List<MultipartFile> files) {
        return ResponseEntity.ok(invoiceService.processUploadedInvoices(files));
    }

    @GetMapping
    public ResponseEntity<List<InvoiceDto>> getAllInvoices() {
        return ResponseEntity.ok(invoiceService.getAllInvoices());
    }
}
