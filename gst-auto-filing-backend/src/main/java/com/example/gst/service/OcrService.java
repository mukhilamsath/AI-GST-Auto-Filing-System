package com.example.gst.service;

import com.example.gst.entity.Invoice;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class OcrService {

    private static final Logger log = LoggerFactory.getLogger(OcrService.class);

    @Value("${ocr.tessdata.path}")
    private String tessdataPath;

    @Value("${ocr.language}")
    private String ocrLanguage;

    // ── Regex Patterns for GST Invoice fields ─────────────────────────────────

    // GSTIN: 15-character format, e.g. 33ABCDE1234F1Z5
    private static final Pattern GSTIN_PATTERN = Pattern.compile(
            "\\b([0-9]{2}[A-Z]{5}[0-9]{4}[A-Z][1-9A-Z]Z[0-9A-Z])\\b"
    );

    // Invoice number: INV-XXXX, GST/XXXX/YY, various formats
    private static final Pattern INVOICE_NO_PATTERN = Pattern.compile(
            "(?:Invoice\\s*(?:No|Number|#)[:\\s.]*|INV[-\\s]*)([A-Z0-9/\\-]+)",
            Pattern.CASE_INSENSITIVE
    );

    // Vendor / Supplier / Seller name on a labeled line
    private static final Pattern VENDOR_PATTERN = Pattern.compile(
            "(?:Vendor|Supplier|Seller|Billed\\s+By|From)[:\\s]+([^\\n\\r]{3,60})",
            Pattern.CASE_INSENSITIVE
    );

    // Date formats: dd/MM/yyyy, dd-MM-yyyy, yyyy-MM-dd, dd MMM yyyy
    private static final Pattern DATE_PATTERN = Pattern.compile(
            "(?:Invoice\\s*Date|Date)[:\\s]*([0-9]{1,2}[/\\-][0-9]{1,2}[/\\-][0-9]{2,4}|[0-9]{4}[/\\-][0-9]{2}[/\\-][0-9]{2}|[0-9]{1,2}\\s+[A-Za-z]{3}\\s+[0-9]{4})",
            Pattern.CASE_INSENSITIVE
    );

    // Monetary amounts (commas allowed), labelled
    private static final Pattern TAXABLE_PATTERN = Pattern.compile(
            "(?:Taxable\\s*(?:Value|Amount)|Sub\\s*Total|Base\\s*Amount)[:\\s]*₹?\\s*([0-9,]+(?:\\.[0-9]{1,2})?)",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern CGST_PATTERN = Pattern.compile(
            "CGST[^\\n\\r₹0-9]*₹?\\s*([0-9,]+(?:\\.[0-9]{1,2})?)",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern SGST_PATTERN = Pattern.compile(
            "SGST[^\\n\\r₹0-9]*₹?\\s*([0-9,]+(?:\\.[0-9]{1,2})?)",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern IGST_PATTERN = Pattern.compile(
            "IGST[^\\n\\r₹0-9]*₹?\\s*([0-9,]+(?:\\.[0-9]{1,2})?)",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern TOTAL_PATTERN = Pattern.compile(
            "(?:Grand\\s*Total|Total\\s*Amount|Amount\\s*Due|Total\\s*Payable|Net\\s*Amount)[:\\s]*₹?\\s*([0-9,]+(?:\\.[0-9]{1,2})?)",
            Pattern.CASE_INSENSITIVE
    );

    // ── Main entry point ──────────────────────────────────────────────────────

    public Invoice extractInvoiceData(MultipartFile file) {
        String rawText = runOcr(file);
        log.info("=== OCR Raw Text ===\n{}", rawText);
        return parseInvoice(rawText, file.getOriginalFilename());
    }

    // ── OCR execution ─────────────────────────────────────────────────────────

    private String runOcr(MultipartFile file) {
        String filename = file.getOriginalFilename() != null
                ? file.getOriginalFilename().toLowerCase()
                : "";

        try {
            BufferedImage image;

            if (filename.endsWith(".pdf")) {
                image = pdfToImage(file);
            } else {
                // PNG, JPG, JPEG, TIFF, BMP, GIF — direct decode
                image = ImageIO.read(file.getInputStream());
            }

            if (image == null) {
                log.warn("Could not decode image from file: {}", filename);
                return "";
            }

            Tesseract tesseract = new Tesseract();
            tesseract.setDatapath(tessdataPath);
            tesseract.setLanguage(ocrLanguage);
            // PSM 6 = Assume a single uniform block of text
            tesseract.setPageSegMode(6);
            // OEM 3 = Default, based on what is available
            tesseract.setOcrEngineMode(3);

            return tesseract.doOCR(image);

        } catch (TesseractException e) {
            log.error("Tesseract OCR failed: {}", e.getMessage(), e);
            return "";
        } catch (IOException e) {
            log.error("File read error during OCR: {}", e.getMessage(), e);
            return "";
        }
    }

    private BufferedImage pdfToImage(MultipartFile file) throws IOException {
        // PDFBox 3.x: use Loader.loadPDF(byte[]) instead of PDDocument.load(InputStream)
        byte[] bytes = file.getBytes();
        try (PDDocument document = Loader.loadPDF(bytes)) {
            PDFRenderer renderer = new PDFRenderer(document);
            // Render the first page at 300 DPI for best OCR accuracy
            return renderer.renderImageWithDPI(0, 300);
        }
    }

    // ── Text Parsing ──────────────────────────────────────────────────────────

    private Invoice parseInvoice(String text, String filename) {
        Invoice invoice = new Invoice();

        // Invoice Number
        String invoiceNumber = extractGroup(INVOICE_NO_PATTERN, text);
        if (invoiceNumber == null || invoiceNumber.isBlank()) {
            invoiceNumber = "OCR-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        }
        invoice.setInvoiceNumber(invoiceNumber.trim());

        // Vendor Name
        String vendor = extractGroup(VENDOR_PATTERN, text);
        invoice.setVendorName(vendor != null ? vendor.trim() : "Unknown Vendor");

        // GSTIN
        String gstin = extractGroup(GSTIN_PATTERN, text);
        invoice.setGstin(gstin != null ? gstin.trim() : "UNKNOWN");

        // Invoice Date
        invoice.setInvoiceDate(parseDate(text));

        // Monetary fields
        double taxable = extractAmount(TAXABLE_PATTERN, text);
        double cgst    = extractAmount(CGST_PATTERN, text);
        double sgst    = extractAmount(SGST_PATTERN, text);
        double igst    = extractAmount(IGST_PATTERN, text);
        double total   = extractAmount(TOTAL_PATTERN, text);

        // If taxable amount not found but total is present, attempt back-calculation
        if (taxable == 0.0 && total > 0.0) {
            double gstSum = cgst + sgst + igst;
            taxable = (gstSum > 0.0) ? Math.round((total - gstSum) * 100.0) / 100.0 : total;
        }

        // If GST amounts not found but taxable is present, apply standard 18% (9+9 CGST/SGST)
        if (taxable > 0.0 && cgst == 0.0 && sgst == 0.0 && igst == 0.0) {
            cgst = Math.round((taxable * 0.09) * 100.0) / 100.0;
            sgst = Math.round((taxable * 0.09) * 100.0) / 100.0;
        }

        // If total not found, calculate it
        if (total == 0.0 && taxable > 0.0) {
            total = Math.round((taxable + cgst + sgst + igst) * 100.0) / 100.0;
        }

        invoice.setTaxableAmount(taxable);
        invoice.setCgst(cgst);
        invoice.setSgst(sgst);
        invoice.setIgst(igst);
        invoice.setTotalAmount(total);

        log.info("Parsed Invoice: number={}, vendor={}, gstin={}, taxable={}, cgst={}, sgst={}, igst={}, total={}",
                invoice.getInvoiceNumber(), invoice.getVendorName(), invoice.getGstin(),
                taxable, cgst, sgst, igst, total);

        return invoice;
    }

    // ── Regex Helpers ─────────────────────────────────────────────────────────

    private String extractGroup(Pattern pattern, String text) {
        Matcher m = pattern.matcher(text);
        return m.find() ? m.group(1) : null;
    }

    private double extractAmount(Pattern pattern, String text) {
        String raw = extractGroup(pattern, text);
        if (raw == null) return 0.0;
        try {
            // Remove commas used as thousands separators
            return Double.parseDouble(raw.replace(",", "").trim());
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    private LocalDate parseDate(String text) {
        Matcher m = DATE_PATTERN.matcher(text);
        if (!m.find()) return LocalDate.now();

        String raw = m.group(1).trim();

        // Try common date formats in order
        String[] formats = {
            "dd/MM/yyyy", "dd-MM-yyyy", "d/M/yyyy", "d-M-yyyy",
            "yyyy-MM-dd", "yyyy/MM/dd",
            "dd MMM yyyy", "d MMM yyyy"
        };

        for (String fmt : formats) {
            try {
                return LocalDate.parse(raw, DateTimeFormatter.ofPattern(fmt));
            } catch (DateTimeParseException ignored) {
                // Try next format
            }
        }

        log.warn("Could not parse date: '{}'", raw);
        return LocalDate.now();
    }
}
