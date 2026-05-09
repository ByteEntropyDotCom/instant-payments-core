package com.byteentropy.instant_payments_core;

import com.byteentropy.instant_payments_core.model.PaymentRequest;
import com.byteentropy.instant_payments_core.repository.LedgerRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
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
    topics = {"payment-requests", "payment-results"}, 
    bootstrapServersProperty = "spring.kafka.bootstrap-servers"
)
public class PaymentIntegrationTest {

    @Autowired 
    private TestRestTemplate restTemplate;

    @Autowired 
    private LedgerRepository ledgerRepository;

    @Test
    @DisplayName("1. Happy Path: Successful API to Ledger Flow")
    void testFullFlow() {
        String txId = UUID.randomUUID().toString();
        PaymentRequest req = new PaymentRequest(txId, "FPS", "SENDER", "RECEIVER", new BigDecimal("100.00"));

        var response = restTemplate.postForEntity("/api/v1/payments/send", req, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo("PAYMENT_ACCEPTED");

        // Wait for the asynchronous chain to complete and hit the DB
        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            var entries = ledgerRepository.findByTransactionId(txId);
            assertThat(entries).hasSize(1);
            assertThat(entries.get(0).getAmount()).isEqualByComparingTo("100.00");
        });
    }

    @Test
    @DisplayName("2. Validation Path: Should reject negative amounts")
    void testValidationFailure() {
        String txId = UUID.randomUUID().toString();
        // Negative amount should trigger @Positive validation
        PaymentRequest req = new PaymentRequest(txId, "FPS", "S1", "R1", new BigDecimal("-50.00"));

        var response = restTemplate.postForEntity("/api/v1/payments/send", req, String.class);

        // Should return 400 Bad Request
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        
        // Verify no ledger entry was created
        var entries = ledgerRepository.findByTransactionId(txId);
        assertThat(entries).isEmpty();
    }

    @Test
    @DisplayName("3. Idempotency Path: Duplicate TX IDs should not double-record")
    void testIdempotency() {
        String txId = "duplicate-" + UUID.randomUUID();
        PaymentRequest req = new PaymentRequest(txId, "FPS", "S1", "R1", new BigDecimal("10.00"));

        // Send twice rapidly
        restTemplate.postForEntity("/api/v1/payments/send", req, String.class);
        restTemplate.postForEntity("/api/v1/payments/send", req, String.class);

        // Wait to ensure both messages were processed by the consumers
        await()
            .atMost(10, TimeUnit.SECONDS)
            .pollInterval(1, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                var entries = ledgerRepository.findByTransactionId(txId);
                // This will now pass because of the check in LedgerService
                assertThat(entries).hasSize(1);
            });
    }
}