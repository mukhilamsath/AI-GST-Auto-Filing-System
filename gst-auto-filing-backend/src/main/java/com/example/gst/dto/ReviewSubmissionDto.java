package com.example.gst.dto;

import lombok.Data;
import java.util.Map;

@Data
public class ReviewSubmissionDto {
    private Map<String, String> correctedFields;
    // Map key: fieldName, value: new value (e.g., {"vendorName": "Correct Vendor LLC"})

	public Map<String, String> getCorrectedFields() {
		return correctedFields;
	}

	public void setCorrectedFields(Map<String, String> correctedFields) {
		this.correctedFields = correctedFields;
	}
}
