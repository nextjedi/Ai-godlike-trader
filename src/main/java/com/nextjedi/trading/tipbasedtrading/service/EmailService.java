package com.nextjedi.trading.tipbasedtrading.service;

import com.nextjedi.trading.tipbasedtrading.events.EventPublisher;
import com.nextjedi.trading.tipbasedtrading.events.reporting.ReportEmailedEvent;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.List;

/**
 * Service for sending emails with reports
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    private final EventPublisher eventPublisher;

    @Value("${trading.reporting.email.enabled:false}")
    private boolean emailEnabled;

    @Value("${trading.reporting.email.from:trading@example.com}")
    private String fromEmail;

    @Value("${trading.reporting.email.recipients:}")
    private List<String> recipients;

    /**
     * Send report via email
     */
    public void sendReport(String reportId, String reportType, String subject,
                          String body, String filePath) {
        if (!emailEnabled) {
            log.debug("Email sending is disabled");
            return;
        }

        if (recipients == null || recipients.isEmpty()) {
            log.warn("No email recipients configured");
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setFrom(fromEmail);
            helper.setTo(recipients.toArray(new String[0]));
            helper.setSubject(subject);
            helper.setText(body, true); // true = HTML

            // Attach report file
            if (filePath != null) {
                FileSystemResource file = new FileSystemResource(new File(filePath));
                helper.addAttachment(file.getFilename(), file);
            }

            mailSender.send(message);

            log.info("Report email sent successfully to {} recipients", recipients.size());

            // Publish success event
            publishEmailedEvent(reportId, reportType, recipients, subject, true, null);

        } catch (MessagingException e) {
            log.error("Failed to send report email: {}", e.getMessage(), e);

            // Publish failure event
            publishEmailedEvent(reportId, reportType, recipients, subject, false, e.getMessage());

            throw new RuntimeException("Failed to send report email", e);
        }
    }

    /**
     * Send daily P&L report
     */
    public void sendDailyPnLReport(String reportId, String filePath, Double totalPnl, int tradeCount) {
        String subject = String.format("Daily P&L Report - %s", java.time.LocalDate.now());

        String body = String.format("""
                <html>
                <body>
                    <h2>Daily Trading Report</h2>
                    <p>Please find attached the daily P&L report.</p>
                    <h3>Summary:</h3>
                    <ul>
                        <li><strong>Date:</strong> %s</li>
                        <li><strong>Total Trades:</strong> %d</li>
                        <li><strong>Total P&L:</strong> ₹%.2f</li>
                    </ul>
                    <p>Best regards,<br/>Trading System</p>
                </body>
                </html>
                """,
                java.time.LocalDate.now(),
                tradeCount,
                totalPnl);

        sendReport(reportId, "DAILY_PNL", subject, body, filePath);
    }

    /**
     * Send trade journal report
     */
    public void sendTradeJournalReport(String reportId, String filePath,
                                      java.time.LocalDate startDate, java.time.LocalDate endDate) {
        String subject = String.format("Trade Journal - %s to %s", startDate, endDate);

        String body = String.format("""
                <html>
                <body>
                    <h2>Trade Journal Report</h2>
                    <p>Please find attached the trade journal report for the period:</p>
                    <ul>
                        <li><strong>From:</strong> %s</li>
                        <li><strong>To:</strong> %s</li>
                    </ul>
                    <p>Best regards,<br/>Trading System</p>
                </body>
                </html>
                """,
                startDate,
                endDate);

        sendReport(reportId, "TRADE_JOURNAL", subject, body, filePath);
    }

    /**
     * Send notification email
     */
    public void sendNotification(String subject, String body) {
        if (!emailEnabled || recipients == null || recipients.isEmpty()) {
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false);

            helper.setFrom(fromEmail);
            helper.setTo(recipients.toArray(new String[0]));
            helper.setSubject(subject);
            helper.setText(body, true);

            mailSender.send(message);

            log.info("Notification email sent: {}", subject);

        } catch (MessagingException e) {
            log.error("Failed to send notification email: {}", e.getMessage(), e);
        }
    }

    private void publishEmailedEvent(String reportId, String reportType, List<String> recipients,
                                     String subject, boolean success, String errorMessage) {
        ReportEmailedEvent event = ReportEmailedEvent.builder()
                .reportId(reportId)
                .reportType(reportType)
                .recipients(recipients)
                .subject(subject)
                .success(success)
                .errorMessage(errorMessage)
                .build();

        eventPublisher.publish(event);
    }
}
