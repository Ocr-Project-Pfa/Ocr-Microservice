package isme.pfaextract.Services;

import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.CLAHE;
import org.opencv.imgproc.Imgproc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.*;

import jakarta.annotation.PostConstruct;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import javax.imageio.ImageIO;
import java.awt.Color;
import lombok.extern.slf4j.Slf4j;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import isme.pfaextract.Models.IdCardField;
import isme.pfaextract.Models.MoroccanIdData;
import isme.pfaextract.utils.MoroccanDataConstants;

@Service
@Slf4j
public class OcrService {
    private static final Set<String> ALLOWED_EXTENSIONS = new HashSet<>(Arrays.asList("png", "jpg", "jpeg", "tiff", "pdf"));

    @Value("${tesseract.data.path}")
    private String tesseractDataPath;

    @Value("${training.data.path}")
    private String trainingDataPath;

    private Tesseract tesseract;


    @PostConstruct
    public void init() {
        try {
            tesseract = new Tesseract();
            tesseract.setDatapath("C:/Program Files/Tesseract-OCR/tessdata");

            // Set both languages with Arabic first for better detection
            tesseract.setLanguage("ara+fra");

            // Optimize settings for ID cards
            tesseract.setVariable("tessedit_char_whitelist",
                    "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789-/.' éèêëîïôöûüçÉÈÊËÎÏÔÖÛÜÇ" +
                            "ابتثجحخدذرزسشصضطظعغفقكلمنهويءؤئإأآة");
            tesseract.setVariable("preserve_interword_spaces", "1");
            tesseract.setVariable("textord_min_linesize", "1.5");
            tesseract.setVariable("user_defined_dpi", "300");

            // Additional optimizations for mixed text
            tesseract.setVariable("tessedit_pageseg_mode", "1");  // Automatic page segmentation with OSD
            tesseract.setVariable("tessedit_ocr_engine_mode", "3"); // Default + LSTM
            tesseract.setVariable("textord_heavy_nr", "1");  // More aggressive noise removal
            tesseract.setVariable("textord_force_make_prop_words", "1");
            tesseract.setVariable("edges_max_children_per_outline", "40");
            tesseract.setVariable("edges_children_per_grandchild", "10");

            loadTrainingData();
            log.info("Tesseract initialized with Moroccan ID optimizations");
        } catch (Exception e) {
            log.error("Failed to initialize Tesseract: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to initialize OCR", e);
        }
    }

    private void loadTrainingData() {
        try {
            File trainingDir = new File("training_data/letters");
            if (trainingDir.exists() && trainingDir.isDirectory()) {
                // Add training data path to Tesseract
                tesseract.setVariable("user_words_file", new File(trainingDir, "words.txt").getAbsolutePath());
                tesseract.setVariable("user_patterns_file", new File(trainingDir, "patterns.txt").getAbsolutePath());

                // Create words file from training data
                createWordsFile(trainingDir);

                log.info("Custom training data loaded successfully");
            }
        } catch (Exception e) {
            log.warn("Could not load training data: {}", e.getMessage());
        }
    }

    private void createWordsFile(File trainingDir) throws IOException {
        File wordsFile = new File(trainingDir, "words.txt");
        Set<String> words = new HashSet<>();

        // Add common words from training data
        for (File file : trainingDir.listFiles()) {
            if (file.getName().startsWith("word_")) {
                String[] parts = file.getName().split("_");
                if (parts.length > 2) {
                    words.add(parts[2].replace(".png", ""));
                }
            }
        }

        // Write words to file
        try (PrintWriter writer = new PrintWriter(wordsFile)) {
            for (String word : words) {
                writer.println(word);
            }
        }
    }

    public String extractText(MultipartFile file) throws IOException, TesseractException {
        validateFile(file);
        File tempFile = null;
        try {
            tempFile = createTempFile(file);
            BufferedImage image = ImageIO.read(tempFile);
            if (image == null) {
                throw new IOException("Could not read image file");
            }

            // Perform OCR with basic settings first
            tesseract.setLanguage("fra+ara");
            tesseract.setPageSegMode(3);
            String result = tesseract.doOCR(image);

            if (result != null && !result.trim().isEmpty()) {
                return cleanupResult(result);
            }

            // If basic OCR fails, try with enhanced preprocessing
            BufferedImage processedImage = preprocessImage(image);
            result = performOcrWithFallback(processedImage);

            return result != null ? cleanupResult(result) : "";
        } catch (Exception e) {
            log.error("OCR processing failed: {}", e.getMessage());
            throw new TesseractException("Failed to process image: " + e.getMessage());
        } finally {
            if (tempFile != null && tempFile.exists()) {
                try {
                    tempFile.delete();
                } catch (Exception e) {
                    log.warn("Failed to delete temp file: {}", e.getMessage());
                }
            }
        }
    }

