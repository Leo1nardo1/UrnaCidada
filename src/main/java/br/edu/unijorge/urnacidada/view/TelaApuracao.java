package br.edu.unijorge.urnacidada.view;

import br.edu.unijorge.urnacidada.model.Apuracao;
import br.edu.unijorge.urnacidada.model.ResultadoCandidato;
import br.edu.unijorge.urnacidada.model.ResultadoCargo;
import br.edu.unijorge.urnacidada.model.TipoCargo;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Tela de apuração: mostra, para cada cargo, os votos de cada candidato,
 * brancos, nulos, total e (nos cargos majoritários) se há eleito ou segundo turno.
 */
public class TelaApuracao extends JPanel {

    private static final String CARTAO_MENSAGEM = "MENSAGEM";
    private static final String CARTAO_RESULTADO = "RESULTADO";
    private static final Locale PT_BR = Locale.of("pt", "BR");

    private Runnable acaoAtualizar;
    private Runnable acaoExportar;
    private Runnable acaoVoltar;

    private final JLabel resumoLabel = new JLabel(" ");
    private final JLabel mensagemLabel = new JLabel(" ", SwingConstants.CENTER);
    private final JPanel painelCargos = new JPanel(new GridLayout(1, 0, 16, 0));

    private final CardLayout layoutCorpo = new CardLayout();
    private final JPanel corpo = new JPanel(layoutCorpo);

    private final JButton botaoAtualizar = criarBotao("Atualizar", EstiloUrna.AZUL, Color.WHITE);
    private final JButton botaoExportar = criarBotao("Exportar CSV", Color.WHITE, EstiloUrna.AZUL);
    private final JButton botaoVoltar = criarBotao("Voltar ao início", Color.WHITE, EstiloUrna.AZUL);

    public TelaApuracao() {
        setLayout(new BorderLayout(0, 14));
        setBackground(EstiloUrna.FUNDO_JANELA);
        setBorder(new EmptyBorder(18, 22, 18, 22));

        add(criarCabecalho(), BorderLayout.NORTH);

        painelCargos.setOpaque(false);
        JScrollPane rolagem = new JScrollPane(painelCargos);
        rolagem.setBorder(null);
        rolagem.setOpaque(false);
        rolagem.getViewport().setOpaque(false);
        rolagem.getVerticalScrollBar().setUnitIncrement(16);

        mensagemLabel.setFont(EstiloUrna.fonte(Font.PLAIN, 18f));
        mensagemLabel.setForeground(EstiloUrna.TEXTO_SUAVE);

        corpo.setOpaque(false);
        corpo.add(mensagemLabel, CARTAO_MENSAGEM);
        corpo.add(rolagem, CARTAO_RESULTADO);
        add(corpo, BorderLayout.CENTER);

        add(criarRodape(), BorderLayout.SOUTH);

        botaoAtualizar.addActionListener(e -> executar(acaoAtualizar));
        botaoExportar.addActionListener(e -> executar(acaoExportar));
        botaoVoltar.addActionListener(e -> executar(acaoVoltar));
        botaoExportar.setEnabled(false);
    }

    private JPanel criarCabecalho() {
        JPanel cabecalho = new JPanel();
        cabecalho.setOpaque(false);
        cabecalho.setLayout(new BoxLayout(cabecalho, BoxLayout.Y_AXIS));

        JLabel titulo = new JLabel("APURAÇÃO");
        titulo.setFont(EstiloUrna.fonte(Font.BOLD, 28f));
        titulo.setForeground(EstiloUrna.AZUL);
        titulo.setAlignmentX(Component.LEFT_ALIGNMENT);

        resumoLabel.setFont(EstiloUrna.fonte(Font.PLAIN, 14f));
        resumoLabel.setForeground(EstiloUrna.TEXTO_SUAVE);
        resumoLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        cabecalho.add(titulo);
        cabecalho.add(Box.createVerticalStrut(4));
        cabecalho.add(resumoLabel);
        return cabecalho;
    }

    private JPanel criarRodape() {
        JPanel rodape = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rodape.setOpaque(false);
        rodape.add(botaoExportar);
        rodape.add(botaoAtualizar);
        rodape.add(botaoVoltar);
        return rodape;
    }

    private static JButton criarBotao(String texto, Color fundo, Color textoCor) {
        JButton botao = new JButton(texto);
        botao.setFont(EstiloUrna.fonte(Font.BOLD, 13f));
        botao.setBackground(fundo);
        botao.setForeground(textoCor);
        botao.setFocusPainted(false);
        botao.setBorder(new javax.swing.border.CompoundBorder(
                new LineBorder(EstiloUrna.AZUL, 1, true), new EmptyBorder(9, 18, 9, 18)));
        botao.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return botao;
    }

    private void executar(Runnable acao) {
        if (acao != null) acao.run();
    }

    // =====================================================================
    // API usada pelo controller
    // =====================================================================

