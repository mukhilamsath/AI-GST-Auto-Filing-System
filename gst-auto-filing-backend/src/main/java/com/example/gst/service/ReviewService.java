package com.example.gst.service;

import com.example.gst.dto.ReviewSubmissionDto;
import com.example.gst.entity.AuditTrail;
import com.example.gst.entity.Invoice;
import com.example.gst.entity.User;
import com.example.gst.repository.AuditTrailRepository;
import com.example.gst.repository.InvoiceRepository;
import com.example.gst.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

@Service
public class ReviewService {

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private AuditTrailRepository auditTrailRepository;

    @Autowired
    private UserRepository userRepository;

    public List<Invoice> getPendingReviews() {
        return invoiceRepository.findByReviewStatus("PENDING_REVIEW");
    }

    public List<Invoice> getAssignedReviews(String email) {
        User user = userRepository.findByEmail(email).orElseThrow();
        // Custom query to get by reviewerId, for now just filter
        return invoiceRepository.findAll().stream()
                .filter(i -> "IN_REVIEW".equals(i.getReviewStatus()) && user.getId().equals(i.getReviewerId()))
                .toList();
    }

    @Transactional
    public Invoice assignReview(Long invoiceId, String reviewerEmail) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new RuntimeException("Invoice not found"));
        
        User reviewer = userRepository.findByEmail(reviewerEmail)
                .orElseThrow(() -> new RuntimeException("Reviewer not found"));
        
        if (!"PENDING_REVIEW".equals(invoice.getReviewStatus())) {
            throw new RuntimeException("Invoice is not pending review");
        }
        
        invoice.setReviewStatus("IN_REVIEW");
        invoice.setReviewerId(reviewer.getId());
        
        return invoiceRepository.save(invoice);
    }

    @Transactional
    public Invoice submitReview(Long invoiceId, ReviewSubmissionDto submissionDto, String reviewerEmail) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new RuntimeException("Invoice not found"));
        
        User reviewer = userRepository.findByEmail(reviewerEmail)
                .orElseThrow(() -> new RuntimeException("Reviewer not found"));
                
        if (!reviewer.getId().equals(invoice.getReviewerId())) {
            throw new RuntimeException("You are not assigned to review this invoice");
        }
        
        for (Map.Entry<String, String> entry : submissionDto.getCorrectedFields().entrySet()) {
            String fieldName = entry.getKey();
            String newValue = entry.getValue();
            
            try {
                Field field = Invoice.class.getDeclaredField(fieldName);
                field.setAccessible(true);
                Object oldValue = field.get(invoice);
                
                String oldStr = oldValue != null ? oldValue.toString() : null;
                
                // Set new value handling types (simplified)
                if (field.getType().equals(Double.class)) {
                    field.set(invoice, Double.valueOf(newValue));
                } else if (field.getType().equals(String.class)) {
                    field.set(invoice, newValue);
                }
                
                // Create Audit Trail
                AuditTrail audit = new AuditTrail();
                audit.setInvoiceId(invoice.getId());
                audit.setReviewerId(reviewer.getId());
                audit.setFieldName(fieldName);
                audit.setOldValue(oldStr);
                audit.setNewValue(newValue);
                auditTrailRepository.save(audit);
                
            } catch (Exception e) {
                throw new RuntimeException("Error updating field: " + fieldName, e);
            }
        }
        
        invoice.setReviewStatus("REVIEWED");
        // Also could update validation status if we want
        
        return invoiceRepository.save(invoice);
    }

    public List<AuditTrail> getAuditTrail(Long invoiceId) {
        return auditTrailRepository.findByInvoiceIdOrderByTimestampDesc(invoiceId);
    }
}
