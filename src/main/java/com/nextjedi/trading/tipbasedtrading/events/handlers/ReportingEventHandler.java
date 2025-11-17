package com.nextjedi.trading.tipbasedtrading.events.handlers;

import com.nextjedi.trading.tipbasedtrading.events.reporting.ReportEmailedEvent;
import com.nextjedi.trading.tipbasedtrading.events.reporting.ReportGeneratedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Handles reporting-related domain events
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ReportingEventHandler {

    /**
     * Handle report generated event
     * - Email report if configured
     * - Archive report
     * - Update report history
     */
    @Async
    @EventListener
    public void handleReportGenerated(ReportGeneratedEvent event) {
        log.info("Report generated - ID: {}, Type: {}, Date: {}, Format: {}, Size: {} bytes, Path: {}",
                event.getReportId(),
                event.getReportType(),
                event.getReportDate(),
                event.getFormat(),
                event.getFileSizeBytes(),
                event.getFilePath());

        // TODO: Email report if enabled
        // if (reportConfig.isEmailEnabled()) {
        //     emailService.sendReport(event);
        // }

        // TODO: Archive old reports
        // reportArchiveService.archiveIfNeeded(event.getReportType());

        // TODO: Update report history
        // reportHistoryService.recordReport(event);
    }

    /**
     * Handle report emailed event
     */
    @Async
    @EventListener
    public void handleReportEmailed(ReportEmailedEvent event) {
        if (event.isSuccess()) {
            log.info("Report emailed successfully - ID: {}, Type: {}, Recipients: {}, Subject: {}",
                    event.getReportId(),
                    event.getReportType(),
                    event.getRecipients(),
                    event.getSubject());
        } else {
            log.error("Failed to email report - ID: {}, Type: {}, Error: {}",
                    event.getReportId(),
                    event.getReportType(),
                    event.getErrorMessage());
        }

        // TODO: Update delivery status
        // reportDeliveryService.updateDeliveryStatus(event);

        // TODO: Audit logging
        // auditService.logReportDelivery(event);
    }
}
