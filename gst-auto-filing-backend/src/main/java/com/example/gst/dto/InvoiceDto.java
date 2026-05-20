package com.example.gst.dto;

import lombok.Data;
import java.time.LocalDate;
import java.util.List;
import com.example.gst.entity.ValidationError;

@Data
public class InvoiceDto {
    private Long id;
    private String invoiceNumber;
    private String vendorName;
    private String gstin;
    private LocalDate invoiceDate;
    private Double taxableAmount;
    private Double cgst;
    private Double sgst;
    private Double igst;
    public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getInvoiceNumber() {
		return invoiceNumber;
	}
	public void setInvoiceNumber(String invoiceNumber) {
		this.invoiceNumber = invoiceNumber;
	}
	public String getVendorName() {
		return vendorName;
	}
	public void setVendorName(String vendorName) {
		this.vendorName = vendorName;
	}
	public String getGstin() {
		return gstin;
	}
	public void setGstin(String gstin) {
		this.gstin = gstin;
	}
	public LocalDate getInvoiceDate() {
		return invoiceDate;
	}
	public void setInvoiceDate(LocalDate invoiceDate) {
		this.invoiceDate = invoiceDate;
	}
	public Double getTaxableAmount() {
		return taxableAmount;
	}
	public void setTaxableAmount(Double taxableAmount) {
		this.taxableAmount = taxableAmount;
	}
	public Double getCgst() {
		return cgst;
	}
	public void setCgst(Double cgst) {
		this.cgst = cgst;
	}
	public Double getSgst() {
		return sgst;
	}
	public void setSgst(Double sgst) {
		this.sgst = sgst;
	}
	public Double getIgst() {
		return igst;
	}
	public void setIgst(Double igst) {
		this.igst = igst;
	}
	public Double getTotalAmount() {
		return totalAmount;
	}
	public void setTotalAmount(Double totalAmount) {
		this.totalAmount = totalAmount;
	}
	public String getValidationStatus() {
		return validationStatus;
	}
	public void setValidationStatus(String validationStatus) {
		this.validationStatus = validationStatus;
	}
	public List<ValidationError> getErrors() {
		return errors;
	}
	public void setErrors(List<ValidationError> errors) {
		this.errors = errors;
	}
	private Double totalAmount;
    private String validationStatus;
    private List<ValidationError> errors;
}
