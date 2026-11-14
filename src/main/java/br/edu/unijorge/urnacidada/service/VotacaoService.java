
package br.edu.unijorge.urnacidada.service;

import br.edu.unijorge.urnacidada.config.DadosSimulacao;
import br.edu.unijorge.urnacidada.model.Candidato;
import br.edu.unijorge.urnacidada.model.Cargo;
import br.edu.unijorge.urnacidada.model.SessaoVotacao;
import br.edu.unijorge.urnacidada.model.TipoVoto;
import br.edu.unijorge.urnacidada.model.Voto;
import br.edu.unijorge.urnacidada.repository.VotoRepository;

import java.util.List;
import java.util.Optional;

public class VotacaoService {

    private static final List<Cargo> ORDEM_CARGOS = List.of(
            Cargo.DEPUTADO_FEDERAL,
            Cargo.PRESIDENTE
    );

    private final DadosSimulacao dadosSimulacao;
    private final VotoRepository votoRepository;

    private SessaoVotacao sessaoAtual;
    private int indiceCargoAtual;

    private Candidato candidatoSelecionado;
    private TipoVoto tipoVotoPendente;

    public VotacaoService(
            DadosSimulacao dadosSimulacao,
            VotoRepository votoRepository
    ) {

        if (dadosSimulacao == null || votoRepository == null) {
            throw new IllegalArgumentException(
                    "Os dados da simulação e o repositório são obrigatórios."
            );
        }

        this.dadosSimulacao = dadosSimulacao;
        this.votoRepository = votoRepository;
    }

    // Inicia uma nova sessão de votação
    public SessaoVotacao iniciarSessao() {

        if (sessaoAtual != null && sessaoAtual.estaAberta()) {
            throw new IllegalStateException(
                    "Já existe uma sessão de votação em andamento."
            );
        }

        sessaoAtual = new SessaoVotacao();

        indiceCargoAtual = 0;

        limparVotoPendente();

        return sessaoAtual;
    }
    // Encerra a sessão manualmente (acionado pelo botão)
    public void encerrarSessao() {
        validarSessaoAtiva();
        sessaoAtual.encerrar();
    }
    // Retorna o cargo que está sendo votado
    public Cargo getCargoAtual() {

        validarSessaoAtiva();

        return ORDEM_CARGOS.get(indiceCargoAtual);
    }

    // Retorna o número do cargo atual na sequência
    public int getNumeroCargoAtual() {

        validarSessaoAtiva();

        return indiceCargoAtual + 1;
    }

    // Quantidade total de cargos da simulação
    public int getQuantidadeCargos() {
        return ORDEM_CARGOS.size();
    }

    // Sequência de cargos da simulação (usada pela barra de progresso)
    public List<Cargo> getCargos() {
        return ORDEM_CARGOS;
    }

    // Candidatos do cargo que está sendo votado (usada pelo painel lateral)
    public List<Candidato> listarCandidatosDoCargoAtual() {

        validarSessaoAtiva();

        return dadosSimulacao.listarCandidatosPorCargo(getCargoAtual());
    }

    // Procura um candidato de acordo com o cargo atual
    public Candidato selecionarCandidato(String numero) {

        validarSessaoAtiva();

        Optional<Candidato> resultado =
                dadosSimulacao.buscarCandidatoPorNumero(
                        numero,
                        getCargoAtual()
                );

        if (resultado.isEmpty()) {

            limparVotoPendente();

            throw new IllegalArgumentException(
                    "Nenhum candidato encontrado para o número informado."
            );
        }

        candidatoSelecionado = resultado.get();

        tipoVotoPendente = TipoVoto.NOMINAL;

        return candidatoSelecionado;
    }

    // Prepara um voto em branco
    public void prepararVotoBranco() {

        validarSessaoAtiva();

        candidatoSelecionado = null;

        tipoVotoPendente = TipoVoto.BRANCO;
    }

    // Prepara um voto nulo
    public void prepararVotoNulo() {

        validarSessaoAtiva();

        candidatoSelecionado = null;

        tipoVotoPendente = TipoVoto.NULO;
    }

    // Corrige a escolha antes da confirmação
    public void corrigirVoto() {

        validarSessaoAtiva();

        limparVotoPendente();
    }

    // Retorna o candidato selecionado, se existir
    public Optional<Candidato> getCandidatoSelecionado() {

        return Optional.ofNullable(candidatoSelecionado);
    }

    // Retorna o tipo de voto preparado, se existir
    public Optional<TipoVoto> getTipoVotoPendente() {

        return Optional.ofNullable(tipoVotoPendente);
    }

    // Confirma e registra o voto
    public Voto confirmarVoto() {

        validarSessaoAtiva();

        if (tipoVotoPendente == null) {
            throw new IllegalStateException(
                    "É necessário selecionar um candidato ou preparar um voto branco/nulo."
            );
        }

        Voto voto = criarVotoPendente();

        // Só avança depois que o repositório confirmar o salvamento
        votoRepository.salvar(voto);

        limparVotoPendente();

        indiceCargoAtual++;

        // Encerra automaticamente depois do último cargo
        if (indiceCargoAtual >= ORDEM_CARGOS.size()) {
            sessaoAtual.encerrar();
        }

        return voto;
    }

    // Retorna os votos registrados na sessão atual
    public List<Voto> listarVotosConfirmados() {

        if (sessaoAtual == null) {
            throw new IllegalStateException(
                    "Ainda não existe uma sessão de votação."
            );
        }

        return votoRepository.buscarPorSessao(sessaoAtual.getId());
    }

    // Verifica se existe uma sessão em andamento
    public boolean estaEmAndamento() {

        return sessaoAtual != null && sessaoAtual.estaAberta();
    }

    // Verifica se a sessão foi encerrada
    public boolean estaConcluida() {

        return sessaoAtual != null
                && sessaoAtual.getStatus()
                == br.edu.unijorge.urnacidada.model.StatusSessao.ENCERRADA;
    }

    // Retorna a sessão atual
    public Optional<SessaoVotacao> getSessaoAtual() {

        return Optional.ofNullable(sessaoAtual);
    }

    // Cria o objeto Voto de acordo com a escolha pendente
    private Voto criarVotoPendente() {

        Cargo cargo = getCargoAtual();
        String sessaoId = sessaoAtual.getId();

        return switch (tipoVotoPendente) {

            case NOMINAL -> {

                if (candidatoSelecionado == null) {
                    throw new IllegalStateException(
                            "Nenhum candidato foi selecionado."
                    );
                }

                yield Voto.criarNominal(
                        sessaoId,
                        cargo,
                        candidatoSelecionado.getId()
                );
            }

            case BRANCO -> Voto.criarBranco(
                    sessaoId,
                    cargo
            );

            case NULO -> Voto.criarNulo(
                    sessaoId,
                    cargo
            );
        };
    }

    // Limpa qualquer escolha ainda não confirmada
    private void limparVotoPendente() {

        candidatoSelecionado = null;

        tipoVotoPendente = null;
    }

    // Garante que as operações ocorram em uma sessão ativa
    private void validarSessaoAtiva() {

        if (sessaoAtual == null) {
            throw new IllegalStateException(
                    "Nenhuma sessão de votação foi iniciada."
            );
        }

        if (!sessaoAtual.estaAberta()) {
            throw new IllegalStateException(
                    "A sessão de votação já foi encerrada."
            );
        }

        if (indiceCargoAtual >= ORDEM_CARGOS.size()) {
            throw new IllegalStateException(
                    "Não existem mais cargos disponíveis para votação."
            );
        }
    }
}
