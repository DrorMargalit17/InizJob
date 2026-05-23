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
        //empty constructor required
    }

    /**This method sends user raw data to AI to create a professional CV document. according to this steps:
     * 1. Create a GenerativeModel instance with the provided API key.
     * 2. Create a Content object with the prompt text.
     * 3. Use the GenerativeModel to generate content based on the prompt.
     * 4. Set up a callback to handle the response.
     * 5. Execute the callback when the response is received.
     * */

    public void generateCvText(String name, String phone, String email, Cv cvData, AiCallback callback) {
        // Set up the Gemini API key
        String apiKey = BuildConfig.GEMINI_API_KEY;

        // Create a GenerativeModel instance
        GenerativeModel gm = new GenerativeModel("gemini-2.5-flash", apiKey);
        GenerativeModelFutures model = GenerativeModelFutures.from(gm);

        /** Advanced prompt with professional category names */
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

        /*create a Content object with the prompt text*/
        Content content = new Content.Builder().addText(promptText).build();

        // Use the GenerativeModel to generate content based on the prompt
        ListenableFuture<GenerateContentResponse> response = model.generateContent(content);

        // Handle the response when it's received
        Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
            @Override
            //If the request is successful, pass the generated text to the callback
            public void onSuccess(GenerateContentResponse result) {
                if (callback != null) {
                    callback.onSuccess(result.getText());
                }
            }

            @Override
            // Handle any errors that occur during the request
            public void onFailure(Throwable t) {
                if (callback != null) {
                    // Pass the error message to the callback
                    callback.onFailure(t.getMessage());
                }
            }
            //Execute the thread when the response is received
        }, Executors.newSingleThreadExecutor());
    }
}