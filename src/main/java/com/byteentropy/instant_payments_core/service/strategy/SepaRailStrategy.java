package com.byteentropy.instant_payments_core.service.strategy;

import com.byteentropy.instant_payments_core.model.PaymentRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class SepaRailStrategy implements PaymentRailStrategy {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${app.kafka.topics.sepa-outbound}")
    private String outboundTopic;

    public SepaRailStrategy(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override 
    public void dispatch(PaymentRequest r) { 
        kafkaTemplate.send(outboundTopic, r.transactionId(), r);
    }

    @Override 
    public String getRailName() { return "SEPA"; }
}