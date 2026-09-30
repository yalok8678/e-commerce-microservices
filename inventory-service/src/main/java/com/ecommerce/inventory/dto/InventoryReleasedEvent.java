package com.ecommerce.inventory.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryReleasedEvent {

    private Long orderId;

    private Long userId;

    private String status;
}