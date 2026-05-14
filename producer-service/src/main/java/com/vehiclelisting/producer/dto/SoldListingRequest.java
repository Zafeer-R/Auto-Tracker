package com.vehiclelisting.producer.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record SoldListingRequest(
        @NotBlank String listingId,
        @NotBlank String vin,
        @NotBlank String make,
        @NotBlank String model,
        @NotNull @Min(1886) Integer year,
        @NotNull @DecimalMin(value = "0.0", inclusive = true) BigDecimal price,
        @NotBlank String dealerId
) {
}
