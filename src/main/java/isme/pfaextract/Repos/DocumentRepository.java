package isme.pfaextract.Repos;


import isme.pfaextract.Models.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {
    List<Document> findByStatus(Document.ProcessingStatus status);
    List<Document> findByUploadDateBetween(LocalDateTime start, LocalDateTime end);
    List<Document> findByFileNameContaining(String fileName);
    long countByStatus(Document.ProcessingStatus status);
}
