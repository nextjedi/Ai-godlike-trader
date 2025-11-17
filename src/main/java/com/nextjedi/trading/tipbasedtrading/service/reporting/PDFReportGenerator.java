package com.nextjedi.trading.tipbasedtrading.service.reporting;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.nextjedi.trading.tipbasedtrading.models.TradeModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Service for generating PDF reports
 */
@Service
@Slf4j
public class PDFReportGenerator {

    @Value("${trading.reporting.output-directory:./reports}")
    private String outputDirectory;

    private static final Font TITLE_FONT = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD);
    private static final Font HEADER_FONT = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);
    private static final Font NORMAL_FONT = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL);

    /**
     * Generate daily P&L report
     */
    public String generateDailyPnLReport(LocalDate date, List<TradeModel> trades,
                                        Double totalPnl, Double winRate) {
        try {
            // Ensure output directory exists
            Path dirPath = Paths.get(outputDirectory);
            if (!Files.exists(dirPath)) {
                Files.createDirectories(dirPath);
            }

            // Create filename
            String filename = String.format("Daily_PnL_%s.pdf",
                    date.format(DateTimeFormatter.ISO_DATE));
            String filePath = Paths.get(outputDirectory, filename).toString();

            // Create document
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, new FileOutputStream(filePath));

            document.open();

            // Title
            Paragraph title = new Paragraph("Daily P&L Report", TITLE_FONT);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);

            // Date
            Paragraph dateP = new Paragraph("Date: " + date.format(DateTimeFormatter.ofPattern("dd MMM yyyy")),
                    HEADER_FONT);
            dateP.setSpacingAfter(20);
            document.add(dateP);

            // Summary section
            document.add(new Paragraph("Summary", HEADER_FONT));
            document.add(Chunk.NEWLINE);

            PdfPTable summaryTable = new PdfPTable(2);
            summaryTable.setWidthPercentage(100);
            summaryTable.setSpacingAfter(20);

            addSummaryRow(summaryTable, "Total Trades", String.valueOf(trades.size()));
            addSummaryRow(summaryTable, "Total P&L", String.format("₹%.2f", totalPnl));
            addSummaryRow(summaryTable, "Win Rate", String.format("%.2f%%", winRate));

            long winningTrades = trades.stream()
                    .filter(t -> t.getPnl() != null && t.getPnl() > 0)
                    .count();
            long losingTrades = trades.stream()
                    .filter(t -> t.getPnl() != null && t.getPnl() < 0)
                    .count();

            addSummaryRow(summaryTable, "Winning Trades", String.valueOf(winningTrades));
            addSummaryRow(summaryTable, "Losing Trades", String.valueOf(losingTrades));

            document.add(summaryTable);

            // Trade details
            if (!trades.isEmpty()) {
                document.add(new Paragraph("Trade Details", HEADER_FONT));
                document.add(Chunk.NEWLINE);

                PdfPTable tradesTable = new PdfPTable(7);
                tradesTable.setWidthPercentage(100);
                tradesTable.setWidths(new float[]{2f, 1.5f, 1.5f, 1.5f, 1f, 1.5f, 1f});

                // Headers
                addTableHeader(tradesTable, "Symbol");
                addTableHeader(tradesTable, "Entry Price");
                addTableHeader(tradesTable, "Exit Price");
                addTableHeader(tradesTable, "Quantity");
                addTableHeader(tradesTable, "Type");
                addTableHeader(tradesTable, "P&L");
                addTableHeader(tradesTable, "Status");

                // Rows
                for (TradeModel trade : trades) {
                    addTableCell(tradesTable, trade.getInstrumentWrapper().getTradingSymbol());
                    addTableCell(tradesTable, trade.getEntryOrder() != null ?
                            String.format("%.2f", trade.getEntryOrder().getPrice()) : "-");
                    addTableCell(tradesTable, trade.getExitOrder() != null ?
                            String.format("%.2f", trade.getExitOrder().getPrice()) : "-");
                    addTableCell(tradesTable, trade.getEntryOrder() != null ?
                            String.valueOf(trade.getEntryOrder().getQuantity()) : "-");
                    addTableCell(tradesTable, trade.getType().name());
                    addTableCell(tradesTable, trade.getPnl() != null ?
                            String.format("₹%.2f", trade.getPnl()) : "-");
                    addTableCell(tradesTable, trade.getTradeStatus().name());
                }

                document.add(tradesTable);
            }

            document.close();

            log.info("Generated daily P&L report: {}", filePath);
            return filePath;

        } catch (DocumentException | IOException e) {
            log.error("Failed to generate PDF report: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate PDF report", e);
        }
    }

    /**
     * Generate trade journal report
     */
    public String generateTradeJournalReport(LocalDate startDate, LocalDate endDate,
                                            List<TradeModel> trades) {
        try {
            Path dirPath = Paths.get(outputDirectory);
            if (!Files.exists(dirPath)) {
                Files.createDirectories(dirPath);
            }

            String filename = String.format("Trade_Journal_%s_to_%s.pdf",
                    startDate.format(DateTimeFormatter.ISO_DATE),
                    endDate.format(DateTimeFormatter.ISO_DATE));
            String filePath = Paths.get(outputDirectory, filename).toString();

            Document document = new Document(PageSize.A4.rotate()); // Landscape
            PdfWriter.getInstance(document, new FileOutputStream(filePath));

            document.open();

            // Title
            Paragraph title = new Paragraph("Trade Journal", TITLE_FONT);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);

            // Period
            Paragraph period = new Paragraph(
                    String.format("Period: %s to %s",
                            startDate.format(DateTimeFormatter.ofPattern("dd MMM yyyy")),
                            endDate.format(DateTimeFormatter.ofPattern("dd MMM yyyy"))),
                    HEADER_FONT);
            period.setSpacingAfter(20);
            document.add(period);

            // Trade table
            if (!trades.isEmpty()) {
                PdfPTable table = new PdfPTable(10);
                table.setWidthPercentage(100);
                table.setWidths(new float[]{1.5f, 2f, 1.5f, 1.5f, 1.5f, 1f, 1.5f, 1.5f, 1f, 1.5f});

                // Headers
                addTableHeader(table, "Date");
                addTableHeader(table, "Symbol");
                addTableHeader(table, "Type");
                addTableHeader(table, "Entry");
                addTableHeader(table, "Exit");
                addTableHeader(table, "Qty");
                addTableHeader(table, "SL");
                addTableHeader(table, "Target");
                addTableHeader(table, "P&L");
                addTableHeader(table, "Status");

                // Rows
                for (TradeModel trade : trades) {
                    addTableCell(table, trade.getCreatedAt().toString().substring(0, 10));
                    addTableCell(table, trade.getInstrumentWrapper().getTradingSymbol());
                    addTableCell(table, trade.getType().name());
                    addTableCell(table, trade.getEntryOrder() != null ?
                            String.format("%.2f", trade.getEntryOrder().getPrice()) : "-");
                    addTableCell(table, trade.getExitOrder() != null ?
                            String.format("%.2f", trade.getExitOrder().getPrice()) : "-");
                    addTableCell(table, trade.getEntryOrder() != null ?
                            String.valueOf(trade.getEntryOrder().getQuantity()) : "-");
                    addTableCell(table, String.format("%.2f", trade.getStopLoss()));
                    addTableCell(table, trade.getTarget() != null ?
                            String.format("%.2f", trade.getTarget()) : "-");
                    addTableCell(table, trade.getPnl() != null ?
                            String.format("₹%.2f", trade.getPnl()) : "-");
                    addTableCell(table, trade.getTradeStatus().name());
                }

                document.add(table);
            }

            document.close();

            log.info("Generated trade journal report: {}", filePath);
            return filePath;

        } catch (DocumentException | IOException e) {
            log.error("Failed to generate trade journal report: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate trade journal report", e);
        }
    }

    // Helper methods

    private void addSummaryRow(PdfPTable table, String label, String value) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, HEADER_FONT));
        labelCell.setBorder(Rectangle.NO_BORDER);
        labelCell.setPadding(5);
        table.addCell(labelCell);

        PdfPCell valueCell = new PdfPCell(new Phrase(value, NORMAL_FONT));
        valueCell.setBorder(Rectangle.NO_BORDER);
        valueCell.setPadding(5);
        table.addCell(valueCell);
    }

    private void addTableHeader(PdfPTable table, String header) {
        PdfPCell cell = new PdfPCell(new Phrase(header, HEADER_FONT));
        cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(5);
        table.addCell(cell);
    }

    private void addTableCell(PdfPTable table, String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, NORMAL_FONT));
        cell.setPadding(5);
        table.addCell(cell);
    }
}
