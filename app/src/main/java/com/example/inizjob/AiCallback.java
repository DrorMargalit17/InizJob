package com.example.inizjob;

/*
 * Interface: AiCallback
 * Purpose: Handles asynchronous responses from the AI Manager back to the UI.
 * * Methods:
 * 1. onSuccess - Called when the AI successfully generates the text.
 * 2. onFailure - Called when an error occurs during the AI generation process.
 */
public interface AiCallback {
    void onSuccess(String result);
    void onFailure(String errorMessage);
}