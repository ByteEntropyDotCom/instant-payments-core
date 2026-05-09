package com.byteentropy.instant_payments_core.consumer;

import com.byteentropy.instant_payments_core.event.PaymentResultEvent;
import com.byteentropy.instant_payments_core.model.PaymentResponse;
import com.byteentropy.instant_payments_core.service.LedgerService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
public class PaymentResultConsumer {
    private final LedgerService ledgerService;
    private final SimpMessagingTemplate messagingTemplate;

    public PaymentResultConsumer(LedgerService ls, SimpMessagingTemplate mt) {
        this.ledgerService = ls;
        this.messagingTemplate = mt;
    }

    @KafkaListener(topics = "payment-results", groupId = "payment-core-group")
    public void consume(PaymentResultEvent event) {
        System.out.println("DEBUG: Final Result received for TX: " + event.transactionId() + " Status: " + event.status());
        
        if ("SUCCESS".equals(event.status())) {
            ledgerService.recordTransaction(event.transactionId(), event.amount());
        }
        
        messagingTemplate.convertAndSend("/topic/payments", new PaymentResponse(event.transactionId(), event.status(), event.message()));
    }
}