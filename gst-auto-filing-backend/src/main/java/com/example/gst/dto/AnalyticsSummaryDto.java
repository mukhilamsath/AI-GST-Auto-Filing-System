package com.example.gst.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnalyticsSummaryDto {
    private List<MonthlyTaxLiabilityDto> taxLiabilities;
    private List<ValidationErrorDistributionDto> errorDistribution;

    public List<MonthlyTaxLiabilityDto> getTaxLiabilities() {
        return taxLiabilities;
    }

    public void setTaxLiabilities(List<MonthlyTaxLiabilityDto> taxLiabilities) {
        this.taxLiabilities = taxLiabilities;
    }

    public List<ValidationErrorDistributionDto> getErrorDistribution() {
        return errorDistribution;
    }

    public void setErrorDistribution(List<ValidationErrorDistributionDto> errorDistribution) {
        this.errorDistribution = errorDistribution;
    }
}
