package com.byteentropy.instant_payments_core.controller;

import com.byteentropy.instant_payments_core.event.PaymentEvent;
import com.byteentropy.instant_payments_core.model.PaymentRequest;
import jakarta.validation.Valid;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payments")
public class PaymentController {

    // Explicitly define the types here so Spring matches the JSON Serializer
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public PaymentController(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @PostMapping("/send")
    public String initiatePayment(@Valid @RequestBody PaymentRequest request) {
        // Create the event
        PaymentEvent event = new PaymentEvent(
            request.transactionId(), 
            request.rail(), 
            request.sender(), 
            request.receiver(), 
            request.amount()
        );

        // Send to Kafka - Spring will now use the JsonSerializer from application.properties
        kafkaTemplate.send("payment-requests", request.transactionId(), event);
        
        return "PAYMENT_ACCEPTED";
    }
}