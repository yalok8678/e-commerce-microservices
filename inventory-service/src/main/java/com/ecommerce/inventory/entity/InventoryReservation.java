package com.ecommerce.inventory.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(
        name = "inventory_reservations",
        indexes = {
                @Index(
                        name = "idx_reservation_order",
                        columnList = "order_id"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryReservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
            name = "order_id",
            nullable = false
    )
    private Long orderId;

    @Column(
            name = "product_id",
            nullable = false
    )
    private Long productId;

    @Column(
            nullable = false
    )
    private Integer quantity;

    @Column(
            nullable = false
    )
    private String status;

    @Column(
            nullable = false
    )
    private Instant createdAt;
}