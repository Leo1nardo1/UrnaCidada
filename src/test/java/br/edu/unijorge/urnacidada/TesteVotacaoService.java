package br.edu.unijorge.urnacidada;

import br.edu.unijorge.urnacidada.config.DadosSimulacao;
import br.edu.unijorge.urnacidada.model.Candidato;
import br.edu.unijorge.urnacidada.model.Cargo;
import br.edu.unijorge.urnacidada.model.TipoVoto;
import br.edu.unijorge.urnacidada.model.Voto;
import br.edu.unijorge.urnacidada.repository.VotoMemoriaRepository;
import br.edu.unijorge.urnacidada.service.VotacaoService;

public class TesteVotacaoService {

    public static void main(String[] args) {

        DadosSimulacao dados = new DadosSimulacao();

        VotoMemoriaRepository repository =
                new VotoMemoriaRepository();

        VotacaoService service = new VotacaoService(
                dados,
                repository
        );

        // =====================================
        // TESTE 1 - Iniciar sessão
        // =====================================

        System.out.println("=== TESTE 1: INICIAR SESSÃO ===");

        var primeiraSessao = service.iniciarSessao();

        verificar(
                service.estaEmAndamento(),
                "A sessão deveria estar em andamento."
        );

        verificar(
                service.getCargoAtual() == Cargo.DEPUTADO_FEDERAL,
                "O primeiro cargo deveria ser Deputado Federal."
        );

        System.out.println("Sessão iniciada corretamente.");
        System.out.println("Primeiro cargo: "
                + service.getCargoAtual().getNome());


        // =====================================
        // TESTE 2 - Impedir confirmação sem escolha
        // =====================================

        System.out.println("\n=== TESTE 2: CONFIRMAÇÃO SEM ESCOLHA ===");

        boolean confirmacaoBloqueada = false;

        try {
            service.confirmarVoto();
        } catch (IllegalStateException e) {
            confirmacaoBloqueada = true;
        }

        verificar(
                confirmacaoBloqueada,
                "A confirmação deveria ser bloqueada."
        );

        System.out.println("Confirmação sem escolha bloqueada.");


        // =====================================
        // TESTE 3 - Candidato inexistente
        // =====================================

        System.out.println("\n=== TESTE 3: CANDIDATO INEXISTENTE ===");

        boolean candidatoInexistente = false;

        try {
            service.selecionarCandidato("9999");
        } catch (IllegalArgumentException e) {
            candidatoInexistente = true;
            System.out.println(e.getMessage());
        }

        verificar(
                candidatoInexistente,
                "O número inexistente deveria ser rejeitado."
        );


        // =====================================
        // TESTE 4 - Selecionar e corrigir
        // =====================================

        System.out.println("\n=== TESTE 4: SELECIONAR E CORRIGIR ===");

        Candidato candidato = service.selecionarCandidato("1101");

        System.out.println("Candidato selecionado: "
                + candidato.getNome());

        service.corrigirVoto();

        verificar(
                service.getCandidatoSelecionado().isEmpty(),
                "A correção deveria limpar o candidato selecionado."
        );

        verificar(
                service.getTipoVotoPendente().isEmpty(),
                "A correção deveria limpar o voto pendente."
        );

        System.out.println("Escolha corrigida corretamente.");


        // =====================================
        // TESTE 5 - Confirmar voto nominal
        // =====================================

        System.out.println("\n=== TESTE 5: CONFIRMAR VOTO NOMINAL ===");

        service.selecionarCandidato("1102");

        Voto votoDeputado = service.confirmarVoto();

        verificar(
                votoDeputado.getTipo() == TipoVoto.NOMINAL,
                "O voto deveria ser nominal."
        );

        verificar(
                votoDeputado.getCargo() == Cargo.DEPUTADO_FEDERAL,
                "O voto deveria pertencer ao cargo Deputado Federal."
        );

        System.out.println("Voto registrado para: Tigre");
        System.out.println("Próximo cargo: "
                + service.getCargoAtual().getNome());


        // =====================================
        // TESTE 6 - Confirmar voto branco
        // =====================================

        System.out.println("\n=== TESTE 6: VOTO BRANCO ===");

        service.prepararVotoBranco();

        Voto votoPresidente = service.confirmarVoto();

        verificar(
                votoPresidente.getTipo() == TipoVoto.BRANCO,
                "O voto deveria ser branco."
        );

        verificar(
                votoPresidente.getCargo() == Cargo.PRESIDENTE,
                "O voto deveria pertencer ao cargo Presidente."
        );

        verificar(
                service.estaConcluida(),
                "A sessão deveria estar encerrada."
        );

        System.out.println("Voto branco registrado.");
        System.out.println("Primeira sessão encerrada.");


        // =====================================
        // TESTE 7 - Recuperar votos da sessão
        // =====================================

        System.out.println("\n=== TESTE 7: RECUPERAR VOTOS ===");

        var votosPrimeiraSessao =
                repository.buscarPorSessao(primeiraSessao.getId());

        verificar(
                votosPrimeiraSessao.size() == 2,
                "A primeira sessão deveria possuir dois votos."
        );

        System.out.println("Votos registrados: "
                + votosPrimeiraSessao.size());


        // =====================================
        // TESTE 8 - Criar uma segunda sessão
        // =====================================

        System.out.println("\n=== TESTE 8: SEGUNDA SESSÃO ===");

        var segundaSessao = service.iniciarSessao();

        verificar(
                service.getCargoAtual() == Cargo.DEPUTADO_FEDERAL,
                "A segunda sessão deveria começar pelo primeiro cargo."
        );

        verificar(
                primeiraSessao.getId().equals(segundaSessao.getId()) == false,
                "As sessões deveriam possuir identificadores diferentes."
        );

        System.out.println("Segunda sessão iniciada.");


        // =====================================
        // TESTE 9 - Voto nulo
        // =====================================

        System.out.println("\n=== TESTE 9: VOTO NULO ===");

        service.prepararVotoNulo();

        Voto votoNulo = service.confirmarVoto();

        verificar(
                votoNulo.getTipo() == TipoVoto.NULO,
                "O voto deveria ser nulo."
        );

        System.out.println("Voto nulo registrado.");


        // =====================================
        // TESTE 10 - Confirmar segundo cargo
        // =====================================

        System.out.println("\n=== TESTE 10: SEGUNDO CARGO ===");

        Candidato presidente = service.selecionarCandidato("22");

        Voto votoFinal = service.confirmarVoto();

        verificar(
                votoFinal.getCandidatoId().equals(presidente.getId()),
                "O voto deveria pertencer ao candidato selecionado."
        );

        verificar(
                service.estaConcluida(),
                "A segunda sessão deveria estar encerrada."
        );

        System.out.println("Presidente selecionado: "
                + presidente.getNome());


        // =====================================
        // TESTE 11 - Separação entre sessões
        // =====================================

        System.out.println("\n=== TESTE 11: SEPARAÇÃO DE SESSÕES ===");

        int votosPrimeira =
                repository.buscarPorSessao(primeiraSessao.getId()).size();

        int votosSegunda =
                repository.buscarPorSessao(segundaSessao.getId()).size();

        verificar(votosPrimeira == 2,
                "A primeira sessão deveria possuir dois votos.");

        verificar(votosSegunda == 2,
                "A segunda sessão deveria possuir dois votos.");

        System.out.println("Primeira sessão: " + votosPrimeira + " votos.");
        System.out.println("Segunda sessão: " + votosSegunda + " votos.");

        System.out.println("\n=== TODOS OS TESTES PASSARAM ===");
    }

    private static void verificar(
            boolean condicao,
            String mensagem
    ) {

        if (!condicao) {
            throw new AssertionError(mensagem);
        }
    }
}
