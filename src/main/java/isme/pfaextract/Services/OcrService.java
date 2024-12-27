package isme.pfaextract.Services;

import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@Service
public class OcrService {

    private static final Logger log = LoggerFactory.getLogger(OcrService.class);

    public String extractText(MultipartFile file) throws IOException, TesseractException {
        // Set the TESSDATA_PREFIX environment variable
        System.setProperty("TESSDATA_PREFIX", "C:\\Program Files (x86)\\Tesseract-OCR\\");

        Tesseract tesseract = new Tesseract();
        tesseract.setDatapath("C:\\Program Files (x86)\\Tesseract-OCR\\tessdata"); // Update the path to Tesseract language data files
        tesseract.setLanguage("eng"); // Set the language to English
        log.info("Processing file: {}", file.getOriginalFilename());

        // Save the file temporarily for OCR processing
        String originalFileName = file.getOriginalFilename();
        String fileExtension = originalFileName.substring(originalFileName.lastIndexOf('.'));
        File tempFile = File.createTempFile("temp", fileExtension);
        file.transferTo(tempFile);

        log.info("Temporary file saved at: {}", tempFile.getAbsolutePath());

        // Perform OCR
        String extractedText = tesseract.doOCR(tempFile);

        log.info("Extracted text: {}", extractedText);

        // Clean up the temporary file
        if (tempFile.delete()) {
            log.info("Temporary file deleted successfully.");
        } else {
            log.warn("Failed to delete temporary file.");
        }

        return extractedText;
    }
}