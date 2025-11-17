package com.nextjedi.trading.tipbasedtrading.events.reporting;

import com.nextjedi.trading.tipbasedtrading.events.DomainEvent;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * Event published when a report is emailed to recipients
 */
@Getter
public class ReportEmailedEvent extends DomainEvent {
    private final String reportId;
    private final String reportType;
    private final List<String> recipients;
    private final String subject;
    private final boolean success;
    private final String errorMessage;

    @Builder
    public ReportEmailedEvent(String reportId, String reportType, List<String> recipients,
                             String subject, boolean success, String errorMessage) {
        super();
        this.reportId = reportId;
        this.reportType = reportType;
        this.recipients = recipients;
        this.subject = subject;
        this.success = success;
        this.errorMessage = errorMessage;
    }

    @Override
    public String getAggregateId() {
        return reportId;
    }
}
