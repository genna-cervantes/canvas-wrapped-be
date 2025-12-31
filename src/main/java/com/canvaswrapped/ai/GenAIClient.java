package com.canvaswrapped.ai;

import com.google.genai.types.GenerateContentConfig;

public interface GenAIClient {
    String generateText(String model, String prompt, GenerateContentConfig config);
}
