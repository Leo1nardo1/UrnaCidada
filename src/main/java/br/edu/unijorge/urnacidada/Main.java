package br.edu.unijorge.urnacidada;

import br.edu.unijorge.urnacidada.config.DadosSimulacao;
import br.edu.unijorge.urnacidada.controller.UrnaController;
import br.edu.unijorge.urnacidada.repository.FirestoreVotoRepository;
import br.edu.unijorge.urnacidada.repository.VotoMemoriaRepository;
import br.edu.unijorge.urnacidada.repository.VotoRepository;
import br.edu.unijorge.urnacidada.service.ApuracaoService;
import br.edu.unijorge.urnacidada.service.VotacaoService;
import br.edu.unijorge.urnacidada.view.TelaApuracao;
import br.edu.unijorge.urnacidada.view.TelaInicial;
import br.edu.unijorge.urnacidada.view.TelaVotacao;

import javax.swing.*;
import java.awt.*;

public class Main {

    public static void main(String[] args) {
        // Suaviza o texto (antialiasing) para a tela da urna ficar nítida
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");

        SwingUtilities.invokeLater(() -> {
            DadosSimulacao dadosSimulacao = new DadosSimulacao();
            VotoRepository votoRepository = criarRepositorio();
            VotacaoService votacaoService = new VotacaoService(dadosSimulacao, votoRepository);
            ApuracaoService apuracaoService = new ApuracaoService(dadosSimulacao, votoRepository);

            CardLayout cardLayout = new CardLayout();
            JPanel painelPrincipal = new JPanel(cardLayout);
            TelaInicial telaInicial = new TelaInicial();
            TelaVotacao telaVotacao = new TelaVotacao();
            TelaApuracao telaApuracao = new TelaApuracao();

            painelPrincipal.add(telaInicial, "INICIO");
            painelPrincipal.add(telaVotacao, "VOTACAO");
            painelPrincipal.add(telaApuracao, "APURACAO");

            new UrnaController(votacaoService, apuracaoService, telaInicial, telaVotacao,
                    telaApuracao, cardLayout, painelPrincipal);

            JFrame janela = new JFrame("Urna Cidadã - Simulação Educativa");
            janela.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            janela.setMinimumSize(new Dimension(1180, 720));
            janela.setSize(1280, 800);
            janela.setLocationRelativeTo(null);
            janela.setContentPane(painelPrincipal);
            janela.setVisible(true);
        });
    }

    /**
     * Usa o Firebase quando há chave configurada. Sem chave, ou se a conexão
     * falhar, a urna continua funcionando com os votos na memória.
     */
    private static VotoRepository criarRepositorio() {
        try {
            return FirestoreVotoRepository.conectar()
                    .map(repositorio -> (VotoRepository) repositorio)
                    .orElseGet(() -> {
                        System.out.println("Chave do Firebase não encontrada: usando memória local.");
                        return new VotoMemoriaRepository();
                    });
        } catch (RuntimeException | LinkageError e) {
            JOptionPane.showMessageDialog(null,
                    "Não foi possível usar o Firebase. A urna vai funcionar com os votos "
                            + "na memória (eles serão perdidos ao fechar).\n\nDetalhes: "
                            + e.getMessage(),
                    "Firebase indisponível", JOptionPane.WARNING_MESSAGE);
            return new VotoMemoriaRepository();
        }
    }
}
