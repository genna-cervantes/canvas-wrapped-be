package com.canvaswrapped.service;

import com.canvaswrapped.ai.GenAIClient;
import com.canvaswrapped.model.WrappedResponse;
import com.google.genai.types.GenerateContentConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

@Service
public class GeminiService {
    private static final Logger log = LoggerFactory.getLogger(GeminiService.class);
    private final GenAIClient client;
    private final GenerateContentConfig config;
    private static final int MAX_ATTEMPTS = 3;

    public GeminiService(GenAIClient client, GenerateContentConfig config) {
        this.client = client;
        this.config = config;
    }

    public String promptArchetype(String prompt) {
        if (prompt.isBlank()) return "";
        if (!checkPromptStructure(prompt)) return "";
        Exception err = null;

        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++){
            try{
                return client.generateText("gemini-2.5-flash", prompt, config);
            }catch(Exception e){
                err = e;
                // swallow and retry
            }
        }

        log.error("e: ", err);

//        TODO log
        return ""; // fails return empty string
    }

    private boolean checkPromptStructure(String prompt){
        ObjectMapper om = new ObjectMapper();

        boolean matches;
        try {
            om.readValue(prompt, WrappedResponse.class);
            matches = true;
        } catch (Exception e) {
            matches = false;
        }

        return matches;
    }
}
