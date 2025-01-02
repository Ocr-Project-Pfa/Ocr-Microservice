package isme.pfaextract.Models;

import jakarta.persistence.*;

@Entity
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fileName;

    @Lob
    private String extractedData;

    private String lastname;
    private String firstname;
    private String dateOfBirth;
    private String bornIn;
    private String idCardNumber;

    private String uploadDate;
    private String processedDate;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getExtractedData() {
        return extractedData;
    }

    public void setExtractedData(String extractedData) {
        this.extractedData = extractedData;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getBornIn() {
        return bornIn;
    }

    public void setBornIn(String bornIn) {
        this.bornIn = bornIn;
    }

    public String getIdCardNumber() {
        return idCardNumber;
    }

    public void setIdCardNumber(String idCardNumber) {
        this.idCardNumber = idCardNumber;
    }

    public String getUploadDate() {
        return uploadDate;
    }

    public void setUploadDate(String uploadDate) {
        this.uploadDate = uploadDate;
    }

    public String getProcessedDate() {
        return processedDate;
    }

    public void setProcessedDate(String processedDate) {
        this.processedDate = processedDate;
    }
}

