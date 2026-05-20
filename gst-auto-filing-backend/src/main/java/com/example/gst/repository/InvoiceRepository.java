package com.example.gst.repository;

import com.example.gst.entity.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    boolean existsByInvoiceNumber(String invoiceNumber);
}
