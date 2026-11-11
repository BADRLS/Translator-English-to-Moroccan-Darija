package com.darija.translator.presentation;

import com.darija.translator.logic.TranslatorService;
import com.darija.translator.model.SpeechTranslationRequest;
import com.darija.translator.model.TranslationRequest;
import com.darija.translator.model.TranslationResponse;
import com.darija.translator.model.TtsResponse;
import com.darija.translator.persistence.TranslationRecord;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;

import java.util.List;
import java.util.Map;

/**
 * Presentation Layer: Jakarta RESTful Web Services Endpoint (TranslatorResource).
 * Exposes the core business method translate() along with speech-to-speech and TTS extensions.
 *
 * Base URI: http://localhost:8080/api/translator
 */
@Path("/translator")
@Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
@Consumes(MediaType.APPLICATION_JSON)
public class TranslatorResource {

    private final TranslatorService translatorService;

    public TranslatorResource() {
        this.translatorService = new TranslatorService();
    }

    /**
     * Core required business method: translate() via HTTP POST.
     * Accepts a JSON payload with {"text": "...", "sourceLanguage": "English"}
     * and returns the Moroccan Darija translation.
     */
    @POST
    @Path("/translate")
    public Response translate(TranslationRequest request, @Context SecurityContext securityContext) {
        try {
            String username = extractUsername(securityContext);
            TranslationResponse response = translatorService.translateText(request, username);
            return Response.ok(response).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "Bad Request", "message", e.getMessage()))
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Translation Failed", "message", e.getMessage()))
                    .build();
        }
    }

    /**
     * Convenience HTTP GET overload of translate() accepting ?text=... as query parameter.
     * Example: GET /api/translator/translate?text=Hello%20friend
     */
    @GET
    @Path("/translate")
    public Response translate(
            @QueryParam("text") String text,
            @QueryParam("sourceLanguage") @DefaultValue("English") String sourceLanguage,
            @Context SecurityContext securityContext) {
        TranslationRequest request = new TranslationRequest(text, sourceLanguage, true);
        return translate(request, securityContext);
    }

    /**
     * Task 3 Extension: Speech-to-Darija (Voice-to-Voice translation) endpoint.
     * POST /api/translator/speech-to-darija
     */
    @POST
    @Path("/speech-to-darija")
    public Response translateSpeech(SpeechTranslationRequest request, @Context SecurityContext securityContext) {
        try {
            String username = extractUsername(securityContext);
            TranslationResponse response = translatorService.translateSpeech(request, username);
            return Response.ok(response).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "Bad Request", "message", e.getMessage()))
                    .build();
        }
    }

    /**
     * Task 3 Extension: Read-Aloud (Text-to-Speech SSML & pronunciation generator).
     * POST /api/translator/tts
     */
    @POST
    @Path("/tts")
    public Response synthesizeReadAloud(Map<String, String> payload) {
        try {
            String darijaArabic = payload != null ? payload.get("darijaArabic") : null;
            String darijaLatin = payload != null ? payload.get("darijaLatin") : null;
            TtsResponse ttsResponse = translatorService.synthesizeReadAloud(darijaArabic, darijaLatin);
            return Response.ok(ttsResponse).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "Bad Request", "message", e.getMessage()))
                    .build();
        }
    }

    /**
     * Persistence Layer Inspection Endpoint: Returns logged translations.
     * GET /api/translator/history
     */
    @GET
    @Path("/history")
    public Response getHistory() {
        List<TranslationRecord> history = translatorService.getTranslationHistory();
        return Response.ok(history).build();
    }

    private String extractUsername(SecurityContext securityContext) {
        if (securityContext != null && securityContext.getUserPrincipal() != null) {
            return securityContext.getUserPrincipal().getName();
        }
        return "admin";
    }
}

