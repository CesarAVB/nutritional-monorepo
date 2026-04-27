package br.com.sistema.models;

import java.time.LocalTime;

import br.com.sistema.configurations.AesEncryptedConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "tbl_configuracao_agendamento")
public class ConfiguracaoAgendamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "duracao_padrao_minutos", nullable = false)
    private Integer duracaoPadraoMinutos;

    @Column(name = "intervalo_entre_consultas_minutos", nullable = false)
    private Integer intervaloEntreConsultasMinutos;

    @Column(name = "dias_atendimento", nullable = false, length = 50)
    private String diasAtendimento;

    @Column(name = "horario_inicio", nullable = false)
    private LocalTime horarioInicio;

    @Column(name = "horario_fim", nullable = false)
    private LocalTime horarioFim;

    @Column(name = "fuso_horario", nullable = false, length = 50)
    private String fusoHorario;

    @Column(name = "janela_notif_inicio", nullable = false)
    private LocalTime janelaNotifInicio;

    @Column(name = "janela_notif_fim", nullable = false)
    private LocalTime janelaNotifFim;

    @Column(name = "email_notificacao", length = 200)
    private String emailNotificacao;

    @Column(name = "notif_ao_criar", nullable = false)
    private Boolean notifAoCriar;

    @Column(name = "notif_ao_cancelar", nullable = false)
    private Boolean notifAoCancelar;

    @Column(name = "alerta_nao_confirmacao", nullable = false)
    private Boolean alertaNaoConfirmacao;

    @Column(name = "lembrete_imediato_ativo", nullable = false)
    private Boolean lembreteImediatoAtivo;

    @Column(name = "lembrete_72h_ativo", nullable = false)
    private Boolean lembrete72hAtivo;

    @Column(name = "lembrete_24h_ativo", nullable = false)
    private Boolean lembrete24hAtivo;

    @Column(name = "lembrete_2h_ativo", nullable = false)
    private Boolean lembrete2hAtivo;

    @Column(name = "evolution_url", length = 500)
    private String evolutionUrl;

    @Column(name = "evolution_instancia", length = 200)
    private String evolutionInstancia;

    @Column(name = "evolution_api_key", columnDefinition = "TEXT")
    @Convert(converter = AesEncryptedConverter.class)
    private String evolutionApiKey;

    @Column(columnDefinition = "TEXT")
    private String templateConfirmacao;

    @Column(columnDefinition = "TEXT")
    private String templateLembrete72h;

    @Column(columnDefinition = "TEXT")
    private String templateLembrete24h;

    @Column(columnDefinition = "TEXT")
    private String templateLembrete2h;

    @Column(columnDefinition = "TEXT")
    private String templateConsultaConfirmada;

    @Column(columnDefinition = "TEXT")
    private String templateConsultaCancelada;
}
