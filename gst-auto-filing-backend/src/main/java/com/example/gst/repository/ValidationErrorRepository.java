package com.example.gst.repository;

import com.example.gst.entity.ValidationError;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ValidationErrorRepository extends JpaRepository<ValidationError, Long> {
    List<ValidationError> findByInvoiceId(Long invoiceId);
}
