package br.com.sistema.models;

import java.time.LocalDateTime;

import br.com.sistema.enums.CanalNotificacao;
import br.com.sistema.enums.StatusNotificacao;
import br.com.sistema.enums.TipoNotificacao;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "tbl_notificacao_agendamento")
public class NotificacaoAgendamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agendamento_id", nullable = false)
    private Agendamento agendamento;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoNotificacao tipo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CanalNotificacao canal;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusNotificacao status;

    @Column(name = "enviado_em")
    private LocalDateTime enviadoEm;

    @Column(columnDefinition = "TEXT")
    private String mensagem;

    @Column(columnDefinition = "TEXT")
    private String erro;
}