    private BufferedImage preprocessImage(BufferedImage image) {
        try {
            // Convert BufferedImage to Mat
            Mat matImage = bufferedImageToMat(image);

            // Convert to grayscale
            Mat grayImage = new Mat();
            Imgproc.cvtColor(matImage, grayImage, Imgproc.COLOR_BGR2GRAY);

            // Apply Gaussian blur
            Imgproc.GaussianBlur(grayImage, grayImage, new Size(5, 5), 0);

            // Adaptive histogram equalization
            Mat equalizedImage = new Mat();
            CLAHE clahe = Imgproc.createCLAHE(2.0, new Size(8, 8));
            clahe.apply(grayImage, equalizedImage);

            // Adaptive thresholding
            Mat thresholdedImage = new Mat();
            Imgproc.adaptiveThreshold(
                    equalizedImage, thresholdedImage, 255,
                    Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C,
                    Imgproc.THRESH_BINARY, 11, 2);

            // Morphological operations to remove noise
            Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(3, 3));
            Imgproc.morphologyEx(thresholdedImage, thresholdedImage, Imgproc.MORPH_CLOSE, kernel);

            // Convert Mat back to BufferedImage
            return matToBufferedImage(thresholdedImage);
        } catch (Exception e) {
            log.error("Image preprocessing failed: {}", e.getMessage());
            throw new RuntimeException("Failed to preprocess image", e);
        }
    }
    private BufferedImage matToBufferedImage(Mat mat) {
        int type = mat.channels() > 1 ? BufferedImage.TYPE_3BYTE_BGR : BufferedImage.TYPE_BYTE_GRAY;
        BufferedImage image = new BufferedImage(mat.cols(), mat.rows(), type);
        byte[] data = new byte[mat.channels() * mat.cols() * mat.rows()];
        mat.get(0, 0, data);
        image.getRaster().setDataElements(0, 0, mat.cols(), mat.rows(), data);
        return image;
    }

    private Mat bufferedImageToMat(BufferedImage image) {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(((DataBufferByte) image.getRaster().getDataBuffer()).getData());
        return Imgcodecs.imdecode(new MatOfByte(inputStream.readAllBytes()), Imgcodecs.IMREAD_UNCHANGED);
    }

    private BufferedImage deskewImage(BufferedImage image) {
        // Calculate skew angle
        double angle = detectSkewAngle(image);

        // Create transform
        AffineTransform transform = AffineTransform.getRotateInstance(
                Math.toRadians(angle),
                image.getWidth() / 2.0,
                image.getHeight() / 2.0);

        // Create new image
        AffineTransformOp op = new AffineTransformOp(transform,
                AffineTransformOp.TYPE_BILINEAR);
        return op.filter(image, null);
    }
    private double detectSkewAngle(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        int[][] pixels = new int[width][height];

        // Convert to binary pixels
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                pixels[x][y] = (image.getRGB(x, y) & 0xFF) < 128 ? 1 : 0;
            }
        }

        // Detect lines using Hough transform
        double maxAngle = 20; // Max skew assumed
        double accuracy = 0.1; // Angle accuracy
        int[] histogram = new int[(int)(2 * maxAngle / accuracy)];

        // Simplified Hough transform for horizontal lines
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (pixels[x][y] == 1) {
                    for (double angle = -maxAngle; angle < maxAngle; angle += accuracy) {
                        double radian = Math.toRadians(angle);
                        double rho = x * Math.cos(radian) + y * Math.sin(radian);
                        int histIndex = (int)((angle + maxAngle) / accuracy);
                        histogram[histIndex]++;
                    }
                }
            }
        }

        // Find peak in histogram
        int maxIndex = 0;
        for (int i = 1; i < histogram.length; i++) {
            if (histogram[i] > histogram[maxIndex]) {
                maxIndex = i;
            }
        }

        return maxIndex * accuracy - maxAngle;
    }

    private BufferedImage adaptiveHistogramEqualization(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        BufferedImage result = new BufferedImage(width, height, image.getType());

        // Window size for local histogram
        int windowSize = 32;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                // Calculate local window boundaries
                int startX = Math.max(0, x - windowSize/2);
                int endX = Math.min(width, x + windowSize/2);
                int startY = Math.max(0, y - windowSize/2);
                int endY = Math.min(height, y + windowSize/2);

                // Calculate local histogram
                int[] histogram = new int[256];
                for (int wy = startY; wy < endY; wy++) {
                    for (int wx = startX; wx < endX; wx++) {
                        int gray = image.getRGB(wx, wy) & 0xFF;
                        histogram[gray]++;
                    }
                }

                // Calculate cumulative histogram
                int[] cdf = new int[256];
                cdf[0] = histogram[0];
                for (int i = 1; i < 256; i++) {
                    cdf[i] = cdf[i-1] + histogram[i];
                }

                // Normalize pixel
                int gray = image.getRGB(x, y) & 0xFF;
                int newGray = (int)(255.0 * cdf[gray] / cdf[255]);
                int rgb = (newGray << 16) | (newGray << 8) | newGray;
                result.setRGB(x, y, rgb);
            }
        }

        return result;
    }
    private BufferedImage applyMedianFilter(BufferedImage image, int windowSize) {
        int width = image.getWidth();
        int height = image.getHeight();
        BufferedImage result = new BufferedImage(width, height, image.getType());

        int[] window = new int[windowSize * windowSize];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                // Fill window
                int idx = 0;
                for (int wy = -windowSize/2; wy <= windowSize/2; wy++) {
                    for (int wx = -windowSize/2; wx <= windowSize/2; wx++) {
                        int px = Math.min(Math.max(x + wx, 0), width - 1);
                        int py = Math.min(Math.max(y + wy, 0), height - 1);
                        window[idx++] = image.getRGB(px, py) & 0xFF;
                    }
                }

                // Sort window to find median
                Arrays.sort(window);
                int median = window[window.length / 2];

                // Set result pixel
                int rgb = (median << 16) | (median << 8) | median;
                result.setRGB(x, y, rgb);
            }
        }

        return result;
    }


    private String performOcrWithFallback(BufferedImage image) throws TesseractException {
        // Try different PSM modes
        int[] psmModes = {4, 3, 6};
        String bestResult = "";
        int bestScore = 0;

        for (int psm : psmModes) {
            try {
                tesseract.setPageSegMode(psm);
                String result = tesseract.doOCR(image);
                int score = evaluateResult(result);
                if (score > bestScore) {
                    bestScore = score;
                    bestResult = result;
                }
            } catch (TesseractException e) {
                log.warn("OCR failed with PSM {}: {}", psm, e.getMessage());
            }
        }

        return bestResult;
    }

    private int evaluateResult(String result) {
        int score = 0;
        String[] keywords = {
                // French keywords
                "ROYAUME", "MAROC", "CARTE", "NATIONALE", "IDENTITE",
                "Né le", "à", "Valable jusqu'au",
                // Arabic keywords
                "المملكة", "المغربية", "البطاقة", "الوطنية", "للتعريف",
                "الاسم", "تاريخ", "الازدياد", "محل", "رقم"
        };

        for (String keyword : keywords) {
            if (result.contains(keyword)) {
                score += 10;
                // Give extra points for key identifiers
                if (keyword.equals("CARTE NATIONALE") ||
                        keyword.equals("البطاقة الوطنية") ||
                        keyword.equals("رقم")) {
                    score += 15;
                }
            }
        }

        // Check for number patterns (CIN format)
        if (result.matches(".*[A-Z]\\d{5,6}.*")) {
            score += 20;
        }

        return score;
    }

    private String cleanupResult(String text) {
        return text.replaceAll("\\s+", " ")           // Normalize spaces
                .replaceAll("[^\\p{L}\\p{N}\\s.,-]", "") // Remove special chars except basic punctuation
                .trim();
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is empty or null");
        }
        String extension = getFileExtension(file.getOriginalFilename());
        if (!ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
            throw new IllegalArgumentException("Invalid file type. Allowed types: " + ALLOWED_EXTENSIONS);
        }
    }

    private String getFileExtension(String filename) {
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
    }

    private File createTempFile(MultipartFile file) throws IOException {
        String originalFileName = file.getOriginalFilename();
        String fileExtension = getFileExtension(originalFileName);
        File tempFile = File.createTempFile("ocr_", "." + fileExtension);
        file.transferTo(tempFile);
        return tempFile;
    }

    public Map<String, String> extractIdCardData(MultipartFile file) throws IOException, TesseractException {
        validateFile(file);
        File tempFile = null;
        try {
            tempFile = createTempFile(file);
            BufferedImage image = ImageIO.read(tempFile);
            if (image == null) {
                throw new IOException("Could not read image file");
            }

            // First get the full text using the original working method
            String fullText = extractText(file);
            Map<String, String> extractedData = new HashMap<>();

            if (fullText != null && !fullText.trim().isEmpty()) {
                extractedData.put("FULL_TEXT", fullText);
                log.debug("Full text extracted: {}", fullText);
            }

            // Then try to extract specific fields
            BufferedImage processedImage = preprocessImage(image);
            for (IdCardField field : IdCardField.values()) {
                try {
                    Rectangle region = field.getRegion();
                    int x = (int) (processedImage.getWidth() * region.x / 100);
                    int y = (int) (processedImage.getHeight() * region.y / 100);
                    int w = (int) (processedImage.getWidth() * region.width / 100);
                    int h = (int) (processedImage.getHeight() * region.height / 100);

                    BufferedImage fieldImage = processedImage.getSubimage(x, y, w, h);
                    configureForField(field);
                    String fieldText = tesseract.doOCR(fieldImage).trim();

                    if (!fieldText.isEmpty()) {
                        extractedData.put(field.name(), fieldText);
                    }
                } catch (Exception e) {
                    log.error("Error processing field {}: {}", field.name(), e.getMessage());
                }
            }

            return extractedData;
        } finally {
            if (tempFile != null && tempFile.exists()) {
                tempFile.delete();
            }
        }
    }

    private void configureForField(IdCardField field) {
        switch (field) {
            case HEADER:
                tesseract.setLanguage("fra");
                tesseract.setPageSegMode(3);  // Fully automatic page segmentation
                break;
            case CIN:
                tesseract.setLanguage("fra");
                tesseract.setPageSegMode(7);  // Treat as single text line
                tesseract.setVariable("tessedit_char_whitelist", "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789");
                break;
            case FULL_NAME_FR:
            case POB_FR:
            case DOB_FR:
                tesseract.setLanguage("fra");
                tesseract.setPageSegMode(7);
                break;
            case FULL_NAME_AR:
            case POB_AR:
            case DOB_AR:
                tesseract.setLanguage("ara");
                tesseract.setPageSegMode(7);
                break;
        }

        // Common settings for better accuracy
        tesseract.setVariable("tessedit_do_invert", "0");
        tesseract.setVariable("preserve_interword_spaces", "1");
        tesseract.setVariable("textord_min_linesize", "1.5");
    }

    public MoroccanIdData parseIdCardText(String text) {
        MoroccanIdData data = new MoroccanIdData();
        data.setRawText(text);

        String[] lines = text.split("\n");

        for (String line : lines) {
            line = line.trim();

            // CIN number pattern
            if (line.matches(".*[A-Z]\\d{8}.*")) {
                String cin = line.replaceAll(".*?([A-Z]\\d{8}).*", "$1");
                data.setCin(cin);
            }

            // Name detection using common names database
            String[] words = line.split("\\s+");
            for (String word : words) {
                if (MoroccanDataConstants.COMMON_FIRST_NAMES.contains(word.toUpperCase())) {
                    StringBuilder fullName = new StringBuilder(word);
                    // Look for surname in next word
                    int idx = line.indexOf(word) + word.length();
                    String remaining = line.substring(idx).trim();
                    String[] remainingWords = remaining.split("\\s+");
                    if (remainingWords.length > 0 &&
                            MoroccanDataConstants.COMMON_LAST_NAMES.contains(remainingWords[0].toUpperCase())) {
                        fullName.append(" ").append(remainingWords[0]);
                    }
                    data.setFullNameFr(fullName.toString());
                    break;
                }
            }

            // City/Place detection
            for (String city : MoroccanDataConstants.CITIES) {
                if (line.toUpperCase().contains(city)) {
                    data.setPlaceOfBirth(city);
                    break;
                }
            }

            // Date patterns
            if (line.matches(".*\\d{2}[./]\\d{2}[./]\\d{4}.*")) {
                String date = line.replaceAll(".*?(\\d{2}[./]\\d{2}[./]\\d{4}).*", "$1");
                if (data.getDateOfBirth() == null) {
                    data.setDateOfBirth(date);
                } else {
                    data.setExpiryDate(date);
                }
            }
        }

        // Set defaults for missing values
        if (data.getCin() == null) data.setCin("Not found");
        if (data.getFullNameFr() == null) data.setFullNameFr("Not found");
        if (data.getDateOfBirth() == null) data.setDateOfBirth("Not found");
        if (data.getPlaceOfBirth() == null) data.setPlaceOfBirth("Not found");
        if (data.getExpiryDate() == null) data.setExpiryDate("Not found");

        return data;
    }
}