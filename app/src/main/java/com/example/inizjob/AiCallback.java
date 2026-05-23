package com.example.inizjob;

/*
 * Interface: AiCallback
 * Purpose: Handles asynchronous responses from the AI Manager back to the UI.
 */

public interface AiCallback {
    // Callback method when the AI successfully generates the text
    void onSuccess(String result);
    // Callback method when an error occurs during the AI generation process
    void onFailure(String errorMessage);
}