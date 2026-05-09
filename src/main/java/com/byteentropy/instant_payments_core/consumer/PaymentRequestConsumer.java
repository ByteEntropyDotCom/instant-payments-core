package com.byteentropy.instant_payments_core.consumer;

import com.byteentropy.instant_payments_core.event.PaymentEvent;
import com.byteentropy.instant_payments_core.event.PaymentResultEvent;
import com.byteentropy.instant_payments_core.model.PaymentRequest;
import com.byteentropy.instant_payments_core.service.PaymentService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class PaymentRequestConsumer {
    private final PaymentService service;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public PaymentRequestConsumer(PaymentService service, KafkaTemplate<String, Object> kt) {
        this.service = service;
        this.kafkaTemplate = kt;
    }

    @KafkaListener(topics = "payment-requests", groupId = "payment-core-group")
    public void consume(PaymentEvent event) {
        System.out.println("DEBUG: Processing request for TX: " + event.transactionId());
        var res = service.processPayment(new PaymentRequest(event.transactionId(), event.rail(), event.sender(), event.receiver(), event.amount()));
        
        PaymentResultEvent resultEvent = new PaymentResultEvent(res.transactionId(), res.status(), res.message(), event.amount());
        kafkaTemplate.send("payment-results", event.transactionId(), resultEvent);
    }
}