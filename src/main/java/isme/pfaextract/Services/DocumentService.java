package isme.pfaextract.Services;

import isme.pfaextract.Models.Document;
import isme.pfaextract.Repos.DocumentRepository;
import isme.pfaextract.Services.OcrService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class DocumentService {
    private final DocumentRepository documentRepository;
    private final OcrService ocrService;

    public Document processDocument(MultipartFile file) throws Exception {
        Document document = new Document();
        document.setFileName(file.getOriginalFilename());
        document.setStatus(Document.ProcessingStatus.PROCESSING);
        document = documentRepository.save(document);

        try {
            // Perform OCR to extract raw text
            String extractedText = ocrService.extractText(file);
            document.setExtractedData(extractedText);

            // Map specific fields from the extracted text
            Map<String, String> mappedFields = ocrService.mapExtractedData(extractedText);
            document.setFirstname(mappedFields.getOrDefault("firstname", ""));
            document.setLastname(mappedFields.getOrDefault("lastname", ""));
            document.setBornAt(mappedFields.getOrDefault("bornAt", ""));
            document.setBornIn(mappedFields.getOrDefault("bornIn", ""));
            document.setIdCardNumber(mappedFields.getOrDefault("IdCardNumber", ""));

            document.setStatus(Document.ProcessingStatus.COMPLETED);
        } catch (Exception e) {
            document.setStatus(Document.ProcessingStatus.FAILED);
            document.setErrorMessage(e.getMessage());
            throw e;
        }

        return documentRepository.save(document);
    }
}
