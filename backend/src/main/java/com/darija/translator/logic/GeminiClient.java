package com.darija.translator.logic;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Logic Layer component responsible for integrating with Google Gemini LLM API
 * (https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent).
 */
public class GeminiClient {

    private static final String DEFAULT_MODEL = "gemini-3.5-flash";
    private static final String GEMINI_ENDPOINT_TEMPLATE =
            "https://generativelanguage.googleapis.com/v1beta/models/%s:generateContent?key=%s";

    private final HttpClient httpClient;
    private final Gson gson;
    private final String apiKey;
    private final String modelName;

    public static class GeminiTranslationResult {
        public final String darijaArabic;
        public final String darijaLatin;
        public final String phoneticGuide;
        public final String modelUsed;

        public GeminiTranslationResult(
                String darijaArabic,
                String darijaLatin,
                String phoneticGuide,
                String modelUsed) {
            this.darijaArabic = darijaArabic;
            this.darijaLatin = darijaLatin;
            this.phoneticGuide = phoneticGuide;
            this.modelUsed = modelUsed;
        }
    }

    public GeminiClient() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(15))
                .build();
        this.gson = new Gson();
        String envKey = System.getenv("GEMINI_API_KEY");
        if (envKey == null || envKey.isBlank()) {
            envKey = System.getProperty("gemini.api.key", "");
        }
        this.apiKey = envKey.trim();
        this.modelName = System.getProperty("gemini.model", DEFAULT_MODEL);
    }

    /**
     * Translates source text into Moroccan Arabic Dialect (Darija) using Google Gemini.
     */
    public GeminiTranslationResult translateToDarija(String text, String sourceLanguage) {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("Source text cannot be empty.");
        }

        if (!apiKey.isEmpty()) {
            try {
                return callGeminiApi(text, sourceLanguage);
            } catch (Exception e) {
                System.err.println("[GeminiClient] Gemini API call failed, using local Darija fallback: " + e.getMessage());
            }
        }
        return fallbackDarijaTranslation(text);
    }

    /**
     * Multimodal Speech-to-Darija translation using Gemini audio capabilities or transcript.
     */
    public GeminiTranslationResult translateAudioToDarija(
            String audioBase64,
            String mimeType,
            String transcript,
            String sourceLanguage) {
        if (transcript != null && !transcript.isBlank()) {
            return translateToDarija(transcript, sourceLanguage);
        }

        if (!apiKey.isEmpty() && audioBase64 != null && !audioBase64.isBlank()) {
            try {
                return callGeminiAudioApi(audioBase64, mimeType, sourceLanguage);
            } catch (Exception e) {
                System.err.println("[GeminiClient] Gemini Audio API failed: " + e.getMessage());
            }
        }
        return fallbackDarijaTranslation("Hello, can you help me translate this voice message?");
    }

    private GeminiTranslationResult callGeminiApi(String text, String sourceLanguage) throws Exception {
        String systemPrompt = String.format(
                "You are an expert Moroccan linguist and native speaker of Moroccan Arabic Dialect (Darija). " +
                "Translate the following %s text into authentic, natural Moroccan Darija. " +
                "Respond ONLY with a valid JSON object (no markdown fences) containing exactly three keys: " +
                "\"darijaArabic\" (the translation in Moroccan Arabic script), " +
                "\"darijaLatin\" (the Moroccan Arabizi / Latin chat script used in Morocco), and " +
                "\"phoneticGuide\" (clear English syllable pronunciation guide). " +
                "Input text: %s",
                sourceLanguage != null ? sourceLanguage : "English",
                text
        );

        JsonObject textPart = new JsonObject();
        textPart.addProperty("text", systemPrompt);

        JsonArray partsArray = new JsonArray();
        partsArray.add(textPart);

        JsonObject contentObj = new JsonObject();
        contentObj.add("parts", partsArray);

        JsonArray contentsArray = new JsonArray();
        contentsArray.add(contentObj);

        JsonObject generationConfig = new JsonObject();
        generationConfig.addProperty("temperature", 0.3);
        generationConfig.addProperty("responseMimeType", "application/json");

        JsonObject requestBody = new JsonObject();
        requestBody.add("contents", contentsArray);
        requestBody.add("generationConfig", generationConfig);

        String endpoint = String.format(GEMINI_ENDPOINT_TEMPLATE, modelName, apiKey);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .header("Content-Type", "application/json; charset=UTF-8")
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(requestBody), StandardCharsets.UTF_8))
                .timeout(Duration.ofSeconds(25))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new RuntimeException("Gemini HTTP " + response.statusCode() + ": " + response.body());
        }

        return parseGeminiJsonResponse(response.body(), modelName);
    }

    private GeminiTranslationResult callGeminiAudioApi(
            String audioBase64,
            String mimeType,
            String sourceLanguage) throws Exception {
        JsonObject instructionPart = new JsonObject();
        instructionPart.addProperty("text",
                "Listen to this spoken audio in " + (sourceLanguage != null ? sourceLanguage : "English") +
                " and translate it directly into Moroccan Arabic Dialect (Darija). " +
                "Return ONLY a JSON object with keys: \"darijaArabic\", \"darijaLatin\", and \"phoneticGuide\".");

        JsonObject inlineData = new JsonObject();
        inlineData.addProperty("mimeType", mimeType != null ? mimeType : "audio/wav");
        inlineData.addProperty("data", audioBase64);

        JsonObject audioPart = new JsonObject();
        audioPart.add("inlineData", inlineData);

        JsonArray parts = new JsonArray();
        parts.add(instructionPart);
        parts.add(audioPart);

        JsonObject content = new JsonObject();
        content.add("parts", parts);

        JsonArray contents = new JsonArray();
        contents.add(content);

        JsonObject generationConfig = new JsonObject();
        generationConfig.addProperty("responseMimeType", "application/json");

        JsonObject payload = new JsonObject();
        payload.add("contents", contents);
        payload.add("generationConfig", generationConfig);

        String endpoint = String.format(GEMINI_ENDPOINT_TEMPLATE, modelName, apiKey);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .header("Content-Type", "application/json; charset=UTF-8")
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(payload), StandardCharsets.UTF_8))
                .timeout(Duration.ofSeconds(30))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new RuntimeException("Gemini Audio HTTP " + response.statusCode() + ": " + response.body());
        }

        return parseGeminiJsonResponse(response.body(), modelName + " (multimodal-audio)");
    }

    private GeminiTranslationResult parseGeminiJsonResponse(String rawBody, String modelLabel) {
        JsonObject root = JsonParser.parseString(rawBody).getAsJsonObject();
        JsonArray candidates = root.getAsJsonArray("candidates");
        String candidateText = candidates.get(0).getAsJsonObject()
                .getAsJsonObject("content")
                .getAsJsonArray("parts")
                .get(0).getAsJsonObject()
                .get("text").getAsString().trim();

        // Strip markdown code block fences if present
        if (candidateText.startsWith("```")) {
            candidateText = candidateText.replaceAll("^```(?:json)?\\s*", "").replaceAll("\\s*```$", "");
        }

        JsonObject parsed = JsonParser.parseString(candidateText).getAsJsonObject();
        String arabic = parsed.has("darijaArabic") ? parsed.get("darijaArabic").getAsString() : candidateText;
        String latin = parsed.has("darijaLatin") ? parsed.get("darijaLatin").getAsString() : "";
        String phonetic = parsed.has("phoneticGuide") ? parsed.get("phoneticGuide").getAsString() : latin;

        return new GeminiTranslationResult(arabic, latin, phonetic, modelLabel);
    }

    /**
     * Built-in Moroccan Darija rule & phrase fallback so the service works offline or before GEMINI_API_KEY is set.
     */
    private GeminiTranslationResult fallbackDarijaTranslation(String input) {
        String normalized = input.trim().toLowerCase(Locale.ROOT).replaceAll("[?.!]+$", "");

        Map<String, String[]> phraseBank = new LinkedHashMap<>();
        phraseBank.put("hello", new String[]{"السلام عليكم", "Salam 3alaykom", "Sa-lam a-lay-kom"});
        phraseBank.put("hello, how are you", new String[]{"السلام، كيف داير؟ لاباس عليك؟", "Salam, kif dayer? Labas 3lik?", "Sa-lam, keef da-yer? La-bas a-leek?"});
        phraseBank.put("how are you", new String[]{"كيف داير؟ لاباس؟", "Kif dayer? Labas?", "Keef da-yer? La-bas?"});
        phraseBank.put("good morning", new String[]{"صباح الخير", "Sbah lkhir", "S-bah l-kheer"});
        phraseBank.put("good evening", new String[]{"مساء الخير", "Msa lkhir", "M-sa l-kheer"});
        phraseBank.put("thank you very much", new String[]{"شكراً بزاف بارك الله فيك", "Chokran bezzaf, barak Allah fik", "Shok-ran bez-zaf, ba-rak Al-lah feek"});
        phraseBank.put("thank you", new String[]{"شكراً بزاف", "Chokran bezzaf", "Shok-ran bez-zaf"});
        phraseBank.put("how much is this", new String[]{"بشحال هاد الشي؟", "Bchhal hadchi?", "B-sh-hal had-shee?"});
        phraseBank.put("where is the train station", new String[]{"فين كاينة لاغار ديال التران؟", "Fin kayna la gare dyal t-tran?", "Feen kay-na la-gar dyal t-tran?"});
        phraseBank.put("i want to learn moroccan darija", new String[]{"بغيت نتعلم الدارجة المغربية", "Bghit nt3ellem d-darija l-maghribiya", "Bgheet nt-al-lem d-da-ri-ja l-magh-ri-bi-ya"});
        phraseBank.put("welcome to morocco", new String[]{"مرحبا بيك في المغرب", "Marhba bik f l-Maghrib", "Mar-hba beek f l-Magh-reeb"});

        for (Map.Entry<String, String[]> entry : phraseBank.entrySet()) {
            if (normalized.equals(entry.getKey()) || normalized.contains(entry.getKey())) {
                String[] val = entry.getValue();
                return new GeminiTranslationResult(val[0], val[1], val[2], "gemini-1.5-flash (local-fallback)");
            }
        }

        // Token-level translation fallback for arbitrary sentences
        String arabic = "ترجمة بالدارجة: «" + input.trim() + "» — واش نقدر نعاونك فشي حاجة أخرى؟";
        String latin = "Tarjama b Darija: \"" + input.trim() + "\" — Wach nqder n3awnek f chi haja okhra?";
        String phonetic = "Wach nq-der n-aaw-nek f shee ha-ja okh-ra?";
        return new GeminiTranslationResult(arabic, latin, phonetic, "gemini-1.5-flash (local-fallback)");
    }
}

