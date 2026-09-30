package com.ecommerce.inventory.service;

import com.ecommerce.inventory.dto.InventoryRequest;
import com.ecommerce.inventory.dto.InventoryResponse;
import com.ecommerce.inventory.entity.Inventory;
import com.ecommerce.inventory.entity.InventoryReservation;
import com.ecommerce.inventory.repository.InventoryRepository;
import com.ecommerce.inventory.repository.InventoryReservationRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final RedisLockService redisLockService;

    private final InventoryReservationRepository
            inventoryReservationRepository;


    // ======================================================
    // CREATE INVENTORY
    // ======================================================

    @Transactional
    public InventoryResponse createInventory(
            InventoryRequest request) {

        if (inventoryRepository
                .existsByProductId(request.getProductId())) {

            throw new RuntimeException(
                    "Inventory already exists for this product"
            );
        }

        Inventory inventory =
                Inventory.builder()
                        .productId(request.getProductId())
                        .availableQuantity(
                                request.getQuantity()
                        )
                        .reservedQuantity(0)
                        .build();

        Inventory savedInventory =
                inventoryRepository.save(inventory);

        return toResponse(savedInventory);
    }


    // ======================================================
    // GET INVENTORY
    // ======================================================

    public InventoryResponse getInventoryByProductId(
            Long productId) {

        Inventory inventory =
                getInventoryEntity(productId);

        return toResponse(inventory);
    }


    // ======================================================
    // ADD STOCK
    // ======================================================

    @Transactional
    public InventoryResponse addStock(
            Long productId,
            Integer quantity) {

        Inventory inventory =
                getInventoryEntity(productId);

        inventory.setAvailableQuantity(
                inventory.getAvailableQuantity()
                        + quantity
        );

        return toResponse(
                inventoryRepository.save(inventory)
        );
    }


    // ======================================================
    // REMOVE STOCK
    // ======================================================

    @Transactional
    public InventoryResponse removeStock(
            Long productId,
            Integer quantity) {

        Inventory inventory =
                getInventoryEntity(productId);

        if (inventory.getAvailableQuantity()
                < quantity) {

            throw new RuntimeException(
                    "Insufficient available stock"
            );
        }

        inventory.setAvailableQuantity(
                inventory.getAvailableQuantity()
                        - quantity
        );

        return toResponse(
                inventoryRepository.save(inventory)
        );
    }


    // ======================================================
    // NORMAL RESERVE STOCK
    // ======================================================
    //
    // KEEPING YOUR ORIGINAL METHOD.
    // Existing controllers/code can continue using it.
    //

    @Transactional
    public InventoryResponse reserveStock(
            Long productId,
            Integer quantity) {

        Inventory inventory =
                getInventoryEntity(productId);

        reserveInventoryQuantity(
                inventory,
                quantity
        );

        return toResponse(
                inventoryRepository.save(inventory)
        );
    }


    // ======================================================
    // SAGA RESERVE STOCK
    // ======================================================
    @Transactional
    public InventoryResponse reserveStockForOrder(
            Long orderId,
            Long productId,
            Integer quantity) {

        String lockKey = "lock:inventory:product:" + productId;

        String lockValue = redisLockService.acquireLock(
                lockKey,
                Duration.ofSeconds(10)
        );

        if (lockValue == null) {
            throw new RuntimeException(
                    "Could not acquire inventory lock for product: "
                            + productId
            );
        }

        try {

            Inventory inventory =
                    getInventoryEntity(productId);

            reserveInventoryQuantity(
                    inventory,
                    quantity
            );

            inventoryRepository.save(inventory);

            InventoryReservation reservation =
                    InventoryReservation.builder()
                            .orderId(orderId)
                            .productId(productId)
                            .quantity(quantity)
                            .status("RESERVED")
                            .createdAt(Instant.now())
                            .build();

            inventoryReservationRepository.save(
                    reservation
            );

            log.info(
                    "Inventory reserved under Redis lock: " +
                            "orderId={}, productId={}, quantity={}",
                    orderId,
                    productId,
                    quantity
            );

            return toResponse(inventory);

        } finally {

            redisLockService.releaseLock(
                    lockKey,
                    lockValue
            );
        }
    }
    // ======================================================
    // RELEASE STOCK
    // ======================================================

    @Transactional
    public InventoryResponse releaseStock(
            Long productId,
            Integer quantity) {

        Inventory inventory =
                getInventoryEntity(productId);

        if (inventory.getReservedQuantity()
                < quantity) {

            throw new RuntimeException(
                    "Cannot release more than reserved stock"
            );
        }

        inventory.setReservedQuantity(
                inventory.getReservedQuantity()
                        - quantity
        );

        inventory.setAvailableQuantity(
                inventory.getAvailableQuantity()
                        + quantity
        );

        return toResponse(
                inventoryRepository.save(inventory)
        );
    }


    // ======================================================
    // RELEASE ORDER RESERVATIONS
    // ======================================================

    @Transactional
    public void releaseOrderReservations(
            Long orderId) {

        List<InventoryReservation> reservations =
                inventoryReservationRepository
                        .findByOrderIdAndStatus(
                                orderId,
                                "RESERVED"
                        );

        if (reservations.isEmpty()) {

            log.info(
                    "No active reservations found: orderId={}",
                    orderId
            );

            return;
        }

        for (InventoryReservation reservation :
                reservations) {

            Inventory inventory =
                    getInventoryEntity(
                            reservation.getProductId()
                    );

            if (inventory.getReservedQuantity()
                    < reservation.getQuantity()) {

                throw new RuntimeException(
                        "Invalid reserved quantity for product: "
                                + reservation.getProductId()
                );
            }

            // ------------------------------------------
            // Return reserved stock
            // ------------------------------------------

            inventory.setReservedQuantity(
                    inventory.getReservedQuantity()
                            - reservation.getQuantity()
            );

            inventory.setAvailableQuantity(
                    inventory.getAvailableQuantity()
                            + reservation.getQuantity()
            );

            inventoryRepository.save(inventory);

            // ------------------------------------------
            // Mark reservation released
            // ------------------------------------------

            reservation.setStatus("RELEASED");

            inventoryReservationRepository.save(
                    reservation
            );

            log.info(
                    "Inventory released: " +
                            "orderId={}, productId={}, quantity={}",
                    orderId,
                    reservation.getProductId(),
                    reservation.getQuantity()
            );
        }
    }


    // ======================================================
    // INTERNAL RESERVATION LOGIC
    // ======================================================

    private void reserveInventoryQuantity(
            Inventory inventory,
            Integer quantity) {

        if (inventory.getAvailableQuantity()
                < quantity) {

            throw new RuntimeException(
                    "Insufficient available stock"
            );
        }

        inventory.setAvailableQuantity(
                inventory.getAvailableQuantity()
                        - quantity
        );

        inventory.setReservedQuantity(
                inventory.getReservedQuantity()
                        + quantity
        );
    }


    // ======================================================
    // GET INVENTORY ENTITY
    // ======================================================

    private Inventory getInventoryEntity(
            Long productId) {

        return inventoryRepository
                .findByProductId(productId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Inventory not found"
                        ));
    }


    // ======================================================
    // CONVERT TO RESPONSE
    // ======================================================

    private InventoryResponse toResponse(
            Inventory inventory) {

        return new InventoryResponse(
                inventory.getId(),
                inventory.getProductId(),
                inventory.getAvailableQuantity(),
                inventory.getReservedQuantity(),
                inventory.getAvailableQuantity()
                        + inventory.getReservedQuantity()
        );
    }
}

