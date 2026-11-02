package com.acme.salary.payslip;

import com.acme.salary.entity.Employee;
import com.acme.salary.entity.Payment;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Builds the payslip PDF for a payment.
 * <p>
 * What the payslip prints is decided in buildPayslip(). Change the lines there freely:
 * heading, subheading, line, row (label on the left, value on the right), gap and rule
 * are the building blocks.
 * <p>
 * The built-in PDF fonts only cover basic Latin characters. Anything else, such as the
 * rupee sign or non-Latin names, is printed as '?'. That is why amounts show the currency
 * code (INR) and not a symbol. To print other scripts, load a TrueType font with
 * PDType0Font.load(...) instead.
 */
@Component
public class PayslipPdfGenerator {

    public byte[] generate(Employee employee, Payment payment) {
        try (PDDocument document = new PDDocument();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            try (PDPageContentStream content = new PDPageContentStream(document, page)) {
                buildPayslip(new PageWriter(content, PDRectangle.A4), employee, payment);
            }

            document.save(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException(
                    "Could not generate the payslip PDF for payment " + payment.getPaymentID(), e);
        }
    }

    // ---------------------------------------------------------------------------------
    // Payslip layout. Edit this method to change what the PDF contains.
    // ---------------------------------------------------------------------------------
    private void buildPayslip(PageWriter w, Employee employee, Payment payment) throws IOException {
        String currency = payment.getCurrency();

        w.heading("PAYSLIP");
        w.line("Payslip no: " + payment.getPaymentID());
        w.line("Pay period: " + payment.getPeriodStart() + " to " + payment.getPeriodEnd());
        w.line("Paid on: " + payment.getPaidDate() + " at " + payment.getPaidTime());
        w.gap(8);
        w.rule();

        w.subheading("Employee");
        w.row("Name", employee.getFirstName() + " " + employee.getLastName());
        w.row("Employee code", employee.getEmployeeCode());
        w.row("Department", employee.getDepartment());
        w.row("Job title", employee.getJobTitle());
        w.gap(8);
        w.rule();

        w.subheading("Earnings");
        w.row("Basic salary", money(currency, payment.getBasicSalary()));
        w.row("HRA", money(currency, payment.getHra()));
        w.row("DA", money(currency, payment.getDa()));
        w.row("Gross salary", money(currency, payment.getGrossSalary()), true);
        w.gap(8);
        w.rule();

        w.subheading("Deductions");
        w.row("Tax", money(currency, payment.getTax()));
        w.row("EPF", money(currency, payment.getEpf()));
        w.row("Unpaid leave (" + payment.getUnpaidLeaveDays() + " days)", money(currency, payment.getLeaveDeduction()));
        w.gap(8);
        w.rule();

        w.row("Net pay", money(currency, payment.getNetSalary()), true);
    }

    private static String money(String currency, BigDecimal amount) {
        return currency + " " + amount.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    /**
     * Writes top-to-bottom on a single page and keeps track of the current vertical position.
     */
    private static final class PageWriter {

        private static final float MARGIN = 50f;
        private static final float TEXT_SIZE = 11f;

        private final PDPageContentStream content;
        private final PDType1Font regular = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
        private final PDType1Font bold = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
        private final float rightEdge;
        private float y;

        PageWriter(PDPageContentStream content, PDRectangle pageSize) {
            this.content = content;
            this.rightEdge = pageSize.getWidth() - MARGIN;
            this.y = pageSize.getHeight() - MARGIN;
        }

        void heading(String text) throws IOException {
            draw(bold, 20f, MARGIN, text);
            y -= 30f;
        }

        void subheading(String text) throws IOException {
            draw(bold, 12f, MARGIN, text);
            y -= 20f;
        }

        void line(String text) throws IOException {
            draw(regular, TEXT_SIZE, MARGIN, text);
            y -= 17f;
        }

        void row(String label, String value) throws IOException {
            row(label, value, false);
        }

        void row(String label, String value, boolean emphasised) throws IOException {
            PDType1Font font = emphasised ? bold : regular;
            String cleanValue = clean(value);
            float valueWidth = font.getStringWidth(cleanValue) / 1000f * TEXT_SIZE;
            draw(font, TEXT_SIZE, MARGIN, label);
            draw(font, TEXT_SIZE, rightEdge - valueWidth, cleanValue);
            y -= 17f;
        }

        void gap(float height) {
            y -= height;
        }

        void rule() throws IOException {
            content.moveTo(MARGIN, y);
            content.lineTo(rightEdge, y);
            content.stroke();
            y -= 15f;
        }

        private void draw(PDType1Font font, float size, float x, String text) throws IOException {
            content.beginText();
            content.setFont(font, size);
            content.newLineAtOffset(x, y);
            content.showText(clean(text));
            content.endText();
        }

        private static String clean(String text) {
            if (text == null) {
                return "";
            }
            StringBuilder cleaned = new StringBuilder(text.length());
            for (char c : text.toCharArray()) {
                boolean supported = (c >= 32 && c <= 126) || (c >= 160 && c <= 255);
                cleaned.append(supported ? c : '?');
            }
            return cleaned.toString();
        }
    }
}
