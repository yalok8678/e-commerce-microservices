package com.ecommerce.vendor.controller;

import com.ecommerce.vendor.dto.UpdateVendorRequest;
import com.ecommerce.vendor.dto.VendorRequest;
import com.ecommerce.vendor.dto.VendorResponse;
import com.ecommerce.vendor.service.VendorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/vendors")
@RequiredArgsConstructor
public class VendorController {

    private final VendorService vendorService;

    @PostMapping
    public ResponseEntity<VendorResponse> createVendor(
            Authentication authentication,
            @Valid @RequestBody VendorRequest request) {

        Long ownerUserId =
                (Long) authentication.getPrincipal();

        VendorResponse response =
                vendorService.createVendor(
                        ownerUserId,
                        request
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<VendorResponse> getVendor(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                vendorService.getVendorById(id)
        );
    }

    @GetMapping("/me")
    public ResponseEntity<VendorResponse> getMyVendor(
            Authentication authentication) {

        Long ownerUserId =
                (Long) authentication.getPrincipal();

        return ResponseEntity.ok(
                vendorService.getVendorByOwnerUserId(ownerUserId)
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<VendorResponse> updateVendor(
            @PathVariable Long id,
            Authentication authentication,
            @Valid @RequestBody UpdateVendorRequest request) {

        Long ownerUserId =
                (Long) authentication.getPrincipal();

        return ResponseEntity.ok(
                vendorService.updateVendor(
                        id,
                        ownerUserId,
                        request
                )
        );
    }

    @PutMapping("/{id}/approve")
    public ResponseEntity<VendorResponse> approveVendor(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                vendorService.approveVendor(id)
        );
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<VendorResponse> rejectVendor(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                vendorService.rejectVendor(id)
        );
    }
}

