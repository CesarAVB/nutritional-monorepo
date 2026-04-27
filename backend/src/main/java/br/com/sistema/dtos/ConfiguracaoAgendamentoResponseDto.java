package br.com.sistema.dtos;

import java.time.LocalTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConfiguracaoAgendamentoResponseDto {

    private Long id;
    private Integer duracaoPadraoMinutos;
    private Integer intervaloEntreConsultasMinutos;
    private String diasAtendimento;
    private LocalTime horarioInicio;
    private LocalTime horarioFim;
    private String fusoHorario;
    private LocalTime janelaNotifInicio;
    private LocalTime janelaNotifFim;
    private String emailNotificacao;
    private Boolean notifAoCriar;
    private Boolean notifAoCancelar;
    private Boolean alertaNaoConfirmacao;
    private Boolean lembreteImediatoAtivo;
    private Boolean lembrete72hAtivo;
    private Boolean lembrete24hAtivo;
    private Boolean lembrete2hAtivo;
    private String evolutionUrl;
    private String evolutionInstancia;
    private String evolutionApiKey;
    private String templateConfirmacao;
    private String templateLembrete72h;
    private String templateLembrete24h;
    private String templateLembrete2h;
    private String templateConsultaConfirmada;
    private String templateConsultaCancelada;
}