    public void mostrarCarregando() {
        resumoLabel.setText(" ");
        mensagemLabel.setForeground(EstiloUrna.TEXTO_SUAVE);
        mensagemLabel.setText("Carregando votos…");
        layoutCorpo.show(corpo, CARTAO_MENSAGEM);
        botaoAtualizar.setEnabled(false);
        botaoExportar.setEnabled(false);
    }

    public void mostrarErro(String mensagem) {
        resumoLabel.setText(" ");
        mensagemLabel.setForeground(new Color(0xB4, 0x2B, 0x1E));
        mensagemLabel.setText("<html><div style='text-align:center;width:520px'>"
                + "Não foi possível apurar os votos.<br><br>" + mensagem + "</div></html>");
        layoutCorpo.show(corpo, CARTAO_MENSAGEM);
        botaoAtualizar.setEnabled(true);
        botaoExportar.setEnabled(false);
    }

    public void mostrarResultado(Apuracao apuracao, String origemDados) {

        String eleitores = apuracao.quantidadeEleitores() == 1
                ? "1 eleitor" : apuracao.quantidadeEleitores() + " eleitores";

        resumoLabel.setText(eleitores + "  \u00B7  dados de: " + origemDados
                + "  \u00B7  atualizado às "
                + apuracao.geradaEm().format(DateTimeFormatter.ofPattern("HH:mm:ss")));

        painelCargos.removeAll();
        for (ResultadoCargo resultado : apuracao.cargos()) {
            painelCargos.add(new CartaoCargo(resultado));
        }
        painelCargos.revalidate();
        painelCargos.repaint();

        layoutCorpo.show(corpo, CARTAO_RESULTADO);
        botaoAtualizar.setEnabled(true);
        botaoExportar.setEnabled(true);
    }

    public void setAcoes(Runnable acaoAtualizar, Runnable acaoExportar, Runnable acaoVoltar) {
        this.acaoAtualizar = acaoAtualizar;
        this.acaoExportar = acaoExportar;
        this.acaoVoltar = acaoVoltar;
    }

    // =====================================================================
    // Componentes internos
    // =====================================================================

    /** Cartão com o resultado de um cargo. */
    private static class CartaoCargo extends JPanel {

        CartaoCargo(ResultadoCargo resultado) {
            setLayout(new BorderLayout());
            setBackground(Color.WHITE);
            setBorder(new LineBorder(EstiloUrna.LINHA, 1, true));

            add(criarTitulo(resultado), BorderLayout.NORTH);
            add(criarLista(resultado), BorderLayout.CENTER);
            add(criarTotais(resultado), BorderLayout.SOUTH);
        }

        private JPanel criarTitulo(ResultadoCargo resultado) {
            JPanel titulo = new JPanel();
            titulo.setLayout(new BoxLayout(titulo, BoxLayout.Y_AXIS));
            titulo.setBackground(EstiloUrna.AZUL);
            titulo.setBorder(new EmptyBorder(14, 18, 14, 18));

            JLabel cargo = new JLabel(resultado.cargo().getNome());
            cargo.setFont(EstiloUrna.fonte(Font.BOLD, 22f));
            cargo.setForeground(Color.WHITE);
            cargo.setAlignmentX(Component.LEFT_ALIGNMENT);

            JLabel tipo = new JLabel(resultado.cargo().getTipo() == TipoCargo.MAJORITARIO
                    ? "Cargo majoritário" : "Cargo proporcional");
            tipo.setFont(EstiloUrna.fonte(Font.PLAIN, 12f));
            tipo.setForeground(new Color(0xD7, 0xE3, 0xF3));
            tipo.setAlignmentX(Component.LEFT_ALIGNMENT);

            titulo.add(cargo);
            titulo.add(tipo);
            return titulo;
        }

        private JComponent criarLista(ResultadoCargo resultado) {
            JPanel lista = new JPanel();
            lista.setLayout(new BoxLayout(lista, BoxLayout.Y_AXIS));
            lista.setOpaque(false);
            lista.setBorder(new EmptyBorder(14, 18, 8, 18));

            int maiorVoto = resultado.candidatos().isEmpty()
                    ? 0 : resultado.candidatos().get(0).votos();

            for (ResultadoCandidato linha : resultado.candidatos()) {
                lista.add(criarLinha(resultado, linha, maiorVoto));
                lista.add(Box.createVerticalStrut(12));
            }
            lista.add(Box.createVerticalGlue());

            JPanel envoltorio = new JPanel(new BorderLayout());
            envoltorio.setOpaque(false);
            envoltorio.add(lista, BorderLayout.NORTH);
            return envoltorio;
        }

