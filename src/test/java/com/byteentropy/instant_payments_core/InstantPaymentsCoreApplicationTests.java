package com.byteentropy.instant_payments_core;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test") // This tells it to use your application-test.properties
@EmbeddedKafka(
    partitions = 1, 
    topics = {"payment-requests", "payment-results"}, 
    bootstrapServersProperty = "spring.kafka.bootstrap-servers"
)
class InstantPaymentsCoreApplicationTests {

    @Test
    void contextLoads() {
        // This test will now pass because EmbeddedKafka is present
    }

}