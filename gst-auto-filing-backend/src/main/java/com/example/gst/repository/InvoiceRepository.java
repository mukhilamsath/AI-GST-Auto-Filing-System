package com.example.gst.repository;

import com.example.gst.entity.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    boolean existsByInvoiceNumber(String invoiceNumber);
    boolean existsByInvoiceNumberAndIdNot(String invoiceNumber, Long id);
    List<Invoice> findByReviewStatus(String reviewStatus);
}
