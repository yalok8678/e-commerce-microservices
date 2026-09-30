package com.ecommerce.vendor.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class VendorResponse {

    private Long id;
    private Long ownerUserId;
    private String businessName;
    private String description;
    private String status;
}