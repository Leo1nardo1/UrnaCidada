package br.edu.unijorge.urnacidada.model;

import java.time.LocalDateTime;
import java.util.UUID;

public class SessaoVotacao {

    private final String id;
    private final LocalDateTime dataInicio;

    private LocalDateTime dataFim;
    private StatusSessao status;

    public SessaoVotacao() {
        this.id = UUID.randomUUID().toString();
        this.dataInicio = LocalDateTime.now();
        this.status = StatusSessao.ABERTA;
    }

    public void encerrar() {

        if (status == StatusSessao.ENCERRADA) {
            throw new IllegalStateException(
                    "Esta sessão já foi encerrada."
            );
        }

        this.dataFim = LocalDateTime.now();
        this.status = StatusSessao.ENCERRADA;
    }

    public String getId() {
        return id;
    }

    public LocalDateTime getDataInicio() {
        return dataInicio;
    }

    public LocalDateTime getDataFim() {
        return dataFim;
    }

    public StatusSessao getStatus() {
        return status;
    }

    public boolean estaAberta() {
        return status == StatusSessao.ABERTA;
    }
}
