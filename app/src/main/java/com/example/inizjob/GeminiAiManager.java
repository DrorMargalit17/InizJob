package com.example.inizjob;

import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.concurrent.Executors;

/*
 * Class: GeminiAiManager
 * Purpose: Service class to handle Google Gemini API requests.
 * * Methods:
 * 1. generateCvText - Sends user raw data to AI to create a professional CV document.
 */
public class GeminiAiManager {

    public GeminiAiManager() {
    }

    public void generateCvText(String name, String phone, String email, Cv cvData, AiCallback callback) {
        String apiKey = BuildConfig.GEMINI_API_KEY;

        GenerativeModel gm = new GenerativeModel("gemini-2.5-flash", apiKey);
        GenerativeModelFutures model = GenerativeModelFutures.from(gm);

        // Advanced prompt with professional category names
        // updated to be optimized for mobile screens and WhatsApp messaging
        String promptText = "You are an expert resume writer. Create a professional CV in HEBREW. " +
                "Take the provided raw data and expand it into beautifully crafted, official-sounding sentences. " +
                "Format the output as a clean text document optimized for mobile screens and WhatsApp messaging. " +
                "Use standard bullet points (•) for lists. Do NOT use markdown symbols like '**' or '#'.\n\n" +
                "Organize the document using these specific headers with emojis:\n" +
                "👤 פרטים אישיים\n" +
                "🎯 תמצית מקצועית\n" +
                "🎓 השכלה\n" +
                "💼 ניסיון תעסוקתי והתנדבות\n" +
                "💻 כישורים ומיומנויות\n" +
                "🏆 הישגים ויוזמות אישיות\n" +
                "🌟 מאפיינים וחוזקות\n" +
                "✨ פרטים נוספים\n\n" +
                "Candidate Details:\n" +
                "Name: " + name + "\nPhone: " + phone + "\nEmail: " + email + "\n" +
                "Summary: " + cvData.summary + "\n" +
                "Education: " + cvData.education + "\n" +
                "Experience: " + cvData.experience + "\n" +
                "Skills: " + cvData.skills + "\n" +
                "Achievements: " + cvData.achievements + "\n" +
                "Traits: " + cvData.traits + "\n" +
                "Unique Detail: " + cvData.uniqueDetail + "\n\n" +
                "Important: Do not invent facts, only enhance the phrasing. Write ONLY the CV content without introductory greetings.";

        Content content = new Content.Builder().addText(promptText).build();
        ListenableFuture<GenerateContentResponse> response = model.generateContent(content);

        Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
            @Override
            public void onSuccess(GenerateContentResponse result) {
                if (callback != null) {
                    callback.onSuccess(result.getText());
                }
            }

            @Override
            public void onFailure(Throwable t) {
                if (callback != null) {
                    callback.onFailure(t.getMessage());
                }
            }
        }, Executors.newSingleThreadExecutor());
    }
}