package com.darija.translator.logic;

import com.darija.translator.model.SpeechTranslationRequest;
import com.darija.translator.model.TranslationRequest;
import com.darija.translator.model.TranslationResponse;
import com.darija.translator.model.TtsResponse;
import com.darija.translator.persistence.InMemoryTranslationDao;
import com.darija.translator.persistence.TranslationDao;
import com.darija.translator.persistence.TranslationRecord;

import java.util.List;

/**
 * Core Business Logic Service in the 3-Tier Architecture.
 * Mediates between the Presentation Layer (TranslatorResource) and Persistence Layer (TranslationDao),
 * while delegating LLM inference to GeminiClient and audio/TTS features to SpeechExtensionService.
 */
public class TranslatorService {

    private final TranslationDao translationDao;
    private final GeminiClient geminiClient;
    private final SpeechExtensionService speechExtensionService;

    public TranslatorService() {
        this.translationDao = InMemoryTranslationDao.getInstance();
        this.geminiClient = new GeminiClient();
        this.speechExtensionService = new SpeechExtensionService(this.geminiClient, this.translationDao);
    }

    /**
     * Core business operation: translates source text into Moroccan Darija and logs the request.
     */
    public TranslationResponse translateText(TranslationRequest request, String authenticatedUser) {
        if (request == null || request.getText() == null || request.getText().isBlank()) {
            throw new IllegalArgumentException("Parameter 'text' is required for translation.");
        }

        GeminiClient.GeminiTranslationResult llmResult = geminiClient.translateToDarija(
                request.getText(),
                request.getSourceLanguage()
        );

        TranslationRecord record = new TranslationRecord(
                authenticatedUser,
                request.getSourceLanguage(),
                request.getText(),
                llmResult.darijaArabic,
                llmResult.darijaLatin,
                llmResult.phoneticGuide,
                llmResult.modelUsed
        );
        translationDao.save(record);

        return new TranslationResponse(
                record.getId(),
                record.getOriginalText(),
                record.getSourceLanguage(),
                record.getDarijaArabic(),
                record.getDarijaLatin(),
                record.getPhoneticGuide(),
                record.getModelUsed(),
                record.getCreatedAt()
        );
    }

    /**
     * Delegates voice-to-voice translation to the modular SpeechExtensionService.
     */
    public TranslationResponse translateSpeech(SpeechTranslationRequest request, String authenticatedUser) {
        return speechExtensionService.translateSpeech(request, authenticatedUser);
    }

    /**
     * Generates Read-Aloud (TTS) payload and SSML metadata for Moroccan Darija speech output.
     */
    public TtsResponse synthesizeReadAloud(String darijaArabic, String darijaLatin) {
        return speechExtensionService.generateReadAloudMetadata(darijaArabic, darijaLatin);
    }

    /**
     * Retrieves logged translation history from the Persistence Tier.
     */
    public List<TranslationRecord> getTranslationHistory() {
        return translationDao.findAll();
    }
}

