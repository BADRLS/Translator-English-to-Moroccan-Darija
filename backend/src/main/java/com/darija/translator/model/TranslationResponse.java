package com.darija.translator.model;

/**
 * Structured response returned by TranslatorResource.translate().
 */
public class TranslationResponse {
    private String id;
    private String originalText;
    private String sourceLanguage;
    private String darijaArabic;
    private String darijaLatin;
    private String phoneticGuide;
    private String modelUsed;
    private String timestamp;

    public TranslationResponse() {
    }

    public TranslationResponse(
            String id,
            String originalText,
            String sourceLanguage,
            String darijaArabic,
            String darijaLatin,
            String phoneticGuide,
            String modelUsed,
            String timestamp) {
        this.id = id;
        this.originalText = originalText;
        this.sourceLanguage = sourceLanguage;
        this.darijaArabic = darijaArabic;
        this.darijaLatin = darijaLatin;
        this.phoneticGuide = phoneticGuide;
        this.modelUsed = modelUsed;
        this.timestamp = timestamp;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOriginalText() {
        return originalText;
    }

    public void setOriginalText(String originalText) {
        this.originalText = originalText;
    }

    public String getSourceLanguage() {
        return sourceLanguage;
    }

    public void setSourceLanguage(String sourceLanguage) {
        this.sourceLanguage = sourceLanguage;
    }

    public String getDarijaArabic() {
        return darijaArabic;
    }

    public void setDarijaArabic(String darijaArabic) {
        this.darijaArabic = darijaArabic;
    }

    public String getDarijaLatin() {
        return darijaLatin;
    }

    public void setDarijaLatin(String darijaLatin) {
        this.darijaLatin = darijaLatin;
    }

    public String getPhoneticGuide() {
        return phoneticGuide;
    }

    public void setPhoneticGuide(String phoneticGuide) {
        this.phoneticGuide = phoneticGuide;
    }

    public String getModelUsed() {
        return modelUsed;
    }

    public void setModelUsed(String modelUsed) {
        this.modelUsed = modelUsed;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}

