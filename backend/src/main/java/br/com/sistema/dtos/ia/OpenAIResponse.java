package br.com.sistema.dtos.ia;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OpenAIResponse {

    private String id;
    private List<Choice> choices;
    private Usage usage;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Usage {
        @com.fasterxml.jackson.annotation.JsonProperty("prompt_tokens")
        private int promptTokens;
        @com.fasterxml.jackson.annotation.JsonProperty("completion_tokens")
        private int completionTokens;
        @com.fasterxml.jackson.annotation.JsonProperty("total_tokens")
        private int totalTokens;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Choice {
        private OpenAIRequest.Message message;

        @com.fasterxml.jackson.annotation.JsonProperty("finish_reason")
        private String finishReason;
    }
}
