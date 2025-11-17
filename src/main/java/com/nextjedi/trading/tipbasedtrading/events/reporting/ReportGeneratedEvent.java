package com.nextjedi.trading.tipbasedtrading.events.reporting;

import com.nextjedi.trading.tipbasedtrading.events.DomainEvent;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

/**
 * Event published when a report is generated
 */
@Getter
public class ReportGeneratedEvent extends DomainEvent {
    private final String reportId;
    private final String reportType; // DAILY_PNL, TRADE_JOURNAL, PORTFOLIO_SUMMARY, PERFORMANCE_ANALYTICS
    private final LocalDate reportDate;
    private final String filePath;
    private final Long fileSizeBytes;
    private final String format; // PDF, EXCEL, HTML

    @Builder
    public ReportGeneratedEvent(String reportId, String reportType, LocalDate reportDate,
                               String filePath, Long fileSizeBytes, String format) {
        super();
        this.reportId = reportId;
        this.reportType = reportType;
        this.reportDate = reportDate;
        this.filePath = filePath;
        this.fileSizeBytes = fileSizeBytes;
        this.format = format;
    }

    @Override
    public String getAggregateId() {
        return reportId;
    }
}
