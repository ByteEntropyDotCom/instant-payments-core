package com.byteentropy.instant_payments_core.controller;

import com.byteentropy.instant_payments_core.event.PaymentEvent;
import com.byteentropy.instant_payments_core.model.PaymentRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payments")
public class PaymentController {

    private static final Logger log = LoggerFactory.getLogger(PaymentController.class);
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public PaymentController(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @PostMapping("/send")
    public String initiatePayment(@Valid @RequestBody PaymentRequest request) {
        PaymentEvent event = new PaymentEvent(
            request.transactionId(), 
            request.rail(), 
            request.sender(), 
            request.receiver(), 
            request.amount()
        );

        log.info("API received payment request: {} for rail: {}", request.transactionId(), request.rail());

        // We push to the ingestion topic. The Core Consumer will then route it to the specific rail topic.
        kafkaTemplate.send("payment-requests", request.transactionId(), event);
        
        return "PAYMENT_ACCEPTED";
    }
}