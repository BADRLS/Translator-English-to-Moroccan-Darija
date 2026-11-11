package com.darija.translator.persistence;

import java.time.Instant;
import java.util.UUID;

/**
 * Persistence Entity representing a historical translation transaction.
 */
public class TranslationRecord {
    private final String id;
    private final String username;
    private final String sourceLanguage;
    private final String originalText;
    private final String darijaArabic;
    private final String darijaLatin;
    private final String phoneticGuide;
    private final String modelUsed;
    private final String createdAt;

    public TranslationRecord(
            String username,
            String sourceLanguage,
            String originalText,
            String darijaArabic,
            String darijaLatin,
            String phoneticGuide,
            String modelUsed) {
        this.id = UUID.randomUUID().toString();
        this.username = username != null ? username : "anonymous";
        this.sourceLanguage = sourceLanguage != null ? sourceLanguage : "English";
        this.originalText = originalText;
        this.darijaArabic = darijaArabic;
        this.darijaLatin = darijaLatin;
        this.phoneticGuide = phoneticGuide;
        this.modelUsed = modelUsed;
        this.createdAt = Instant.now().toString();
    }

    public String getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getSourceLanguage() {
        return sourceLanguage;
    }

    public String getOriginalText() {
        return originalText;
    }

    public String getDarijaArabic() {
        return darijaArabic;
    }

    public String getDarijaLatin() {
        return darijaLatin;
    }

    public String getPhoneticGuide() {
        return phoneticGuide;
    }

    public String getModelUsed() {
        return modelUsed;
    }

    public String getCreatedAt() {
        return createdAt;
    }
}

