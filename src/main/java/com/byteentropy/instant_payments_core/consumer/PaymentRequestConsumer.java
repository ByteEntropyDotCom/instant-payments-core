package com.byteentropy.instant_payments_core.consumer;

import com.byteentropy.instant_payments_core.event.PaymentEvent;
import com.byteentropy.instant_payments_core.model.PaymentRequest;
import com.byteentropy.instant_payments_core.service.PaymentService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class PaymentRequestConsumer {
    private final PaymentService service;

    public PaymentRequestConsumer(PaymentService service) {
        this.service = service;
    }

    @KafkaListener(topics = "payment-requests", groupId = "payment-core-group")
    public void consume(PaymentEvent event) {
        System.out.println("DEBUG: Core received request for TX: " + event.transactionId());
        
        service.processPayment(new PaymentRequest(
            event.transactionId(), 
            event.rail(), 
            event.sender(), 
            event.receiver(), 
            event.amount()
        ));
    }
}