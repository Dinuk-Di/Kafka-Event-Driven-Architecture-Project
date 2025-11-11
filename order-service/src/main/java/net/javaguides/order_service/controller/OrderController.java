package net.javaguides.order_service.controller;

import java.util.UUID;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import net.javaguides.base_domains.dto.Order;
import net.javaguides.base_domains.dto.OrderEvent;
import net.javaguides.order_service.kafka.OrderProducer;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    
    private OrderProducer orderProducer;

    public OrderController(OrderProducer orderProducer) {
        this.orderProducer = orderProducer;
    }

    @PostMapping("/")
    public String placeHolder(@RequestBody Order order) {
        order.setOrderId(UUID.randomUUID().toString());
        OrderEvent orderEvent = new OrderEvent();
        orderEvent.setStatus("PENDING");
        orderEvent.setMessage("Order is in PENDING state");
        orderEvent.setOrder(order);
        orderProducer.sendMessage(orderEvent);

        return "Order Placed Successfully";
    }
}
