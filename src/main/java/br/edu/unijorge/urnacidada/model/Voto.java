package br.edu.unijorge.urnacidada.model;

import java.time.LocalDateTime;
import java.util.UUID;

public class Voto {

    private final String id;
    private final String sessaoId;
    private final Cargo cargo;
    private final TipoVoto tipo;
    private final String candidatoId;
    private final LocalDateTime dataHora;

    private Voto(
            String id,
            String sessaoId,
            Cargo cargo,
            TipoVoto tipo,
            String candidatoId,
            LocalDateTime dataHora
    ) {

        if (sessaoId == null || sessaoId.isBlank()) {
            throw new IllegalArgumentException("A sessão do voto é obrigatória.");
        }

        if (cargo == null || tipo == null) {
            throw new IllegalArgumentException("Cargo e tipo de voto são obrigatórios.");
        }

        if (tipo == TipoVoto.NOMINAL &&
                (candidatoId == null || candidatoId.isBlank())) {
            throw new IllegalArgumentException(
                    "O voto nominal precisa identificar o candidato."
            );
        }

        if (tipo != TipoVoto.NOMINAL && candidatoId != null) {
            throw new IllegalArgumentException(
                    "Votos brancos e nulos não possuem candidato."
            );
        }

        this.id = id;
        this.sessaoId = sessaoId;
        this.cargo = cargo;
        this.tipo = tipo;
        this.candidatoId = candidatoId;
        this.dataHora = dataHora;
    }

    private Voto(
            String sessaoId,
            Cargo cargo,
            TipoVoto tipo,
            String candidatoId
    ) {
        this(UUID.randomUUID().toString(), sessaoId, cargo, tipo,
                candidatoId, LocalDateTime.now());
    }

    /**
     * Recria um voto já registrado (por exemplo, lido do Firestore),
     * preservando o ID e a data/hora originais.
     */
    public static Voto reconstituir(
            String id,
            String sessaoId,
            Cargo cargo,
            TipoVoto tipo,
            String candidatoId,
            LocalDateTime dataHora
    ) {

        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("O ID do voto é obrigatório.");
        }

        if (dataHora == null) {
            throw new IllegalArgumentException("A data e hora do voto são obrigatórias.");
        }

        return new Voto(id, sessaoId, cargo, tipo, candidatoId, dataHora);
    }

    public static Voto criarNominal(
            String sessaoId,
            Cargo cargo,
            String candidatoId
    ) {
        return new Voto(sessaoId, cargo, TipoVoto.NOMINAL, candidatoId);
    }

    public static Voto criarBranco(
            String sessaoId,
            Cargo cargo
    ) {
        return new Voto(sessaoId, cargo, TipoVoto.BRANCO, null);
    }

    public static Voto criarNulo(
            String sessaoId,
            Cargo cargo
    ) {
        return new Voto(sessaoId, cargo, TipoVoto.NULO, null);
    }

    public String getId() {
        return id;
    }

    public String getSessaoId() {
        return sessaoId;
    }

    public Cargo getCargo() {
        return cargo;
    }

    public TipoVoto getTipo() {
        return tipo;
    }

    public String getCandidatoId() {
        return candidatoId;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }
}
