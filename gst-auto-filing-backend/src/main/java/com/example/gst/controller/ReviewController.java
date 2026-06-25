package com.example.gst.controller;

import com.example.gst.dto.ReviewSubmissionDto;
import com.example.gst.entity.AuditTrail;
import com.example.gst.entity.Invoice;
import com.example.gst.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    @GetMapping("/queue")
    @PreAuthorize("hasAnyRole('REVIEWER', 'ADMIN')")
    public ResponseEntity<List<Invoice>> getPendingReviews() {
        return ResponseEntity.ok(reviewService.getPendingReviews());
    }

    @GetMapping("/assigned")
    @PreAuthorize("hasAnyRole('REVIEWER', 'ADMIN')")
    public ResponseEntity<List<Invoice>> getAssignedReviews(Authentication authentication) {
        return ResponseEntity.ok(reviewService.getAssignedReviews(authentication.getName()));
    }

    @PostMapping("/assign/{invoiceId}")
    @PreAuthorize("hasAnyRole('REVIEWER', 'ADMIN')")
    public ResponseEntity<Invoice> assignReview(@PathVariable Long invoiceId, Authentication authentication) {
        return ResponseEntity.ok(reviewService.assignReview(invoiceId, authentication.getName()));
    }

    @PostMapping("/submit/{invoiceId}")
    @PreAuthorize("hasAnyRole('REVIEWER', 'ADMIN')")
    public ResponseEntity<Invoice> submitReview(@PathVariable Long invoiceId, 
                                                @RequestBody ReviewSubmissionDto submissionDto,
                                                Authentication authentication) {
        return ResponseEntity.ok(reviewService.submitReview(invoiceId, submissionDto, authentication.getName()));
    }

    @GetMapping("/audit/{invoiceId}")
    @PreAuthorize("hasAnyRole('USER', 'REVIEWER', 'ADMIN')")
    public ResponseEntity<List<AuditTrail>> getAuditTrail(@PathVariable Long invoiceId) {
        return ResponseEntity.ok(reviewService.getAuditTrail(invoiceId));
    }
}
