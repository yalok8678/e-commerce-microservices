
        package com.ecommerce.vendor.service;

import com.ecommerce.vendor.dto.UpdateVendorRequest;
import com.ecommerce.vendor.dto.VendorRequest;
import com.ecommerce.vendor.dto.VendorResponse;
import com.ecommerce.vendor.entity.Vendor;
import com.ecommerce.vendor.repository.VendorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class VendorService {

    private final VendorRepository vendorRepository;

    public VendorResponse createVendor(
            Long ownerUserId,
            VendorRequest request) {

        if (vendorRepository.findByOwnerUserId(ownerUserId).isPresent()) {
            throw new RuntimeException(
                    "User already has a vendor account");
        }

        Vendor vendor = Vendor.builder()
                .ownerUserId(ownerUserId)
                .businessName(request.getBusinessName())
                .description(request.getDescription())
                .status("PENDING")
                .build();

        Vendor savedVendor = vendorRepository.save(vendor);

        return toResponse(savedVendor);
    }

    public VendorResponse getVendorById(Long id) {

        Vendor vendor = vendorRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Vendor not found"));

        return toResponse(vendor);
    }

    public VendorResponse getVendorByOwnerUserId(Long ownerUserId) {

        Vendor vendor = vendorRepository.findByOwnerUserId(ownerUserId)
                .orElseThrow(() ->
                        new RuntimeException("Vendor not found"));

        return toResponse(vendor);
    }

    public VendorResponse updateVendor(
            Long vendorId,
            Long ownerUserId,
            UpdateVendorRequest request) {

        Vendor vendor = vendorRepository.findById(vendorId)
                .orElseThrow(() ->
                        new RuntimeException("Vendor not found"));

        if (!vendor.getOwnerUserId().equals(ownerUserId)) {
            throw new RuntimeException(
                    "You are not allowed to modify this vendor");
        }

        vendor.setBusinessName(request.getBusinessName());
        vendor.setDescription(request.getDescription());

        Vendor updatedVendor = vendorRepository.save(vendor);

        return toResponse(updatedVendor);
    }

    private VendorResponse toResponse(Vendor vendor) {

        return new VendorResponse(
                vendor.getId(),
                vendor.getOwnerUserId(),
                vendor.getBusinessName(),
                vendor.getDescription(),
                vendor.getStatus()
        );
    }
    public VendorResponse approveVendor(Long vendorId) {

        Vendor vendor = vendorRepository.findById(vendorId)
                .orElseThrow(() ->
                        new RuntimeException("Vendor not found"));

        if (!vendor.getStatus().equals("PENDING")) {
            throw new RuntimeException(
                    "Only pending vendors can be approved");
        }

        vendor.setStatus("APPROVED");

        Vendor updatedVendor = vendorRepository.save(vendor);

        return toResponse(updatedVendor);
    }

    public VendorResponse rejectVendor(Long vendorId) {

        Vendor vendor = vendorRepository.findById(vendorId)
                .orElseThrow(() ->
                        new RuntimeException("Vendor not found"));

        if (!vendor.getStatus().equals("PENDING")) {
            throw new RuntimeException(
                    "Only pending vendors can be rejected");
        }

        vendor.setStatus("REJECTED");

        Vendor updatedVendor = vendorRepository.save(vendor);

        return toResponse(updatedVendor);
    }
}
