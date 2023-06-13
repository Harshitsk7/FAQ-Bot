package com.example.atry;


import android.util.Log;

public class handleApiResponse {
    private handleApiResponse(String jsonResponse) {
        // Log the JSON response
        Log.d("JSON Response", jsonResponse);

        // Create a new message to display the JSON response
        Message responseMessage = new Message(jsonResponse, false);
        MessageAdapter messageAdapter = null;
        messageAdapter.addMessage(responseMessage);
    }
}