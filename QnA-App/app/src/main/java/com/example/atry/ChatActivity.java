package com.example.atry;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.JsonReader;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ChatActivity extends Activity {

    private EditText messageEditText;
    private MessageAdapter messageAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        messageEditText = findViewById(R.id.messageEditText);
        Button sendButton = findViewById(R.id.sendButton);

        // Initialize RecyclerView and its adapter
        messageAdapter = new MessageAdapter();
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(messageAdapter);

        // Set click listener for the send button
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = messageEditText.getText().toString().trim();
                if (!message.isEmpty()) {
                    sendMessage(message);
                    messageEditText.getText().clear();
                }
            }
        });
    }

    private void sendMessage(String message) {
        // Create a new user message
        Message userMessage = new Message(message, true);
        messageAdapter.addMessage(userMessage);

        // Make API request and handle the response
        String appId = "W88PLG-6XLL8LY9AH"; // Replace with your actual Wolfram Alpha app ID
        String baseUrl = "https://api.wolframalpha.com/v2/query";
        String encodedMessage = Uri.encode(message, StandardCharsets.UTF_8.toString());
        String url = baseUrl + "?input=" + encodedMessage + "&format=plaintext&output=JSON&appid=" + appId;

        MyJsonTask jsonTask = new MyJsonTask();
        jsonTask.execute(url);
    }




    private void handleApiResponse(String jsonResponse) {
        try {
            JSONObject response = new JSONObject(jsonResponse);
            JSONObject queryResult = response.getJSONObject("queryresult");

            // Check if the API request was successful
            boolean success = queryResult.getBoolean("success");
            if (success) {
                JSONArray pods = queryResult.getJSONArray("pods");

                // Iterate over the pods and extract the desired information
                String inputInterpretation = null;
                String result = null;
                String basicInformation = null;
                for (int i = 0; i < pods.length(); i++) {
                    JSONObject pod = pods.getJSONObject(i);
                    String title = pod.getString("title");

                    // Check if the pod contains "Input interpretation", "Result", or "Basic information"
                    if (title.equals("Input interpretation")) {
                        JSONArray subpods = pod.getJSONArray("subpods");
                        JSONObject subpod = subpods.getJSONObject(0);
                        inputInterpretation = subpod.getString("plaintext");
                    } else if (title.equals("Result")) {
                        JSONArray subpods = pod.getJSONArray("subpods");
                        JSONObject subpod = subpods.getJSONObject(0);
                        result = subpod.getString("plaintext");
                    } else if (title.equals("Basic information")) {
                        JSONArray subpods = pod.getJSONArray("subpods");
                        JSONObject subpod = subpods.getJSONObject(0);
                        basicInformation = subpod.getString("plaintext");
                    }
                }

                // Display the input interpretation, result, and basic information if available
                if (inputInterpretation != null) {
                    handleMessageResponse(inputInterpretation);
                }
                if (result != null) {
                    handleMessageResponse(result);
                }
                if (basicInformation != null) {
                    handleMessageResponse(basicInformation);
                }

                // If no relevant pods were found, handle as invalid response
                if (inputInterpretation == null && result == null && basicInformation == null) {
                    handleInvalidResponse();
                }
            } else {
                // If the API request was not successful, handle as error response
                handleApiError(new Throwable("API Error"));
            }
        } catch (JSONException e) {
            // If there was an error parsing the JSON response, handle as error response
            handleApiError(new Throwable("JSON Parsing Error: " + e.getMessage()));
        }
    }



    private void handleMessageResponse(String message) {
        // Process the message response, e.g., display in the chat interface
        Message responseMessage = new Message(message, false);
        messageAdapter.addMessage(responseMessage);
    }

    private void handleInvalidResponse() {
        // Handle the case when the JSON response does not contain a relevant result
        Message errorMessage = new Message("Invalid response", false);
        messageAdapter.addMessage(errorMessage);
    }





    private void handleApiError(Throwable t) {
        // Display an error message in the chat interface
        String errorMessage = "API Error: " + t.getMessage();
        Message errorResponse = new Message(errorMessage, false);
        messageAdapter.addMessage(errorResponse);
    }

    @SuppressLint("StaticFieldLeak")
    private class MyJsonTask extends AsyncTask<String, Void, String> {

        private static final String TAG = "MyJsonTask";

        @Override
        protected String doInBackground(String... urls) {
            if (urls.length == 0) {
                return null;
            }

            String urlString = urls[0];
            HttpURLConnection urlConnection = null;
            JsonReader reader = null;
            String jsonResponse = "";

            try {
                URL url = new URL(urlString);
                urlConnection = (HttpURLConnection) url.openConnection();
                InputStream inputStream = urlConnection.getInputStream();

                // Read the JSON response from the input stream
                jsonResponse = readJsonResponse(inputStream);

                // Parse the JSON response
                reader = new JsonReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
                reader.setLenient(true); // Allow lenient parsing of malformed JSON
                // You can parse the JSON response here if needed

            } catch (IOException e) {
                Log.e(TAG, "Error reading JSON: " + e.getMessage());
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        Log.e(TAG, "Error closing JSON reader: " + e.getMessage());
                    }
                }
            }

            return jsonResponse;
        }

        @Override
        protected void onPostExecute(String jsonResponse) {
            if (jsonResponse != null) {
                handleApiResponse(jsonResponse);
            } else {
                handleApiError(new Throwable("Empty response"));
            }
        }

        private String readJsonResponse(InputStream inputStream) throws IOException {
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder stringBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
            }
            return stringBuilder.toString();
        }
    }
}
