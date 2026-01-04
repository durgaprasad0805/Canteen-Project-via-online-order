package com.klcanteen.controller;

import com.klcanteen.model.Order;
import com.klcanteen.repository.OrderRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@RestController
public class OrderController {

    private final OrderRepository orderRepository;
    private final Random random = new Random();

    public OrderController(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @PostMapping(path = "/saveOrder")
    public ResponseEntity<String> saveOrder(
            @RequestParam(name = "totalAmount", required = false) Double totalAmount,
            @RequestParam(name = "itemsJson", required = false) String itemsJson
    ) {
        if (totalAmount == null) {
            totalAmount = 0.0;
        }
        if (itemsJson == null) {
            itemsJson = "[]";
        }

        String orderCode = generateOrderCode();
        Order order = new Order(orderCode, totalAmount, itemsJson);
        orderRepository.save(order);

        String msg = "Order saved successfully with code: " + orderCode;
        return ResponseEntity.ok(msg);
    }

    @GetMapping("/api/orders/{orderCode}")
    public ResponseEntity<Map<String, Object>> trackOrder(@PathVariable String orderCode) {
        return orderRepository.findByOrderCode(orderCode)
                .map(order -> {
                    Map<String, Object> body = new HashMap<>();
                    body.put("orderCode", order.getOrderCode());
                    body.put("totalAmount", order.getTotalAmount());
                    body.put("createdAt", order.getCreatedAt().toString());
                    body.put("status", computeStatus(order.getCreatedAt()));
                    return ResponseEntity.ok(body);
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    private String computeStatus(LocalDateTime createdAt) {
        if (createdAt == null) {
            return "Order placed";
        }
        long minutes = Duration.between(createdAt, LocalDateTime.now()).toMinutes();
        if (minutes < 2) {
            return "Order placed";
        } else if (minutes < 10) {
            return "Being prepared";
        } else if (minutes < 20) {
            return "Out for delivery";
        } else {
            return "Delivered";
        }
    }

    private String generateOrderCode() {
        LocalDateTime now = LocalDateTime.now();
        String ts = now.format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        int rand = random.nextInt(9000) + 1000;
        return "ORD-" + ts + "-" + rand;
    }
}