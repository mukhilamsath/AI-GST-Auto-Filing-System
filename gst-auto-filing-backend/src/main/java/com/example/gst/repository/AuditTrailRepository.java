package com.example.gst.repository;

import com.example.gst.entity.AuditTrail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditTrailRepository extends JpaRepository<AuditTrail, Long> {
    List<AuditTrail> findByInvoiceIdOrderByTimestampDesc(Long invoiceId);
}
