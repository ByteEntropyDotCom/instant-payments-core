package com.byteentropy.instant_payments_core;

import com.byteentropy.instant_payments_core.event.PaymentResultEvent;
import com.byteentropy.instant_payments_core.model.PaymentRequest;
import com.byteentropy.instant_payments_core.repository.LedgerRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@EmbeddedKafka(
    partitions = 1, 
    // We must include the outbound topics used by strategies
    topics = {
        "payment-requests", 
        "payment-results", 
        "fps-outbound-test", 
        "sepa-outbound-test", 
        "fast-outbound-test"
    }, 
    bootstrapServersProperty = "spring.kafka.bootstrap-servers"
)
public class PaymentIntegrationTest {

    @Autowired 
    private TestRestTemplate restTemplate;

    @Autowired 
    private LedgerRepository ledgerRepository;

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Test
    @DisplayName("1. Happy Path: Successful Dispatch to Provider")
    void testFullFlow() {
        String txId = UUID.randomUUID().toString();
        PaymentRequest req = new PaymentRequest(txId, "FPS", "SENDER", "RECEIVER", new BigDecimal("100.00"));

        // Step 1: Hit API
        var response = restTemplate.postForEntity("/api/v1/payments/send", req, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        // Step 2: Since we have NO real provider running, the test simulates the Provider's response.
        // In a real scenario, the provider listens to 'fps-outbound-test' and sends to 'payment-results'.
        kafkaTemplate.send("payment-results", txId, 
            new PaymentResultEvent(txId, "SUCCESS", "Processed by Test Provider", new BigDecimal("100.00")));

        // Step 3: Wait for PaymentResultConsumer to pick it up and update the ledger
        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            var entries = ledgerRepository.findByTransactionId(txId);
            assertThat(entries).hasSize(1);
            assertThat(entries.get(0).getAmount()).isEqualByComparingTo("100.00");
        });
    }

    @Test
    @DisplayName("2. Validation Path: Should reject negative amounts at Controller")
    void testValidationFailure() {
        String txId = UUID.randomUUID().toString();
        PaymentRequest req = new PaymentRequest(txId, "FPS", "S1", "R1", new BigDecimal("-50.00"));

        var response = restTemplate.postForEntity("/api/v1/payments/send", req, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(ledgerRepository.findByTransactionId(txId)).isEmpty();
    }

    @Test
    @DisplayName("3. Idempotency Path: Core should not dispatch duplicates")
    void testIdempotency() {
        String txId = "duplicate-" + UUID.randomUUID();
        PaymentRequest req = new PaymentRequest(txId, "FPS", "S1", "R1", new BigDecimal("10.00"));

        // Record it manually in ledger first to simulate a previous successful run
        kafkaTemplate.send("payment-results", txId, 
            new PaymentResultEvent(txId, "SUCCESS", "First Run", new BigDecimal("10.00")));
        
        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> 
            assertThat(ledgerRepository.findByTransactionId(txId)).hasSize(1)
        );

        // Try to send again - PaymentService should see it in the DB via IdempotencyManager and skip
        restTemplate.postForEntity("/api/v1/payments/send", req, String.class);

        // Verify ledger still only has 1 entry
        await().during(3, TimeUnit.SECONDS).untilAsserted(() -> {
            assertThat(ledgerRepository.findByTransactionId(txId)).hasSize(1);
        });
    }
}