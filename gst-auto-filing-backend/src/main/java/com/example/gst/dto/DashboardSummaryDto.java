package com.example.gst.dto;

import lombok.Data;

@Data
public class DashboardSummaryDto {
    private long totalInvoices;
    private double totalGstCollected;
    private long validationErrorsCount;
    private long validInvoicesCount;
	public long getTotalInvoices() {
		return totalInvoices;
	}
	public void setTotalInvoices(long totalInvoices) {
		this.totalInvoices = totalInvoices;
	}
	public double getTotalGstCollected() {
		return totalGstCollected;
	}
	public void setTotalGstCollected(double totalGstCollected) {
		this.totalGstCollected = totalGstCollected;
	}
	public long getValidationErrorsCount() {
		return validationErrorsCount;
	}
	public void setValidationErrorsCount(long validationErrorsCount) {
		this.validationErrorsCount = validationErrorsCount;
	}
	public long getValidInvoicesCount() {
		return validInvoicesCount;
	}
	public void setValidInvoicesCount(long validInvoicesCount) {
		this.validInvoicesCount = validInvoicesCount;
	}
}
