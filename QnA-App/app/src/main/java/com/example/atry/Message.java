package com.example.atry;

public class Message {
    private final String content;
    private final boolean userMessage;

    public Message(String content, boolean userMessage) {
        this.content = content;
        this.userMessage = userMessage;
    }

    public String getContent() {
        return content;
    }

    public boolean isUserMessage() {
        return userMessage;
    }
}
