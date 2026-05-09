package com.byteentropy.instant_payments_core.controller;
import com.byteentropy.instant_payments_core.model.PaymentResponse;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class WebSocketController {
    @MessageMapping("/payment-updates")
    @SendTo("/topic/payments")
    public PaymentResponse broadcastUpdate(PaymentResponse response) { return response; }
}