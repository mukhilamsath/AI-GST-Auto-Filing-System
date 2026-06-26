package com.example.gst.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyTaxLiabilityDto {
    private String month; // format: YYYY-MM
    private Double cgst;
    private Double sgst;
    private Double igst;

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
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
}
