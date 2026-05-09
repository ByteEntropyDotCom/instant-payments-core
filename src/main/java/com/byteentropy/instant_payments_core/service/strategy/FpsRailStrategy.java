package com.byteentropy.instant_payments_core.service.strategy;

import com.byteentropy.instant_payments_core.model.PaymentRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class FpsRailStrategy implements PaymentRailStrategy {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${app.kafka.topics.fps-outbound}")
    private String outboundTopic;

    public FpsRailStrategy(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override 
    public void dispatch(PaymentRequest r) { 
        kafkaTemplate.send(outboundTopic, r.transactionId(), r);
    }

    @Override 
    public String getRailName() { return "FPS"; }
}