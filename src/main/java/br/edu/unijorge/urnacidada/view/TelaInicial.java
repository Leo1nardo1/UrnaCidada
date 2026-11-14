package br.edu.unijorge.urnacidada.view;

import javax.swing.*;
import java.awt.*;

public class TelaInicial extends JPanel {

    private Runnable acaoIniciar;
    private Runnable acaoApuracao;
    private final JLabel armazenamentoLabel = new JLabel(" ");

    public TelaInicial() {

        setLayout(new BorderLayout());
        setBackground(new Color(235, 238, 242));

        JPanel conteudo = new JPanel();
        conteudo.setLayout(new BoxLayout(conteudo, BoxLayout.Y_AXIS));
        conteudo.setBackground(Color.WHITE);
        conteudo.setBorder(
                BorderFactory.createEmptyBorder(55, 70, 55, 70)
        );

        JLabel titulo = new JLabel("URNA CIDADÃ");
        titulo.setFont(new Font("Arial", Font.BOLD, 38));
        titulo.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitulo = new JLabel(
                "Simulação Educativa do Voto Eletrônico"
        );
        subtitulo.setFont(new Font("Arial", Font.PLAIN, 18));
        subtitulo.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel descricao = new JLabel(
                "<html><div style='text-align:center;'>"
                        + "Participe de uma simulação educativa "
                        + "do processo de votação brasileiro."
                        + "</div></html>"
        );

        descricao.setFont(new Font("Arial", Font.PLAIN, 15));
        descricao.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel cargos = new JLabel(
                "Cargos simulados: Deputado Federal e Presidente"
        );

        cargos.setFont(new Font("Arial", Font.BOLD, 14));
        cargos.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel aviso = new JLabel(
                "<html><div style='text-align:center;'>"
                        + "Candidatos e partidos fictícios. "
                        + "Esta é uma simulação simplificada, "
                        + "não uma eleição oficial."
                        + "</div></html>"
        );

        aviso.setFont(new Font("Arial", Font.ITALIC, 12));
        aviso.setForeground(Color.GRAY);
        aviso.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton botaoIniciar = new JButton("INICIAR VOTAÇÃO");

        botaoIniciar.setFont(new Font("Arial", Font.BOLD, 17));
        botaoIniciar.setBackground(new Color(35, 90, 150));
        botaoIniciar.setForeground(Color.WHITE);
        botaoIniciar.setFocusPainted(false);
        botaoIniciar.setAlignmentX(Component.CENTER_ALIGNMENT);
        botaoIniciar.setMaximumSize(new Dimension(280, 55));

        botaoIniciar.addActionListener(e -> {

            if (acaoIniciar != null) {
                acaoIniciar.run();
            }
        });

        conteudo.add(titulo);
        conteudo.add(Box.createVerticalStrut(15));
        conteudo.add(subtitulo);
        conteudo.add(Box.createVerticalStrut(40));
        conteudo.add(descricao);
        conteudo.add(Box.createVerticalStrut(20));
        conteudo.add(cargos);
        conteudo.add(Box.createVerticalStrut(15));
        conteudo.add(aviso);
        JButton botaoApuracao = new JButton("VER APURAÇÃO");

        botaoApuracao.setFont(new Font("Arial", Font.BOLD, 13));
        botaoApuracao.setBackground(Color.WHITE);
        botaoApuracao.setForeground(new Color(35, 90, 150));
        botaoApuracao.setFocusPainted(false);
        botaoApuracao.setAlignmentX(Component.CENTER_ALIGNMENT);
        botaoApuracao.setMaximumSize(new Dimension(280, 42));

        botaoApuracao.addActionListener(e -> {

            if (acaoApuracao != null) {
                acaoApuracao.run();
            }
        });

        armazenamentoLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        armazenamentoLabel.setForeground(Color.GRAY);
        armazenamentoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        conteudo.add(Box.createVerticalGlue());
        conteudo.add(botaoIniciar);
        conteudo.add(Box.createVerticalStrut(12));
        conteudo.add(botaoApuracao);
        conteudo.add(Box.createVerticalStrut(18));
        conteudo.add(armazenamentoLabel);

        add(conteudo, BorderLayout.CENTER);
    }

    public void setAcaoIniciar(Runnable acaoIniciar) {
        this.acaoIniciar = acaoIniciar;
    }

    public void setAcaoApuracao(Runnable acaoApuracao) {
        this.acaoApuracao = acaoApuracao;
    }

    public void setModoArmazenamento(String descricao) {
        armazenamentoLabel.setText("Votos guardados em: " + descricao);
    }
}
