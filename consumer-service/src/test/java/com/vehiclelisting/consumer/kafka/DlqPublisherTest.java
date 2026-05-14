package com.vehiclelisting.consumer.kafka;

import com.vehiclelisting.consumer.config.VehicleListingKafkaProperties;
import com.vehiclelisting.consumer.model.DlqMessage;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class DlqPublisherTest {

    private final KafkaTemplate<String, Object> kafkaTemplate = mock(KafkaTemplate.class);
    private final DlqPublisher publisher = new DlqPublisher(
            kafkaTemplate,
            new VehicleListingKafkaProperties("vehicle-listing-events", "vehicle-listing-events-dlq"),
            Clock.fixed(Instant.parse("2026-04-30T15:00:00Z"), ZoneOffset.UTC)
    );

    @Test
    void shouldPublishDlqMessageWithOriginalPayloadAndReason() {
        publisher.publish("{ bad-json", "Invalid JSON");

        ArgumentCaptor<DlqMessage> captor = ArgumentCaptor.forClass(DlqMessage.class);
        verify(kafkaTemplate).send(org.mockito.ArgumentMatchers.eq("vehicle-listing-events-dlq"), captor.capture());

        DlqMessage message = captor.getValue();
        assertThat(message.originalPayload()).isEqualTo("{ bad-json");
        assertThat(message.errorReason()).isEqualTo("Invalid JSON");
        assertThat(message.sourceTopic()).isEqualTo("vehicle-listing-events");
        assertThat(message.failedAt()).isEqualTo("2026-04-30T15:00:00Z");
    }
}
