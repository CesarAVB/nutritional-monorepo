package br.com.sistema.dtos.ia;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OpenAIRequest {

    private String model;
    private List<Message> messages;
    private Double temperature;

    @JsonProperty("response_format")
    private Map<String, String> responseFormat;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Message {
        private String role;
        private String content;
    }
}
