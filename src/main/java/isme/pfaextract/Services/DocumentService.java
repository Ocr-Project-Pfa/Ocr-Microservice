package isme.pfaextract.Services;

import isme.pfaextract.Models.Document;
import isme.pfaextract.Repos.DocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

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
            String extractedText = ocrService.extractText(file);
            document.setExtractedData(extractedText);
            document.setStatus(Document.ProcessingStatus.COMPLETED);
        } catch (Exception e) {
            document.setStatus(Document.ProcessingStatus.FAILED);
            document.setErrorMessage(e.getMessage());
            throw e;
        }

        return documentRepository.save(document);
    }
}