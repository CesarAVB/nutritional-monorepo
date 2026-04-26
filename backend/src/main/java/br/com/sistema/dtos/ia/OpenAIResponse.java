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

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Choice {
        private OpenAIRequest.Message message;

        @com.fasterxml.jackson.annotation.JsonProperty("finish_reason")
        private String finishReason;
    }
}
