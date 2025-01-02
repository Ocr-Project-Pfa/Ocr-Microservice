package isme.pfaextract.Controllers;

import isme.pfaextract.Models.Document;
import isme.pfaextract.Repos.DocumentRepository;
import isme.pfaextract.Services.OcrService;
import net.sourceforge.tess4j.TesseractException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
@RequestMapping("/api/documents")
public class DocumentController {

    private final DocumentRepository documentRepository;
    private final OcrService ocrService;

    public DocumentController(DocumentRepository documentRepository, OcrService ocrService) {
        this.documentRepository = documentRepository;
        this.ocrService = ocrService;
    }

    @PostMapping("/upload")
    public ResponseEntity<Document> uploadDocument(@RequestParam("file") MultipartFile file) {
        try {
            Document document = ocrService.extractData(file);
            document.setUploadDate(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
            documentRepository.save(document);

            return ResponseEntity.ok(document);
        } catch (IOException | TesseractException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
