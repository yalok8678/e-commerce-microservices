package com.ecommerce.vendor.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateVendorRequest {

    @NotBlank(message = "Business name is required")
    private String businessName;

    private String description;
}