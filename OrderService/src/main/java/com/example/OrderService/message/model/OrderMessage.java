
package com.example.OrderService.message.model;


import com.example.OrderService.dto.OrderItemDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderMessage {
    private String orderId;
    private Long userId;
    private List<OrderItemDTO> items;
    private double totalPrice;
}
