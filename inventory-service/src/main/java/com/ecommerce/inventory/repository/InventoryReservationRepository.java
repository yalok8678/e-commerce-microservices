package com.ecommerce.inventory.repository;

import com.ecommerce.inventory.entity.InventoryReservation;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InventoryReservationRepository
        extends JpaRepository<InventoryReservation, Long> {

    List<InventoryReservation> findByOrderIdAndStatus(
            Long orderId,
            String status
    );
}