package br.edu.unijorge.urnacidada;

import br.edu.unijorge.urnacidada.model.*;

public class TesteModelos {

    public static void main(String[] args) {

        Partido partido = new Partido(
                "P001",
                "Partido da Natureza",
                "PN",
                "11"
        );

        Candidato candidato = new Candidato(
                "C001",
                "Leão",
                "1101",
                partido,
                Cargo.DEPUTADO_FEDERAL
        );

        SessaoVotacao sessao = new SessaoVotacao();

        Voto voto = Voto.criarNominal(
                sessao.getId(),
                Cargo.DEPUTADO_FEDERAL,
                candidato.getId()
        );

        System.out.println("=== TESTE DOS MODELOS ===");

        System.out.println("Partido: " + partido.getNome());
        System.out.println("Número do partido: " + partido.getNumero());

        System.out.println("Candidato: " + candidato.getNome());
        System.out.println("Número: " + candidato.getNumero());

        System.out.println("Cargo: " + candidato.getCargo().getNome());
        System.out.println("Tipo de voto: " + voto.getTipo());

        System.out.println("Sessão: " + sessao.getStatus());

        sessao.encerrar();

        System.out.println("Sessão após encerramento: "
                + sessao.getStatus());

        System.out.println("=== TESTE FINALIZADO ===");
    }
}
