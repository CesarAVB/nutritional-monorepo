package br.com.sistema.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true) // Ignora propriedades desconhecidas no JSON de entrada
public class RelatorioRequestDTO {
    private Long pacienteId;
    private Long consultaId;
    private String templateType; // "padrao", "simples", "detalhado"

    public Long getPacienteId() {
        return pacienteId;
    }
    public void setPacienteId(Long pacienteId) {
        this.pacienteId = pacienteId;
    }
    public Long getConsultaId() {
        return consultaId;
    }
    public void setConsultaId(Long consultaId) {
        this.consultaId = consultaId;
    }
    public String getTemplateType() {
        return templateType;
    }
    public void setTemplateType(String templateType) {
        this.templateType = templateType;
    }

    // Compatibilidade com payloads que enviam 'id' (ex.: id da consulta)
    // Se o cliente enviar 'id' em vez de 'consultaId', mapeamos automaticamente
    public void setId(Long id) {
        if (this.consultaId == null) {
            this.consultaId = id;
        }
    }
}