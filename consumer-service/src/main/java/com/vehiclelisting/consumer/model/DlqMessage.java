package com.vehiclelisting.consumer.model;

public record DlqMessage(
        String originalPayload,
        String errorReason,
        String sourceTopic,
        String failedAt
) {
}
