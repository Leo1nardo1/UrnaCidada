package br.edu.unijorge.urnacidada.model;

import java.util.List;
import java.util.Optional;

/**
 * Resultado da apuração de um cargo.
 *
 * A lista de candidatos vem ordenada do mais votado para o menos votado.
 * Percentuais seguem a regra da Justiça Eleitoral: para definir o eleito,
 * contam apenas os votos válidos (nominais); brancos e nulos ficam de fora.
 */
public record ResultadoCargo(
        Cargo cargo,
        List<ResultadoCandidato> candidatos,
        int brancos,
        int nulos
) {

    public ResultadoCargo {

        if (cargo == null) {
            throw new IllegalArgumentException("O cargo é obrigatório.");
        }

        if (brancos < 0 || nulos < 0) {
            throw new IllegalArgumentException("Brancos e nulos não podem ser negativos.");
        }

        candidatos = List.copyOf(candidatos);
    }

    /** Votos dados a candidatos (votos válidos). */
    public int votosNominais() {
        return candidatos.stream().mapToInt(ResultadoCandidato::votos).sum();
    }

    public int totalVotos() {
        return votosNominais() + brancos + nulos;
    }

    /** Percentual em relação ao total de votos do cargo (inclui brancos e nulos). */
    public double percentualDoTotal(int votos) {
        return totalVotos() == 0 ? 0 : votos * 100.0 / totalVotos();
    }

    /** Percentual em relação aos votos válidos (só nominais). */
    public double percentualDosValidos(int votos) {
        return votosNominais() == 0 ? 0 : votos * 100.0 / votosNominais();
    }

    /**
     * Candidato eleito em primeiro turno: só existe em cargo majoritário
     * e exige mais de 50% dos votos válidos.
     */
    public Optional<ResultadoCandidato> eleitoPrimeiroTurno() {

        if (cargo.getTipo() != TipoCargo.MAJORITARIO || candidatos.isEmpty()) {
            return Optional.empty();
        }

        ResultadoCandidato lider = candidatos.get(0);

        return lider.votos() * 2 > votosNominais()
                ? Optional.of(lider)
                : Optional.empty();
    }

    /** Cargo majoritário com votos válidos, mas sem ninguém acima de 50%. */
    public boolean haveraSegundoTurno() {
        return cargo.getTipo() == TipoCargo.MAJORITARIO
                && votosNominais() > 0
                && eleitoPrimeiroTurno().isEmpty();
    }
}
