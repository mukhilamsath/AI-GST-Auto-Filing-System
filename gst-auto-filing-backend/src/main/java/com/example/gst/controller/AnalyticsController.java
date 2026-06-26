package com.example.gst.controller;

import com.example.gst.dto.AnalyticsSummaryDto;
import com.example.gst.service.AnalyticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    @Autowired
    private AnalyticsService analyticsService;

    @GetMapping("/summary")
    @PreAuthorize("hasAnyRole('USER', 'REVIEWER', 'ADMIN')")
    public ResponseEntity<AnalyticsSummaryDto> getSummary() {
        return ResponseEntity.ok(analyticsService.getSummary());
    }
}
