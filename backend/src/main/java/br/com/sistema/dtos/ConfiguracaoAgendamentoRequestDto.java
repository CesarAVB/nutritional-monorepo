package br.com.sistema.dtos;

import java.time.LocalTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConfiguracaoAgendamentoRequestDto {

    @NotNull
    private Integer duracaoPadraoMinutos;

    @NotNull
    private Integer intervaloEntreConsultasMinutos;

    @NotBlank
    private String diasAtendimento;

    @NotNull
    private LocalTime horarioInicio;

    @NotNull
    private LocalTime horarioFim;

    @NotBlank
    private String fusoHorario;

    @NotNull
    private LocalTime janelaNotifInicio;

    @NotNull
    private LocalTime janelaNotifFim;

    private String emailNotificacao;

    @NotNull
    private Boolean notifAoCriar;

    @NotNull
    private Boolean notifAoCancelar;

    @NotNull
    private Boolean alertaNaoConfirmacao;

    @NotNull
    private Boolean lembreteImediatoAtivo;

    @NotNull
    private Boolean lembrete72hAtivo;

    @NotNull
    private Boolean lembrete24hAtivo;

    @NotNull
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
