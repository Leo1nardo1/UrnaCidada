package br.edu.unijorge.urnacidada.model;

/** Quantidade de votos nominais recebidos por um candidato. */
public record ResultadoCandidato(Candidato candidato, int votos) {

    public ResultadoCandidato {

        if (candidato == null) {
            throw new IllegalArgumentException("O candidato é obrigatório.");
        }

        if (votos < 0) {
            throw new IllegalArgumentException("A quantidade de votos não pode ser negativa.");
        }
    }
}
