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
        String promptText = "You are an expert resume writer. Create a professional CV in HEBREW. " +
                "Enhance the wording to sound official and impressive, but strictly follow the facts provided. " +
                "Use these exact Hebrew headers in the document:\n" +
                "1. תמצית מקצועית\n2. השכלה\n3. ניסיון תעסוקתי והתנדבות\n4. כישורים ומיומנויות טכניות\n" +
                "5. הישגים ויוזמות אישיות\n6. מאפיינים וחוזקות\n7. פרטים נוספים וייחוד אישי\n\n" +
                "Candidate Details:\n" +
                "Name: " + name + ", Phone: " + phone + ", Email: " + email + "\n" +
                "Objective Data: " + cvData.summary + "\n" +
                "Education Data: " + cvData.education + "\n" +
                "Experience Data: " + cvData.experience + "\n" +
                "Skills Data: " + cvData.skills + "\n" +
                "Achievements Data: " + cvData.achievements + "\n" +
                "Traits Data: " + cvData.traits + "\n" +
                "Unique Detail: " + cvData.uniqueDetail + "\n\n" +
                "Formatting: Professional, clean, no Markdown asterisks.";

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