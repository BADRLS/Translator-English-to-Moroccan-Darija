package com.darija.translator.model;

/**
 * Request payload for text translation into Moroccan Darija.
 */
public class TranslationRequest {
    private String text;
    private String sourceLanguage = "English";
    private boolean includeArabizi = true;

    public TranslationRequest() {
    }

    public TranslationRequest(String text, String sourceLanguage, boolean includeArabizi) {
        this.text = text;
        this.sourceLanguage = sourceLanguage != null ? sourceLanguage : "English";
        this.includeArabizi = includeArabizi;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getSourceLanguage() {
        return sourceLanguage;
    }

    public void setSourceLanguage(String sourceLanguage) {
        this.sourceLanguage = sourceLanguage;
    }

    public boolean isIncludeArabizi() {
        return includeArabizi;
    }

    public void setIncludeArabizi(boolean includeArabizi) {
        this.includeArabizi = includeArabizi;
    }
}

