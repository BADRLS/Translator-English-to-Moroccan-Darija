package com.darija.translator.model;

/**
 * Request model for Speech-to-Darija (Voice-to-Voice) extension.
 * Accepts either a spoken transcript or base64-encoded audio bytes.
 */
public class SpeechTranslationRequest {
    private String transcript;
    private String audioBase64;
    private String mimeType = "audio/wav";
    private String sourceLanguage = "English";

    public SpeechTranslationRequest() {
    }

    public String getTranscript() {
        return transcript;
    }

    public void setTranscript(String transcript) {
        this.transcript = transcript;
    }

    public String getAudioBase64() {
        return audioBase64;
    }

    public void setAudioBase64(String audioBase64) {
        this.audioBase64 = audioBase64;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public String getSourceLanguage() {
        return sourceLanguage;
    }

    public void setSourceLanguage(String sourceLanguage) {
        this.sourceLanguage = sourceLanguage;
    }
}

