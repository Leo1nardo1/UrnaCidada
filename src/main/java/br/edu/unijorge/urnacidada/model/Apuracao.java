package br.edu.unijorge.urnacidada.model;

import java.time.LocalDateTime;
import java.util.List;

/** Fotografia da apuração: resultados por cargo, quantidade de eleitores e horário. */
public record Apuracao(
        List<ResultadoCargo> cargos,
        int quantidadeEleitores,
        LocalDateTime geradaEm
) {

    public Apuracao {

        if (geradaEm == null) {
            throw new IllegalArgumentException("O horário da apuração é obrigatório.");
        }

        cargos = List.copyOf(cargos);
    }
}
