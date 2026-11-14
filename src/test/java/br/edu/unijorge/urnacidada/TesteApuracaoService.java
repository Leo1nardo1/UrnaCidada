package br.edu.unijorge.urnacidada;

import br.edu.unijorge.urnacidada.config.DadosSimulacao;
import br.edu.unijorge.urnacidada.model.Apuracao;
import br.edu.unijorge.urnacidada.model.Cargo;
import br.edu.unijorge.urnacidada.model.ResultadoCargo;
import br.edu.unijorge.urnacidada.repository.VotoMemoriaRepository;
import br.edu.unijorge.urnacidada.service.ApuracaoService;
import br.edu.unijorge.urnacidada.service.ExportadorCsv;
import br.edu.unijorge.urnacidada.service.VotacaoService;

public class TesteApuracaoService {

    public static void main(String[] args) {

        DadosSimulacao dados = new DadosSimulacao();
        VotoMemoriaRepository repository = new VotoMemoriaRepository();
        VotacaoService votacao = new VotacaoService(dados, repository);
        ApuracaoService apuracao = new ApuracaoService(dados, repository);

        // =====================================
        // TESTE 1 - Apuração sem votos
        // =====================================

        System.out.println("=== TESTE 1: APURAÇÃO VAZIA ===");

        Apuracao vazia = apuracao.apurar();

        verificar(vazia.quantidadeEleitores() == 0, "Não deveria haver eleitores.");
        verificar(vazia.cargos().size() == Cargo.values().length,
                "Deveria haver um resultado por cargo.");

        for (ResultadoCargo r : vazia.cargos()) {
            verificar(r.totalVotos() == 0, "O total deveria ser zero.");
            verificar(r.eleitoPrimeiroTurno().isEmpty(), "Ninguém deveria estar eleito.");
            verificar(!r.haveraSegundoTurno(), "Sem votos não há segundo turno.");
            verificar(r.percentualDoTotal(0) == 0, "Percentual sem votos deveria ser zero.");
        }

        verificar(vazia.cargos().get(0).candidatos().size() == 6,
                "Todos os deputados deveriam aparecer, mesmo sem votos.");

        // =====================================
        // TESTE 2 - Contagem de votos
        // =====================================

        System.out.println("=== TESTE 2: CONTAGEM ===");

        // Eleitor 1: Leão (1101) e Fênix (22)
        votar(votacao, "1101", "22");
        // Eleitor 2: Leão (1101) e Fênix (22)
        votar(votacao, "1101", "22");
        // Eleitor 3: Tigre (1102) e Coruja (11)
        votar(votacao, "1102", "11");
        // Eleitor 4: nulo e branco
        votarNuloEBranco(votacao);
        // Eleitor 5: branco e nulo
        votacao.iniciarSessao();
        votacao.prepararVotoBranco();
        votacao.confirmarVoto();
        votacao.prepararVotoNulo();
        votacao.confirmarVoto();

        Apuracao resultado = apuracao.apurar();

        verificar(resultado.quantidadeEleitores() == 5, "Deveriam existir 5 eleitores.");

        ResultadoCargo deputados = resultado.cargos().get(0);
        ResultadoCargo presidente = resultado.cargos().get(1);

        verificar(deputados.cargo() == Cargo.DEPUTADO_FEDERAL, "Ordem dos cargos incorreta.");
        verificar(deputados.votosNominais() == 3, "Deveria haver 3 votos nominais para deputado.");
        verificar(deputados.brancos() == 1, "Deveria haver 1 branco para deputado.");
        verificar(deputados.nulos() == 1, "Deveria haver 1 nulo para deputado.");
        verificar(deputados.totalVotos() == 5, "Total de deputado deveria ser 5.");

        verificar(deputados.candidatos().get(0).candidato().getNome().equals("Leão"),
                "Leão deveria liderar.");
        verificar(deputados.candidatos().get(0).votos() == 2, "Leão deveria ter 2 votos.");
        verificar(deputados.candidatos().get(1).votos() == 1, "Tigre deveria ter 1 voto.");

        System.out.println("Líder dos deputados: "
                + deputados.candidatos().get(0).candidato().getNome());

        // =====================================
        // TESTE 3 - Regra do segundo turno
        // =====================================

        System.out.println("=== TESTE 3: ELEITO / SEGUNDO TURNO ===");

        // Presidente: Fênix 2, Coruja 1 (válidos = 3) -> Fênix tem 66,7%
        verificar(presidente.votosNominais() == 3, "Presidente: 3 votos válidos.");
        verificar(presidente.eleitoPrimeiroTurno().isPresent(), "Deveria haver eleito.");
        verificar(presidente.eleitoPrimeiroTurno().get().candidato().getNome().equals("Fênix"),
                "Fênix deveria ser eleita.");
        verificar(!presidente.haveraSegundoTurno(), "Não deveria haver segundo turno.");

        // Proporcional nunca elege por maioria simples nesta simulação
        verificar(deputados.eleitoPrimeiroTurno().isEmpty(),
                "Cargo proporcional não tem eleito por maioria.");

        // Mais 1 voto para Panda e 1 para Coruja: Fênix 2 de 5 = 40% -> segundo turno
        votar(votacao, "1101", "33");
        votar(votacao, "1101", "11");

        ResultadoCargo presidente2 = apuracao.apurar().cargos().get(1);

        verificar(presidente2.votosNominais() == 5, "Presidente: 5 votos válidos.");
        verificar(presidente2.eleitoPrimeiroTurno().isEmpty(),
                "Ninguém passou de 50% dos válidos.");
        verificar(presidente2.haveraSegundoTurno(), "Deveria haver segundo turno.");

        // Branco e nulo não contam para a maioria: 1 voto válido + branco = eleito
        VotoMemoriaRepository r2 = new VotoMemoriaRepository();
        VotacaoService v2 = new VotacaoService(dados, r2);
        votar(v2, "1101", "22");
        v2.iniciarSessao();
        v2.prepararVotoBranco();
        v2.confirmarVoto();
        v2.prepararVotoBranco();
        v2.confirmarVoto();
        ResultadoCargo p3 = new ApuracaoService(dados, r2).apurar().cargos().get(1);
        verificar(p3.eleitoPrimeiroTurno().isPresent(),
                "Brancos não deveriam impedir a eleição pelos votos válidos.");

        // =====================================
        // TESTE 4 - CSV
        // =====================================

        System.out.println("=== TESTE 4: EXPORTAÇÃO CSV ===");

        String csv = ExportadorCsv.gerar(apuracao.apurar());
        String[] linhas = csv.split("\n");

        verificar(linhas[0].startsWith("cargo;numero;nome;partido;votos"),
                "Cabeçalho do CSV incorreto.");
        // cabeçalho + (6 deputados + 3 totais) + (3 presidentes + 3 totais)
        verificar(linhas.length == 1 + 9 + 6, "Quantidade de linhas do CSV incorreta: " + linhas.length);
        verificar(csv.contains("Deputado Federal;1101;Leão;PN;4;"),
                "Linha do Leão não encontrada no CSV.");

        System.out.println(csv);
        System.out.println("=== TODOS OS TESTES PASSARAM ===");
    }

    private static void votar(VotacaoService votacao, String deputado, String presidente) {
        votacao.iniciarSessao();
        votacao.selecionarCandidato(deputado);
        votacao.confirmarVoto();
        votacao.selecionarCandidato(presidente);
        votacao.confirmarVoto();
    }

    private static void votarNuloEBranco(VotacaoService votacao) {
        votacao.iniciarSessao();
        votacao.prepararVotoNulo();
        votacao.confirmarVoto();
        votacao.prepararVotoBranco();
        votacao.confirmarVoto();
    }

    private static void verificar(boolean condicao, String mensagem) {
        if (!condicao) {
            throw new AssertionError(mensagem);
        }
    }
}
