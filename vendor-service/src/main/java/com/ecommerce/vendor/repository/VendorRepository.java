package com.ecommerce.vendor.repository;

import com.ecommerce.vendor.entity.Vendor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VendorRepository extends JpaRepository<Vendor, Long> {

    Optional<Vendor> findByOwnerUserId(Long ownerUserId);
}