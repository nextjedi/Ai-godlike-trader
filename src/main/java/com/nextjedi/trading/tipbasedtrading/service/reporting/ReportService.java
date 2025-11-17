package com.nextjedi.trading.tipbasedtrading.service.reporting;

import com.nextjedi.trading.tipbasedtrading.dao.TradeModelRepository;
import com.nextjedi.trading.tipbasedtrading.events.EventPublisher;
import com.nextjedi.trading.tipbasedtrading.events.reporting.ReportGeneratedEvent;
import com.nextjedi.trading.tipbasedtrading.models.TradeModel;
import com.nextjedi.trading.tipbasedtrading.models.TradeStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for generating trading reports
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ReportService {

    private final TradeModelRepository tradeRepository;
    private final PDFReportGenerator pdfGenerator;
    private final EventPublisher eventPublisher;

    /**
     * Generate daily P&L report
     */
    public String generateDailyPnLReport(LocalDate date) {
        log.info("Generating daily P&L report for {}", date);

        // Get all completed trades for the date
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(23, 59, 59);

        List<TradeModel> trades = tradeRepository.findAll().stream()
                .filter(t -> t.getTradeStatus() == TradeStatus.COMPLETED)
                .filter(t -> {
                    LocalDateTime createdAt = LocalDateTime.ofInstant(
                            t.getCreatedAt(), ZoneId.systemDefault());
                    return createdAt.isAfter(startOfDay) && createdAt.isBefore(endOfDay);
                })
                .collect(Collectors.toList());

        // Calculate metrics
        Double totalPnl = trades.stream()
                .filter(t -> t.getPnl() != null)
                .mapToDouble(TradeModel::getPnl)
                .sum();

        long winningTrades = trades.stream()
                .filter(t -> t.getPnl() != null && t.getPnl() > 0)
                .count();

        Double winRate = trades.isEmpty() ? 0.0 :
                (winningTrades * 100.0) / trades.size();

        // Generate PDF
        String filePath = pdfGenerator.generateDailyPnLReport(date, trades, totalPnl, winRate);

        // Publish event
        publishReportGeneratedEvent("DAILY_PNL", date, filePath, "PDF");

        log.info("Daily P&L report generated: {}", filePath);
        return filePath;
    }

    /**
     * Generate trade journal report for a date range
     */
    public String generateTradeJournalReport(LocalDate startDate, LocalDate endDate) {
        log.info("Generating trade journal report from {} to {}", startDate, endDate);

        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(23, 59, 59);

        List<TradeModel> trades = tradeRepository.findAll().stream()
                .filter(t -> {
                    LocalDateTime createdAt = LocalDateTime.ofInstant(
                            t.getCreatedAt(), ZoneId.systemDefault());
                    return createdAt.isAfter(start) && createdAt.isBefore(end);
                })
                .collect(Collectors.toList());

        // Generate PDF
        String filePath = pdfGenerator.generateTradeJournalReport(startDate, endDate, trades);

        // Publish event
        publishReportGeneratedEvent("TRADE_JOURNAL", startDate, filePath, "PDF");

        log.info("Trade journal report generated: {}", filePath);
        return filePath;
    }

    /**
     * Scheduled daily report generation (3:30 PM on weekdays)
     */
    @Scheduled(cron = "${trading.reporting.schedule.daily:0 30 15 * * MON-FRI}")
    public void scheduledDailyReport() {
        try {
            log.info("Running scheduled daily P&L report generation");
            generateDailyPnLReport(LocalDate.now());
        } catch (Exception e) {
            log.error("Failed to generate scheduled daily report: {}", e.getMessage(), e);
        }
    }

    /**
     * Scheduled weekly report generation (Saturday 10 AM)
     */
    @Scheduled(cron = "${trading.reporting.schedule.weekly:0 0 10 * * SAT}")
    public void scheduledWeeklyReport() {
        try {
            log.info("Running scheduled weekly trade journal generation");
            LocalDate endDate = LocalDate.now().minusDays(1);
            LocalDate startDate = endDate.minusDays(6);
            generateTradeJournalReport(startDate, endDate);
        } catch (Exception e) {
            log.error("Failed to generate scheduled weekly report: {}", e.getMessage(), e);
        }
    }

    /**
     * Generate portfolio summary report
     */
    public String generatePortfolioSummaryReport(String portfolioName) {
        // TODO: Implement portfolio summary report
        log.info("Portfolio summary report generation not yet implemented");
        return null;
    }

    /**
     * Generate performance analytics report
     */
    public String generatePerformanceAnalyticsReport(LocalDate startDate, LocalDate endDate) {
        // TODO: Implement performance analytics report
        log.info("Performance analytics report generation not yet implemented");
        return null;
    }

    // Helper methods

    private void publishReportGeneratedEvent(String reportType, LocalDate reportDate,
                                            String filePath, String format) {
        File file = new File(filePath);
        long fileSize = file.exists() ? file.length() : 0;

        ReportGeneratedEvent event = ReportGeneratedEvent.builder()
                .reportId(UUID.randomUUID().toString())
                .reportType(reportType)
                .reportDate(reportDate)
                .filePath(filePath)
                .fileSizeBytes(fileSize)
                .format(format)
                .build();

        eventPublisher.publish(event);
    }
}
