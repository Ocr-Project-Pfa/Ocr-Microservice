package isme.pfaextract.Services;

import isme.pfaextract.Models.Document;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class OcrService {

    private static final Logger log = LoggerFactory.getLogger(OcrService.class);

    public Document extractData(MultipartFile file) throws IOException, TesseractException {
        System.setProperty("TESSDATA_PREFIX", "C:\\Program Files (x86)\\Tesseract-OCR\\");

        Tesseract tesseract = new Tesseract();
        tesseract.setDatapath("C:\\Program Files (x86)\\Tesseract-OCR\\tessdata");
        tesseract.setLanguage("fra"); // Use only French for better accuracy

        // Save temporary file for OCR processing
        File tempFile = File.createTempFile("temp", file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf('.')));
        file.transferTo(tempFile);

        String extractedText = tesseract.doOCR(tempFile);

        if (tempFile.delete()) {
            log.info("Temporary file deleted successfully.");
        }

        log.info("Extracted OCR Text: \n{}", extractedText);

        return parseFields(cleanExtractedText(extractedText), file.getOriginalFilename());
    }

    private String cleanExtractedText(String text) {
        // Remove unwanted characters and noise
        return text.replaceAll("[^A-Za-zÀ-ÿ0-9\\s/,-]", "") // Keep only valid characters
                .replaceAll("\\s{2,}", " ") // Collapse multiple spaces
                .trim();
    }

    private Document parseFields(String text, String fileName) {
        Document document = new Document();

        // Set file name and raw extracted text
        document.setFileName(fileName);
        document.setExtractedData(text);

        // Extract lastname between "DD" and "tHp"
        String lastnameRegex = "DD\\s+(.*?)\\s+tHp";
        Matcher lastnameMatcher = Pattern.compile(lastnameRegex).matcher(text);
        if (lastnameMatcher.find()) {
            document.setLastname(lastnameMatcher.group(1).trim());
        } else {
            log.warn("Lastname not found.");
        }

        // Extract firstname between "t" and "- 55N"
        String firstnameRegex = "t\\s+(.*?)\\s+- 55N";
        Matcher firstnameMatcher = Pattern.compile(firstnameRegex).matcher(text);
        if (firstnameMatcher.find()) {
            document.setFirstname(firstnameMatcher.group(1).trim());
        } else {
            log.warn("Firstname not found.");
        }

        // Match date of birth (DDMMYYYY format)
        String dobRegex = "\\b(\\d{8})\\b";
        Matcher dobMatcher = Pattern.compile(dobRegex).matcher(text);
        if (dobMatcher.find()) {
            String dob = dobMatcher.group(1);
            document.setDateOfBirth(dob.substring(0, 2) + "/" + dob.substring(2, 4) + "/" + dob.substring(4));
        } else {
            log.warn("Date of Birth not found.");
        }

        // Extract ID card number
        String idCardRegex = "\\b([A-Z]{2}\\d{6})\\b";
        Matcher idCardMatcher = Pattern.compile(idCardRegex).matcher(text);
        if (idCardMatcher.find()) {
            document.setIdCardNumber(idCardMatcher.group(1));
        } else {
            log.warn("ID Card Number not found.");
        }

        // Extract place of birth (general pattern for capitalized words)
        String bornInRegex = "TN\\s+([A-Za-zÀ-ÿ]+)";
        Matcher bornInMatcher = Pattern.compile(bornInRegex).matcher(text);
        if (bornInMatcher.find()) {
            document.setBornIn(bornInMatcher.group(1).trim());
        } else {
            log.warn("Born In not found.");
        }

        return document;
    }
}
