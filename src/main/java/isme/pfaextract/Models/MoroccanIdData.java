package isme.pfaextract.Models;

import lombok.Data;

@Data
public class MoroccanIdData {
    private String cin;           // e.g., "21016N"
    private String fullNameFr;    // e.g., "MOUHCINE"
    private String fullNameAr;    // e.g., "محسن"
    private String dateOfBirth;   // e.g., "29.11.1978"
    private String placeOfBirth;  // e.g., "TANGER ASSILAH - TANGER"
    private String expiryDate;    // e.g., "09.09.2029"
    private String rawText;       // The complete OCR output
}