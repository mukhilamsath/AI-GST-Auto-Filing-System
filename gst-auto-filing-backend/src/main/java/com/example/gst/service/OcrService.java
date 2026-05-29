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
import java.io.File;
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

    // ── Optimized Regex Patterns for GST Invoice Fields ──────────────────────

    // Relaxed slightly to capture OCR confusion (e.g., reading 'Z' as '7')
    private static final Pattern GSTIN_PATTERN = Pattern.compile(
            "\\b([0-9]{2}[A-Z]{5}[0-9]{4}[A-Z][0-9A-Z][0-9A-Z][0-9A-Z])\\b"
    );

    // Streamlined to jump straight to the invoice alphanumeric sequence tracking value
    private static final Pattern INVOICE_NO_PATTERN = Pattern.compile(
            "(?:Invoice\\s*(?:No|Number|#)|INV)[:\\s-]*([A-Z0-9/\\-]+)",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern VENDOR_PATTERN = Pattern.compile(
            "(?:Vendor|Supplier|Seller|Billed\\s+By|From)[:\\s]+([^\\n\\r]{3,60})",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern DATE_PATTERN = Pattern.compile(
            "(?:Invoice\\s*Date|Date)[:\\s]*([0-9]{1,2}[/\\-][0-9]{1,2}[/\\-][0-9]{2,4}|[0-9]{4}[/\\-][0-9]{2}[/\\-][0-9]{2}|[0-9]{1,2}\\s+[A-Za-z]{3}\\s+[0-9]{4})",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern TAXABLE_PATTERN = Pattern.compile(
            "(?:Taxable\\s*(?:Value|Amount)|Sub\\s*Total|Base\\s*Amount)[:\\s]*[X₹]?\\s*([0-9,]+(?:\\.[0-9]{1,2})?)",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern CGST_PATTERN = Pattern.compile(
            "CGST[^\\n\\rX₹0-9]*[X₹]?\\s*([0-9,]+(?:\\.[0-9]{1,2})?)",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern SGST_PATTERN = Pattern.compile(
            "SGST[^\\n\\rX₹0-9]*[X₹]?\\s*([0-9,]+(?:\\.[0-9]{1,2})?)",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern IGST_PATTERN = Pattern.compile(
            "IGST[^\\n\\rX₹0-9]*[X₹]?\\s*([0-9,]+(?:\\.[0-9]{1,2})?)",
            Pattern.CASE_INSENSITIVE
    );

    // Made currency indicators fully optional to handle lines that lack symbols entirely
    private static final Pattern TOTAL_PATTERN = Pattern.compile(
            "(?:Grand\\s*Total|Total\\s*Amount|Amount\\s*Due|Total\\s*Payable|Net\\s*Amount)[:\\s]*[X₹]?\\s*([0-9,]+(?:\\.[0-9]{1,2})?)",
            Pattern.CASE_INSENSITIVE
    );

    // ── Main Entry Point ──────────────────────────────────────────────────────

    public Invoice extractInvoiceData(MultipartFile file) {
        String rawText = runOcr(file);
        log.info("=== OCR Raw Text ===\n{}", rawText);
        return parseInvoice(rawText, file.getOriginalFilename());
    }

    // ── OCR Page Loop Engine ──────────────────────────────────────────────────

    private String runOcr(MultipartFile file) {
        String filename = file.getOriginalFilename() != null
                ? file.getOriginalFilename().toLowerCase()
                : "";

        // JVM Safety Fallback Check: Prevents native Invalid Memory Access crashes
        File checkFile = new File(tessdataPath, ocrLanguage + ".traineddata");
        if (!checkFile.exists()) {
            log.error("CRITICAL: Tesseract data file missing at: {}", checkFile.getAbsolutePath());
            return "";
        }

        try {
            Tesseract tesseract = new Tesseract();
            tesseract.setDatapath(tessdataPath);
            tesseract.setLanguage(ocrLanguage);
            tesseract.setPageSegMode(6);
            tesseract.setOcrEngineMode(3);

            if (filename.endsWith(".pdf")) {
                StringBuilder fullDocumentText = new StringBuilder();
                byte[] bytes = file.getBytes();
                
                try (PDDocument document = Loader.loadPDF(bytes)) {
                    PDFRenderer renderer = new PDFRenderer(document);
                    int totalPages = document.getNumberOfPages();
                    
                    // Iterates through all pages so trailing total values are processed
                    for (int i = 0; i < totalPages; i++) {
                        BufferedImage pageImage = renderer.renderImageWithDPI(i, 300);
                        fullDocumentText.append(tesseract.doOCR(pageImage)).append("\n");
                    }
                }
                return fullDocumentText.toString();
            } else {
                BufferedImage image = ImageIO.read(file.getInputStream());
                if (image == null) {
                    log.warn("Could not decode image from file: {}", filename);
                    return "";
                }
                return tesseract.doOCR(image);
            }

        } catch (TesseractException e) {
            log.error("Tesseract OCR native error: {}", e.getMessage(), e);
            return "";
        } catch (IOException e) {
            log.error("File input read error during processing: {}", e.getMessage(), e);
            return "";
        }
    }

    // ── Structural Text Parsing ───────────────────────────────────────────────

    private Invoice parseInvoice(String text, String filename) {
        Invoice invoice = new Invoice();

        // Invoice Number Extraction
        String invoiceNumber = extractGroup(INVOICE_NO_PATTERN, text);
        if (invoiceNumber == null || invoiceNumber.isBlank()) {
            invoiceNumber = "OCR-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        }
        invoice.setInvoiceNumber(invoiceNumber.trim());

        // Vendor Name Extraction
        String vendor = extractGroup(VENDOR_PATTERN, text);
        invoice.setVendorName(vendor != null ? vendor.trim() : "Unknown Vendor");

        // GSTIN Extraction & Common Char Corrections
        String gstin = extractGroup(GSTIN_PATTERN, text);
        if (gstin != null) {
            gstin = gstin.trim().toUpperCase();
            if (gstin.length() == 15) {
                // If 14th character was read as '7' due to font weight issues, normalize it back to standard 'Z'
                if (gstin.charAt(13) == '7') {
                    gstin = gstin.substring(0, 13) + 'Z' + gstin.substring(14);
                }
            }
            invoice.setGstin(gstin);
        } else {
            invoice.setGstin("UNKNOWN");
        }

        // Invoice Date
        invoice.setInvoiceDate(parseDate(text));

        // Monetary Math Operations
        double taxable = extractAmount(TAXABLE_PATTERN, text);
        double cgst    = extractAmount(CGST_PATTERN, text);
        double sgst    = extractAmount(SGST_PATTERN, text);
        double igst    = extractAmount(IGST_PATTERN, text);
        double total   = extractAmount(TOTAL_PATTERN, text);

        // Taxable Back-calculation fallback logic
        if (taxable == 0.0 && total > 0.0) {
            double gstSum = cgst + sgst + igst;
            taxable = (gstSum > 0.0) ? Math.round((total - gstSum) * 100.0) / 100.0 : total;
        }

        // Default 18% Rule Application fallback logic
        if (taxable > 0.0 && cgst == 0.0 && sgst == 0.0 && igst == 0.0) {
            cgst = Math.round((taxable * 0.09) * 100.0) / 100.0;
            sgst = Math.round((taxable * 0.09) * 100.0) / 100.0;
        }

        // Final Aggregate Total Compilation fallback logic
        if (total == 0.0 && taxable > 0.0) {
            total = Math.round((taxable + cgst + sgst + igst) * 100.0) / 100.0;
        }

        invoice.setTaxableAmount(taxable);
        invoice.setCgst(cgst);
        invoice.setSgst(sgst);
        invoice.setIgst(igst);
        invoice.setTotalAmount(total);

        log.info("Successfully Parsed Invoice: number={}, vendor={}, gstin={}, total={}",
                invoice.getInvoiceNumber(), invoice.getVendorName(), invoice.getGstin(), total);

        return invoice;
    }

    // ── Regex & Cleansing Helpers ─────────────────────────────────────────────

    private String extractGroup(Pattern pattern, String text) {
        Matcher m = pattern.matcher(text);
        return m.find() ? m.group(1) : null;
    }

    private double extractAmount(Pattern pattern, String text) {
        String raw = extractGroup(pattern, text);
        if (raw == null) return 0.0;
        try {
            // Strips out everything except raw integers, periods, and commas to bypass 'X' vs '₹' bugs
            String sanitized = raw.replaceAll("[^0-9.,]", "").trim();
            return Double.parseDouble(sanitized.replace(",", ""));
        } catch (NumberFormatException e) {
            log.warn("Failed to parse numeric amount from raw text: '{}'", raw);
            return 0.0;
        }
    }

    private LocalDate parseDate(String text) {
        Matcher m = DATE_PATTERN.matcher(text);
        if (!m.find()) return LocalDate.now();

        String raw = m.group(1).trim();
        String[] formats = {
            "dd/MM/yyyy", "dd-MM-yyyy", "d/M/yyyy", "d-M-yyyy",
            "yyyy-MM-dd", "yyyy/MM/dd", "dd MMM yyyy", "d MMM yyyy"
        };

        for (String fmt : formats) {
            try {
                return LocalDate.parse(raw, DateTimeFormatter.ofPattern(fmt));
            } catch (DateTimeParseException ignored) {
                // Check next format configuration variant
            }
        }

        log.warn("Could not match format configurations for date text: '{}'", raw);
        return LocalDate.now();
    }
}