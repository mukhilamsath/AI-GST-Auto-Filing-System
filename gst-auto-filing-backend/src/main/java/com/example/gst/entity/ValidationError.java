package com.example.gst.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "validation_errors")
@Data
public class ValidationError {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long invoiceId;
    
    private String errorMessage;
    private String severity; // WARNING, ERROR
    
    public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getInvoiceId() {
		return invoiceId;
	}

	public void setInvoiceId(Long invoiceId) {
		this.invoiceId = invoiceId;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	public String getSeverity() {
		return severity;
	}

	public void setSeverity(String severity) {
		this.severity = severity;
	}

	public ValidationError() {}
    
    public ValidationError(Long invoiceId, String errorMessage, String severity) {
        this.invoiceId = invoiceId;
        this.errorMessage = errorMessage;
        this.severity = severity;
    }
}