        private JPanel criarLinha(ResultadoCargo resultado, ResultadoCandidato linha, int maiorVoto) {
            JPanel painel = new JPanel(new BorderLayout(0, 4));
            painel.setOpaque(false);
            painel.setAlignmentX(Component.LEFT_ALIGNMENT);

            JLabel nome = new JLabel("<html><b>" + linha.candidato().getNome() + "</b>"
                    + "  <span style='color:#5C6269'>" + linha.candidato().getNumero()
                    + " \u00B7 " + linha.candidato().getPartido().getSigla() + "</span></html>");
            nome.setFont(EstiloUrna.fonte(Font.PLAIN, 15f));

            double pct = resultado.percentualDosValidos(linha.votos());
            JLabel valor = new JLabel(linha.votos() + " voto" + (linha.votos() == 1 ? "" : "s")
                    + "  \u00B7  " + String.format(PT_BR, "%.1f", pct) + "%");
            valor.setFont(EstiloUrna.fonte(Font.BOLD, 13f));

            JPanel topo = new JPanel(new BorderLayout());
            topo.setOpaque(false);
            topo.add(nome, BorderLayout.WEST);
            topo.add(valor, BorderLayout.EAST);

            boolean lider = linha.votos() > 0 && linha.votos() == maiorVoto;
            BarraVotos barra = new BarraVotos(pct / 100.0, lider);

            painel.add(topo, BorderLayout.NORTH);
            painel.add(barra, BorderLayout.CENTER);
            painel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 54));
            return painel;
        }

        private JPanel criarTotais(ResultadoCargo resultado) {
            JPanel totais = new JPanel();
            totais.setLayout(new BoxLayout(totais, BoxLayout.Y_AXIS));
            totais.setBackground(new Color(0xF6, 0xF7, 0xF9));
            totais.setBorder(new javax.swing.border.CompoundBorder(
                    new MatteBorder(1, 0, 0, 0, EstiloUrna.LINHA),
                    new EmptyBorder(12, 18, 14, 18)));

            totais.add(linhaTotal("Votos em candidatos (válidos)", resultado.votosNominais(), false));
            totais.add(linhaTotal("Brancos", resultado.brancos(), false));
            totais.add(linhaTotal("Nulos", resultado.nulos(), false));
            totais.add(linhaTotal("Total", resultado.totalVotos(), true));
            totais.add(Box.createVerticalStrut(10));

            JLabel situacao = new JLabel(descreverSituacao(resultado));
            situacao.setFont(EstiloUrna.fonte(Font.PLAIN, 13f));
            situacao.setAlignmentX(Component.LEFT_ALIGNMENT);
            totais.add(situacao);
            return totais;
        }

        private JPanel linhaTotal(String rotulo, int valor, boolean destaque) {
            JPanel linha = new JPanel(new BorderLayout());
            linha.setOpaque(false);
            linha.setAlignmentX(Component.LEFT_ALIGNMENT);

            JLabel r = new JLabel(rotulo);
            JLabel v = new JLabel(String.valueOf(valor));
            int estilo = destaque ? Font.BOLD : Font.PLAIN;
            r.setFont(EstiloUrna.fonte(estilo, 14f));
            v.setFont(EstiloUrna.fonte(Font.BOLD, 14f));

            linha.add(r, BorderLayout.WEST);
            linha.add(v, BorderLayout.EAST);
            linha.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));
            return linha;
        }

        private String descreverSituacao(ResultadoCargo resultado) {
            String estilo = "<html><div style='width:300px'>";

            if (resultado.totalVotos() == 0) {
                return estilo + "Nenhum voto registrado ainda.</div></html>";
            }

            if (resultado.cargo().getTipo() == TipoCargo.PROPORCIONAL) {
                return estilo + "Na eleição real, as vagas são distribuídas pelo quociente "
                        + "eleitoral dos partidos. Aqui mostramos só os votos de cada candidato."
                        + "</div></html>";
            }

            if (resultado.votosNominais() == 0) {
                return estilo + "Só há votos brancos e nulos: não há candidato com votos válidos."
                        + "</div></html>";
            }

            return resultado.eleitoPrimeiroTurno()
                    .map(eleito -> estilo + "<b style='color:#1B7F34'>ELEITO NO 1º TURNO: "
                            + eleito.candidato().getNome() + "</b><br>Passou de 50% dos votos válidos."
                            + "</div></html>")
                    .orElse(estilo + "<b style='color:#C25A06'>SEGUNDO TURNO</b><br>"
                            + "Nenhum candidato passou de 50% dos votos válidos.</div></html>");
        }
    }

    /** Barra horizontal proporcional aos votos. */
    private static class BarraVotos extends JComponent {

        private final double fracao;
        private final boolean lider;

        BarraVotos(double fracao, boolean lider) {
            this.fracao = Math.max(0, Math.min(1, fracao));
            this.lider = lider;
            setPreferredSize(new Dimension(100, 14));
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int altura = getHeight();
            g2.setColor(new Color(0xE6, 0xE8, 0xEB));
            g2.fillRoundRect(0, 0, getWidth(), altura, altura, altura);

            int largura = (int) Math.round(getWidth() * fracao);
            if (largura > 0) {
                g2.setColor(lider ? EstiloUrna.VERDE : new Color(0x6F, 0x93, 0xC4));
                g2.fillRoundRect(0, 0, Math.max(largura, altura), altura, altura, altura);
            }
            g2.dispose();
        }
    }
}
