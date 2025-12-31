package com.canvaswrapped.service;

import com.canvaswrapped.ai.GenAIClient;
import com.canvaswrapped.model.WrappedResponse;
import com.google.genai.types.GenerateContentConfig;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.ObjectMapper;

import static org.mockito.ArgumentMatchers.eq;

@ExtendWith(MockitoExtension.class)
public class GeminiServiceTest {

    @Mock
    GenAIClient client;
    @Mock
    GenerateContentConfig config;

    @InjectMocks
    GeminiService geminiService;

    @Test
    void promptArchetype_returnsJsonString_happyPath(){
        String prompt = generateCompletePrompt();
        String jsonResponse = "{\"type\": \"The Steady Navigator\",\"message\": \"Keep shining brightly! Your consistent effort and positive energy are always valuable.\"}";

        Mockito.when(client.generateText(eq("gemini-2.5-flash"), eq(prompt), eq(config))).thenReturn(jsonResponse);
        String json = geminiService.promptArchetype(prompt);

        Assertions.assertEquals(jsonResponse, json);
    }

    @Test
    void promptArchetype_returnsBlankStringOnEmptyPrompt(){
        String prompt = "";
        String json = geminiService.promptArchetype(prompt);
        Assertions.assertEquals("", json);
    }

    @Test
    void promptArchetype_returnsBlankStringOnInvalidPromptStructure(){
        String prompt = "invalid prompt";
        String json = geminiService.promptArchetype(prompt);
        Assertions.assertEquals("", json);
    }

    @Test
    void promptArchetype_retriesAndEventuallyReturnsValidJson(){
        String prompt = generateCompletePrompt();
        String jsonResponse = "{\"type\": \"The Steady Navigator\",\"message\": \"Keep shining brightly! Your consistent effort and positive energy are always valuable.\"}";

        Mockito.when(client.generateText(eq("gemini-2.5-flash"), eq(prompt), eq(config)))
                .thenThrow(new RuntimeException("timeout 1"))
                .thenThrow(new RuntimeException("timeout 2"))
                .thenReturn(jsonResponse);
        String json = geminiService.promptArchetype(prompt);

        Assertions.assertEquals(jsonResponse, json);
    }

//    TODO integration tests:
//    unrelated prompt
//    all missing data
//    partial data
//    override system prompt

    static String generateCompletePrompt(){
        ObjectMapper om = new ObjectMapper();

        WrappedResponse wr = new WrappedResponse();
        return om.writeValueAsString(wr);
    }


}
