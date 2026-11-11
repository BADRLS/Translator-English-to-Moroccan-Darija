package com.darija.translator.logic;

import com.darija.translator.model.SpeechTranslationRequest;
import com.darija.translator.model.TranslationResponse;
import com.darija.translator.model.TtsResponse;
import com.darija.translator.persistence.TranslationDao;
import com.darija.translator.persistence.TranslationRecord;

/**
 * Task 3 (Extensions): Modular Business Logic for Voice-to-Voice (Speech Translation)
 * and Read-Aloud (Text-to-Speech) synthesis in Moroccan Darija.
 */
public class SpeechExtensionService {

    private final GeminiClient geminiClient;
    private final TranslationDao translationDao;

    public SpeechExtensionService(GeminiClient geminiClient, TranslationDao translationDao) {
        this.geminiClient = geminiClient;
        this.translationDao = translationDao;
    }

    /**
     * Voice-to-Voice / Speech Translation pipeline:
     * Accepts spoken audio (Base64) or a voice transcript, translates to Darija via Gemini,
     * persists the transaction, and returns the translation ready for voice playback.
     */
    public TranslationResponse translateSpeech(SpeechTranslationRequest request, String username) {
        if (request == null || ((request.getTranscript() == null || request.getTranscript().isBlank())
                && (request.getAudioBase64() == null || request.getAudioBase64().isBlank()))) {
            throw new IllegalArgumentException("Either 'transcript' or 'audioBase64' must be provided for speech translation.");
        }

        GeminiClient.GeminiTranslationResult result = geminiClient.translateAudioToDarija(
                request.getAudioBase64(),
                request.getMimeType(),
                request.getTranscript(),
                request.getSourceLanguage()
        );

        String originalLabel = (request.getTranscript() != null && !request.getTranscript().isBlank())
                ? request.getTranscript()
                : "[Voice Audio Clip (" + request.getMimeType() + ")]";

        TranslationRecord saved = translationDao.save(new TranslationRecord(
                username,
                request.getSourceLanguage(),
                originalLabel,
                result.darijaArabic,
                result.darijaLatin,
                result.phoneticGuide,
                result.modelUsed
        ));

        return new TranslationResponse(
                saved.getId(),
                saved.getOriginalText(),
                saved.getSourceLanguage(),
                saved.getDarijaArabic(),
                saved.getDarijaLatin(),
                saved.getPhoneticGuide(),
                saved.getModelUsed(),
                saved.getCreatedAt()
        );
    }

    /**
     * Read-Aloud (Text-to-Speech) generator:
     * Builds W3C SSML (Speech Synthesis Markup Language) and ar-MA voice configuration
     * so clients can read Darija translations aloud accurately.
     */
    public TtsResponse generateReadAloudMetadata(String darijaArabic, String darijaLatin) {
        if ((darijaArabic == null || darijaArabic.isBlank()) && (darijaLatin == null || darijaLatin.isBlank())) {
            throw new IllegalArgumentException("Text to synthesize cannot be empty.");
        }

        String primaryText = (darijaArabic != null && !darijaArabic.isBlank()) ? darijaArabic : darijaLatin;
        String phonetic = (darijaLatin != null && !darijaLatin.isBlank()) ? darijaLatin : primaryText;

        String ssml = String.format(
                "<speak version=\"1.1\" xmlns=\"http://www.w3.org/2001/10/synthesis\" xml:lang=\"ar-MA\">" +
                "<voice name=\"ar-MA-Standard-A\"><prosody rate=\"0.92\" pitch=\"+0st\">%s</prosody></voice>" +
                "</speak>",
                escapeXml(primaryText)
        );

        return new TtsResponse(
                primaryText,
                "ar-MA",
                phonetic,
                ssml,
                0.92,
                1.0
        );
    }

    private String escapeXml(String input) {
        return input.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }
}

