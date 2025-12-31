package com.canvaswrapped.ai;

import com.google.genai.Client;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import org.springframework.stereotype.Component;

@Component
public class GeminiGenAIClient implements GenAIClient{
    private final Client client;

    public GeminiGenAIClient(Client client) {
        this.client = client;
    }

    @Override
    public String generateText(String model, String prompt, GenerateContentConfig config){
        GenerateContentResponse resp = client.models.generateContent(model, prompt, config);
        return resp.text();
    }
}
