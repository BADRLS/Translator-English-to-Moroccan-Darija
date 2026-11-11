package com.darija.translator.model;

/**
 * Response model for Read-Aloud (Text-to-Speech) extension.
 * Provides SSML markup, phonetic pronunciation, and speech synthesis metadata.
 */
public class TtsResponse {
    private String text;
    private String voiceLocale;
    private String phoneticPronunciation;
    private String ssml;
    private double recommendedRate;
    private double recommendedPitch;

    public TtsResponse() {
    }

    public TtsResponse(
            String text,
            String voiceLocale,
            String phoneticPronunciation,
            String ssml,
            double recommendedRate,
            double recommendedPitch) {
        this.text = text;
        this.voiceLocale = voiceLocale;
        this.phoneticPronunciation = phoneticPronunciation;
        this.ssml = ssml;
        this.recommendedRate = recommendedRate;
        this.recommendedPitch = recommendedPitch;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getVoiceLocale() {
        return voiceLocale;
    }

    public void setVoiceLocale(String voiceLocale) {
        this.voiceLocale = voiceLocale;
    }

    public String getPhoneticPronunciation() {
        return phoneticPronunciation;
    }

    public void setPhoneticPronunciation(String phoneticPronunciation) {
        this.phoneticPronunciation = phoneticPronunciation;
    }

    public String getSsml() {
        return ssml;
    }

    public void setSsml(String ssml) {
        this.ssml = ssml;
    }

    public double getRecommendedRate() {
        return recommendedRate;
    }

    public void setRecommendedRate(double recommendedRate) {
        this.recommendedRate = recommendedRate;
    }

    public double getRecommendedPitch() {
        return recommendedPitch;
    }

    public void setRecommendedPitch(double recommendedPitch) {
        this.recommendedPitch = recommendedPitch;
    }
}

