package br.edu.unijorge.urnacidada.controller;

import br.edu.unijorge.urnacidada.model.Apuracao;
import br.edu.unijorge.urnacidada.model.Candidato;
import br.edu.unijorge.urnacidada.model.Cargo;
import br.edu.unijorge.urnacidada.service.ApuracaoService;
import br.edu.unijorge.urnacidada.service.ExportadorCsv;
import br.edu.unijorge.urnacidada.service.VotacaoService;
import br.edu.unijorge.urnacidada.view.TelaApuracao;
import br.edu.unijorge.urnacidada.view.TelaInicial;
import br.edu.unijorge.urnacidada.view.TelaVotacao;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.concurrent.ExecutionException;

public class UrnaController {

    private static final String TELA_INICIAL = "INICIO";
    private static final String TELA_VOTACAO = "VOTACAO";
    private static final String TELA_APURACAO = "APURACAO";

    private final VotacaoService votacaoService;
    private final ApuracaoService apuracaoService;
    private final TelaApuracao telaApuracao;
    private Apuracao ultimaApuracao;
    private final TelaInicial telaInicial;
    private final TelaVotacao telaVotacao;
    private final CardLayout cardLayout;
    private final JPanel painelPrincipal;

    public UrnaController(VotacaoService votacaoService, ApuracaoService apuracaoService,
                          TelaInicial telaInicial, TelaVotacao telaVotacao,
                          TelaApuracao telaApuracao, CardLayout cardLayout,
                          JPanel painelPrincipal) {
        if (votacaoService == null || apuracaoService == null || telaInicial == null
                || telaVotacao == null || telaApuracao == null
                || cardLayout == null || painelPrincipal == null) {
            throw new IllegalArgumentException("As dependências do controller são obrigatórias.");
        }
        this.votacaoService = votacaoService;
        this.apuracaoService = apuracaoService;
        this.telaApuracao = telaApuracao;
        this.telaInicial = telaInicial;
        this.telaVotacao = telaVotacao;
        this.cardLayout = cardLayout;
        this.painelPrincipal = painelPrincipal;
        configurarAcoes();
    }

    private void configurarAcoes() {
        telaInicial.setAcaoIniciar(this::iniciarVotacao);
        telaInicial.setAcaoApuracao(this::abrirApuracao);
        telaInicial.setModoArmazenamento(apuracaoService.getOrigemDados());
        telaApuracao.setAcoes(this::abrirApuracao, this::exportarApuracao,
                () -> exibirTela(TELA_INICIAL));
        telaVotacao.setAcoes(this::processarNumero, this::prepararVotoBranco,
                this::confirmarVoto, this::corrigirVoto, this::iniciarVotacao,
                this::encerrarVotacaoManual);
    }

    private void iniciarVotacao() {
        try {
            votacaoService.iniciarSessao();
            atualizarTelaVotacao();
            exibirTela(TELA_VOTACAO);
        } catch (RuntimeException e) {
            mostrarErro("Não foi possível iniciar a votação.", e);
        }
    }
    private void encerrarVotacaoManual() {
        int resposta = JOptionPane.showConfirmDialog(painelPrincipal,
                "Tem certeza que deseja encerrar a votação e ir para a apuração?\n"
                        + "Os votos já confirmados serão mantidos.",
                "Encerrar Votação", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (resposta == JOptionPane.YES_OPTION) {
            try {
                // Só tenta encerrar se a sessão ainda estiver aberta!
                if (votacaoService.estaEmAndamento()) {
                    votacaoService.encerrarSessao();
                }
                abrirApuracao(); // Redireciona para a tela de apuração
            } catch (RuntimeException e) {
                mostrarErro("Não foi possível encerrar a votação.", e);
            }
        }
    }
    private void processarNumero(String numero) {
        try {
            Candidato candidato = votacaoService.selecionarCandidato(numero);
            telaVotacao.mostrarCandidato(candidato);
        } catch (IllegalArgumentException e) {
            votacaoService.prepararVotoNulo();
            telaVotacao.mostrarVotoNulo(numero);
        } catch (RuntimeException e) {
            mostrarErro("Não foi possível processar o número digitado.", e);
        }
    }

    private void prepararVotoBranco() {
        try {
            votacaoService.prepararVotoBranco();
            telaVotacao.mostrarVotoBranco();
        } catch (RuntimeException e) {
            mostrarErro("Não foi possível preparar o voto em branco.", e);
        }
    }

    private void confirmarVoto() {
        try {
            votacaoService.confirmarVoto();
            if (votacaoService.estaConcluida()) {
                telaVotacao.mostrarFinalizacao();
            } else {
                atualizarTelaVotacao();
            }
        } catch (RuntimeException e) {
            mostrarErro("Não foi possível registrar o voto.", e);
        }
    }

    private void corrigirVoto() {
        try {
            votacaoService.corrigirVoto();
            telaVotacao.limparEntrada();
        } catch (RuntimeException e) {
            mostrarErro("Não foi possível corrigir a escolha.", e);
        }
    }

    private void atualizarTelaVotacao() {
        telaVotacao.atualizarCargo(votacaoService.getCargoAtual(),
                votacaoService.getNumeroCargoAtual(), votacaoService.getCargos(),
                votacaoService.listarCandidatosDoCargoAtual());
    }

    // A leitura pode ser lenta (Firebase), então roda fora da thread da interface
    private void abrirApuracao() {
        telaApuracao.mostrarCarregando();
        exibirTela(TELA_APURACAO);

        new SwingWorker<Apuracao, Void>() {
            @Override
            protected Apuracao doInBackground() {
                return apuracaoService.apurar();
            }

            @Override
            protected void done() {
                try {
                    ultimaApuracao = get();
                    telaApuracao.mostrarResultado(ultimaApuracao,
                            apuracaoService.getOrigemDados());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    telaApuracao.mostrarErro("A leitura foi interrompida.");
                } catch (ExecutionException e) {
                    Throwable causa = e.getCause() != null ? e.getCause() : e;
                    telaApuracao.mostrarErro(causa.getMessage());
                }
            }
        }.execute();
    }

    private void exportarApuracao() {
        if (ultimaApuracao == null) {
            return;
        }

        JFileChooser seletor = new JFileChooser();
        seletor.setDialogTitle("Salvar apuração em CSV");
        seletor.setSelectedFile(new File("apuracao-urna-cidada.csv"));

        if (seletor.showSaveDialog(painelPrincipal) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        try {
            // BOM no início para o Excel reconhecer os acentos (UTF-8)
            String conteudo = "\uFEFF" + ExportadorCsv.gerar(ultimaApuracao);
            Files.writeString(seletor.getSelectedFile().toPath(), conteudo, StandardCharsets.UTF_8);
            JOptionPane.showMessageDialog(painelPrincipal,
                    "Arquivo salvo em:\n" + seletor.getSelectedFile().getAbsolutePath(),
                    "Exportação concluída", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e) {
            mostrarErro("Não foi possível salvar o arquivo.", e);
        }
    }

    private void exibirTela(String nomeTela) {
        cardLayout.show(painelPrincipal, nomeTela);
    }

    private void mostrarErro(String mensagem, Exception e) {
        JOptionPane.showMessageDialog(painelPrincipal,
                mensagem + "\nDetalhes: " + e.getMessage(),
                "Erro na votação", JOptionPane.ERROR_MESSAGE);
    }
}
