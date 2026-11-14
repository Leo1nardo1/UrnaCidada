package br.edu.unijorge.urnacidada;

import br.edu.unijorge.urnacidada.config.DadosSimulacao;
import br.edu.unijorge.urnacidada.model.Candidato;
import br.edu.unijorge.urnacidada.model.Cargo;

public class TesteDadosSimulacao {

    public static void main(String[] args) {

        DadosSimulacao dados = new DadosSimulacao();

        System.out.println("=== PARTIDOS CADASTRADOS ===");

        dados.listarPartidos().forEach(partido ->
                System.out.println(
                        partido.getNumero() + " - "
                                + partido.getNome()
                                + " (" + partido.getSigla() + ")"
                )
        );

        System.out.println("\n=== CANDIDATOS A DEPUTADO FEDERAL ===");

        dados.listarCandidatosPorCargo(Cargo.DEPUTADO_FEDERAL)
                .forEach(TesteDadosSimulacao::exibirCandidato);

        System.out.println("\n=== CANDIDATOS A PRESIDENTE ===");

        dados.listarCandidatosPorCargo(Cargo.PRESIDENTE)
                .forEach(TesteDadosSimulacao::exibirCandidato);

        System.out.println("\n=== TESTANDO BUSCA POR NÚMERO ===");

        Candidato candidato = dados
                .buscarCandidatoPorNumero(
                        "1101",
                        Cargo.DEPUTADO_FEDERAL
                )
                .orElseThrow(() ->
                        new IllegalStateException("Candidato não encontrado.")
                );

        System.out.println("Candidato encontrado: " + candidato.getNome());

        boolean inexistente = dados
                .buscarCandidatoPorNumero(
                        "9999",
                        Cargo.DEPUTADO_FEDERAL
                )
                .isEmpty();

        if (!inexistente) {
            throw new IllegalStateException(
                    "A busca deveria retornar um resultado vazio."
            );
        }

        System.out.println("Número inexistente tratado corretamente.");

        System.out.println("\n=== TESTE FINALIZADO ===");
    }

    private static void exibirCandidato(Candidato candidato) {

        System.out.println(
                candidato.getNumero()
                        + " - " + candidato.getNome()
                        + " - " + candidato.getPartido().getSigla()
                        + " - " + candidato.getCargo().getNome()
        );
    }
}
