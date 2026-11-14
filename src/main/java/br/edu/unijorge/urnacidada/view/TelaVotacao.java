package br.edu.unijorge.urnacidada.view;

import br.edu.unijorge.urnacidada.model.Candidato;
import br.edu.unijorge.urnacidada.model.Cargo;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Tela principal da votação, inspirada na urna eletrônica de 2026.
 *
 * Três áreas: painel lateral de candidatos (apoio didático), a tela da urna
 * (com barra de progresso fixa no topo) e o teclado com BRANCO, CORRIGE e CONFIRMA.
 * O eleitor digita, confere e confirma sem trocar de tela.
 */
public class TelaVotacao extends JPanel {

    private enum Estado { AGUARDANDO, CANDIDATO, NULO, BRANCO, FIM }

    private static final String CARTAO_VOTO = "VOTO";
    private static final String CARTAO_FIM = "FIM";
    private static final String CARTAO_ACOES = "ACOES";
    private static final String CARTAO_NOVA = "NOVA";

    // ---- estado da tela ----
    private Timer timerFinalizacao;
    private Cargo cargoAtual;
    private List<Cargo> sequenciaCargos = List.of();
    private int posicaoCargo;
    private Estado estado = Estado.AGUARDANDO;
    private Candidato candidatoExibido;
    private final StringBuilder numeroDigitado = new StringBuilder();

    // ---- ações ligadas pelo controller ----
    private Consumer<String> acaoNumeroCompleto;
    private Runnable acaoVotoBranco;
    private Runnable acaoConfirmar;
    private Runnable acaoCorrigir;
    private Runnable acaoNovaVotacao;
    private Runnable acaoEncerrarVotacao;

    // ---- componentes ----
    private final PainelCandidatos painelCandidatos = new PainelCandidatos();
    private final JButton botaoPainel = new JButton("Ocultar candidatos");
    private final JButton botaoEncerrarVotacao = new JButton("Encerrar votação");
    private final BarraProgressoUrna barraProgresso = new BarraProgressoUrna();

    private final CardLayout layoutTela = new CardLayout();
    private final JPanel areaTela = new JPanel(layoutTela);

    private final JLabel cargoLabel = new JLabel(" ");
    private final JPanel painelDigitos = new JPanel();
    private final JLabel nomeLabel = new JLabel(" ");
    private final JLabel partidoLabel = new JLabel(" ");
    private final JLabel avisoMenorLabel = new JLabel("NÚMERO SEM CANDIDATO");
    private final JLabel avisoGrandeLabel = new JLabel(" ");
    private final JLabel mensagemLabel = new JLabel(" ");
    private final FotoCandidato foto = new FotoCandidato();

    private JPanel linhaNumero;
    private JPanel linhaNome;
    private JPanel linhaPartido;

    private final CardLayout layoutAcoes = new CardLayout();
    private final JPanel areaAcoes = new JPanel(layoutAcoes);
    private final Map<String, TeclaUrna> teclasNumericas = new HashMap<>();
    private final TeclaUrna botaoBranco =
            new TeclaUrna("BRANCO", EstiloUrna.TECLA_BRANCO, EstiloUrna.TEXTO, 13f, false);
    private final TeclaUrna botaoCorrige =
            new TeclaUrna("CORRIGE", EstiloUrna.TECLA_CORRIGE, EstiloUrna.TEXTO, 13f, false);
    private final TeclaUrna botaoConfirma =
            new TeclaUrna("CONFIRMA", EstiloUrna.TECLA_CONFIRMA, EstiloUrna.TEXTO, 14f, false);
    private final TeclaUrna botaoNovaVotacao =
            new TeclaUrna("NOVA VOTAÇÃO", EstiloUrna.AZUL, Color.WHITE, 15f, false);

    public TelaVotacao() {
        setLayout(new BorderLayout(0, 12));
        setBackground(EstiloUrna.FUNDO_JANELA);
        setBorder(new EmptyBorder(14, 16, 16, 16));

        add(criarBarraSuperior(), BorderLayout.NORTH);

        JPanel corpo = new JPanel(new BorderLayout(16, 0));
        corpo.setOpaque(false);
        corpo.add(painelCandidatos, BorderLayout.WEST);
        corpo.add(criarUrna(), BorderLayout.CENTER);
        add(corpo, BorderLayout.CENTER);
        // Configura o timer para 3 segundos (3000 ms) e dispara apenas uma vez
        timerFinalizacao = new Timer(3000, e -> {
            if (acaoNovaVotacao != null) {
                acaoNovaVotacao.run();
            }
        });
        timerFinalizacao.setRepeats(false);
        configurarAcoesBotoes();
        configurarAtalhosTeclado();
        renderizar();
    }

    // =====================================================================
    // Construção da interface
    // =====================================================================

    private JPanel criarBarraSuperior() {
        JPanel barra = new JPanel(new BorderLayout(12, 0));
        barra.setOpaque(false);

        JLabel titulo = new JLabel("URNA CIDADÃ");
        titulo.setFont(EstiloUrna.fonte(Font.BOLD, 22f));
        titulo.setForeground(EstiloUrna.AZUL);

        JLabel subtitulo = new JLabel("Simulação educativa  \u00B7  candidatos e partidos fictícios");
        subtitulo.setFont(EstiloUrna.fonte(Font.PLAIN, 13f));
        subtitulo.setForeground(EstiloUrna.TEXTO_SUAVE);

        JPanel textos = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 0));
        textos.setOpaque(false);
        textos.add(titulo);
        textos.add(subtitulo);

        // Configuração dos botões
        botaoPainel.setFont(EstiloUrna.fonte(Font.PLAIN, 12f));
        botaoPainel.setFocusable(false);
        botaoPainel.addActionListener(e -> alternarPainelCandidatos());

        botaoEncerrarVotacao.setFont(EstiloUrna.fonte(Font.PLAIN, 12f));
        botaoEncerrarVotacao.setFocusable(false);
        botaoEncerrarVotacao.addActionListener(e -> {
            if (acaoEncerrarVotacao != null) acaoEncerrarVotacao.run();
        });

        // Painel para agrupar os botões à direita
        JPanel painelBotoesTopo = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        painelBotoesTopo.setOpaque(false);
        painelBotoesTopo.add(botaoEncerrarVotacao);
        painelBotoesTopo.add(botaoPainel);

        barra.add(textos, BorderLayout.CENTER);
        barra.add(painelBotoesTopo, BorderLayout.EAST);
        return barra;
    }

    private JPanel criarUrna() {
        PainelArredondado carcaca = new PainelArredondado(
                new GridBagLayout(), EstiloUrna.CARCACA, EstiloUrna.CARCACA_BORDA, 30);
        carcaca.setBorder(new EmptyBorder(20, 20, 20, 20));

        GridBagConstraints gc = new GridBagConstraints();
        gc.gridy = 0;
        gc.fill = GridBagConstraints.BOTH;
        gc.weighty = 1;

        gc.gridx = 0;
        gc.weightx = 1;
        gc.insets = new Insets(0, 0, 0, 18);
        carcaca.add(criarMoldura(), gc);

        gc.gridx = 1;
        gc.weightx = 0;
        gc.insets = new Insets(0, 0, 0, 0);
        carcaca.add(criarTeclado(), gc);
        return carcaca;
    }

    private JPanel criarMoldura() {
        PainelArredondado moldura = new PainelArredondado(
                new BorderLayout(), EstiloUrna.MOLDURA_TELA, null, 22);
        moldura.setBorder(new EmptyBorder(14, 14, 14, 14));

        JPanel tela = new JPanel(new BorderLayout());
        tela.setBackground(EstiloUrna.TELA);

        JPanel topo = new JPanel(new BorderLayout());
        topo.setOpaque(false);
        topo.setBorder(new EmptyBorder(14, 18, 10, 18));
        topo.add(barraProgresso, BorderLayout.CENTER);
        tela.add(topo, BorderLayout.NORTH);

        areaTela.setOpaque(false);
        areaTela.add(criarCartaoVoto(), CARTAO_VOTO);
        areaTela.add(criarCartaoFim(), CARTAO_FIM);
        tela.add(areaTela, BorderLayout.CENTER);

        moldura.add(tela, BorderLayout.CENTER);
        return moldura;
    }

    private JPanel criarCartaoVoto() {
        JPanel cartao = new JPanel(new BorderLayout(18, 0));
        cartao.setOpaque(false);
        cartao.setBorder(new EmptyBorder(6, 22, 0, 22));

        JPanel info = new JPanel();
        info.setOpaque(false);
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));

        JLabel seuVoto = new JLabel("SEU VOTO PARA");
        seuVoto.setFont(EstiloUrna.fonte(Font.BOLD, 13f));
        seuVoto.setForeground(EstiloUrna.TEXTO_SUAVE);
        seuVoto.setAlignmentX(Component.LEFT_ALIGNMENT);

        cargoLabel.setFont(EstiloUrna.fonte(Font.BOLD, 32f));
        cargoLabel.setForeground(EstiloUrna.TEXTO);
        cargoLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        painelDigitos.setOpaque(false);
        linhaNumero = criarLinha("Número:", painelDigitos, 66);

        nomeLabel.setFont(EstiloUrna.fonte(Font.BOLD, 22f));
        nomeLabel.setForeground(EstiloUrna.TEXTO);
        linhaNome = criarLinha("Nome:", nomeLabel, 36);

        partidoLabel.setFont(EstiloUrna.fonte(Font.BOLD, 18f));
        partidoLabel.setForeground(EstiloUrna.TEXTO);
        linhaPartido = criarLinha("Partido:", partidoLabel, 32);

        avisoMenorLabel.setFont(EstiloUrna.fonte(Font.BOLD, 20f));
        avisoMenorLabel.setForeground(new Color(0xB4, 0x2B, 0x1E));
        avisoMenorLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        avisoGrandeLabel.setFont(EstiloUrna.fonte(Font.BOLD, 32f));
        avisoGrandeLabel.setForeground(EstiloUrna.TEXTO);
        avisoGrandeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        info.add(seuVoto);
        info.add(Box.createVerticalStrut(2));
        info.add(cargoLabel);
        info.add(Box.createVerticalStrut(22));
        info.add(linhaNumero);
        info.add(Box.createVerticalStrut(16));
        info.add(linhaNome);
        info.add(Box.createVerticalStrut(8));
        info.add(linhaPartido);
        info.add(Box.createVerticalStrut(14));
        info.add(avisoMenorLabel);
        info.add(Box.createVerticalStrut(4));
        info.add(avisoGrandeLabel);
        info.add(Box.createVerticalGlue());

        JPanel colunaFoto = new JPanel(new BorderLayout());
        colunaFoto.setOpaque(false);
        colunaFoto.add(foto, BorderLayout.NORTH);

        cartao.add(info, BorderLayout.CENTER);
        cartao.add(colunaFoto, BorderLayout.EAST);
        cartao.add(criarRodapeTela(), BorderLayout.SOUTH);
        return cartao;
    }

    private JPanel criarLinha(String rotulo, JComponent valor, int alturaMaxima) {
        JPanel linha = new JPanel();
        linha.setLayout(new BoxLayout(linha, BoxLayout.X_AXIS));
        linha.setOpaque(false);
        linha.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel rotuloLabel = new JLabel(rotulo);
        rotuloLabel.setFont(EstiloUrna.fonte(Font.PLAIN, 17f));
        rotuloLabel.setForeground(EstiloUrna.TEXTO_SUAVE);
        rotuloLabel.setPreferredSize(new Dimension(84, 30));
        rotuloLabel.setMinimumSize(new Dimension(84, 30));
        rotuloLabel.setMaximumSize(new Dimension(84, alturaMaxima));

        linha.add(rotuloLabel);
        linha.add(Box.createHorizontalStrut(8));
        linha.add(valor);
        linha.add(Box.createHorizontalGlue());
        linha.setMaximumSize(new Dimension(Integer.MAX_VALUE, alturaMaxima));
        return linha;
    }

    private JPanel criarRodapeTela() {
        JPanel rodape = new JPanel();
        rodape.setOpaque(false);
        rodape.setLayout(new BoxLayout(rodape, BoxLayout.Y_AXIS));
        rodape.setBorder(new javax.swing.border.CompoundBorder(
                new MatteBorder(1, 0, 0, 0, EstiloUrna.LINHA),
                new EmptyBorder(10, 0, 14, 0)));

        mensagemLabel.setFont(EstiloUrna.fonte(Font.ITALIC, 12f));
        mensagemLabel.setForeground(EstiloUrna.AZUL);
        mensagemLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel instrucao = new JLabel("Aperte a tecla:");
        instrucao.setFont(EstiloUrna.fonte(Font.BOLD, 14f));
        instrucao.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel confirma = new JLabel("<html><b style='color:#1B7F34'>CONFIRMA</b> para "
                + "<b>CONFIRMAR</b> este voto</html>");
        confirma.setFont(EstiloUrna.fonte(Font.PLAIN, 13f));
        confirma.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel corrige = new JLabel("<html><b style='color:#C25A06'>CORRIGE</b> para "
                + "<b>REINICIAR</b> este voto</html>");
        corrige.setFont(EstiloUrna.fonte(Font.PLAIN, 13f));
        corrige.setAlignmentX(Component.LEFT_ALIGNMENT);

        rodape.add(mensagemLabel);
        rodape.add(Box.createVerticalStrut(8));
        rodape.add(instrucao);
        rodape.add(Box.createVerticalStrut(3));
        rodape.add(confirma);
        rodape.add(Box.createVerticalStrut(2));
        rodape.add(corrige);
        return rodape;
    }

    private JPanel criarCartaoFim() {
        JPanel cartao = new JPanel(new GridBagLayout());
        cartao.setOpaque(false);

        JLabel fim = new JLabel("FIM");
        fim.setFont(EstiloUrna.fonte(Font.BOLD, 120f));
        fim.setForeground(EstiloUrna.TEXTO);

        JLabel votou = new JLabel("VOTOU");
        votou.setFont(EstiloUrna.fonte(Font.BOLD, 20f));
        votou.setForeground(EstiloUrna.TEXTO_SUAVE);

        JLabel obrigado = new JLabel("Obrigado por participar da Urna Cidadã!");
        obrigado.setFont(EstiloUrna.fonte(Font.PLAIN, 14f));
        obrigado.setForeground(EstiloUrna.TEXTO_SUAVE);

        JLabel avisoTimer = new JLabel("Uma nova votação começará em instantes...");
        avisoTimer.setFont(EstiloUrna.fonte(Font.ITALIC, 12f));
        avisoTimer.setForeground(new Color(0x8A, 0x90, 0x97));

        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = 0;
        gc.gridy = 0;
        cartao.add(fim, gc);
        gc.gridy = 1;
        cartao.add(votou, gc);
        gc.gridy = 2;
        gc.insets = new Insets(18, 0, 0, 0);
        cartao.add(obrigado, gc);

        gc.gridy = 3; // Adiciona o aviso do timer
        gc.insets = new Insets(4, 0, 0, 0);
        cartao.add(avisoTimer, gc);
        return cartao;
    }

    private JPanel criarTeclado() {
        PainelArredondado teclado = new PainelArredondado(
                new BorderLayout(0, 16), EstiloUrna.PLACA_TECLADO, EstiloUrna.CARCACA_BORDA, 24);
        teclado.setBorder(new EmptyBorder(18, 18, 18, 18));
        teclado.setPreferredSize(new Dimension(290, 100));

        JLabel marca = new JLabel("<html><div style='text-align:center'>"
                + "<span style='font-size:16px'><b>URNA CIDADÃ</b></span><br>"
                + "<span style='font-size:9px'>SIMULAÇÃO EDUCATIVA</span></div></html>",
                SwingConstants.CENTER);
        marca.setFont(EstiloUrna.fonte(Font.BOLD, 12f));
        marca.setForeground(EstiloUrna.TEXTO);
        teclado.add(marca, BorderLayout.NORTH);

        JPanel numeros = new JPanel(new GridLayout(4, 3, 10, 10));
        numeros.setOpaque(false);
        for (int n = 1; n <= 9; n++) {
            numeros.add(criarTeclaNumero(String.valueOf(n)));
        }
        numeros.add(new JLabel());
        numeros.add(criarTeclaNumero("0"));
        numeros.add(new JLabel());
        teclado.add(numeros, BorderLayout.CENTER);

        areaAcoes.setOpaque(false);
        areaAcoes.setPreferredSize(new Dimension(100, 76));
        areaAcoes.add(criarLinhaAcoes(), CARTAO_ACOES);
        areaAcoes.add(botaoNovaVotacao, CARTAO_NOVA);
        teclado.add(areaAcoes, BorderLayout.SOUTH);
        return teclado;
    }

    private TeclaUrna criarTeclaNumero(String digito) {
        TeclaUrna tecla = new TeclaUrna(digito, EstiloUrna.TECLA_NUMERO, Color.WHITE, 26f, digito.equals("5"));
        tecla.addActionListener(e -> adicionarDigito(digito));
        teclasNumericas.put(digito, tecla);
        return tecla;
    }

    private JPanel criarLinhaAcoes() {
        JPanel linha = new JPanel(new GridBagLayout());
        linha.setOpaque(false);
        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.BOTH;
        gc.weighty = 1;
        gc.gridy = 0;

        gc.gridx = 0;
        gc.weightx = 1;
        gc.insets = new Insets(0, 0, 0, 8);
        linha.add(botaoBranco, gc);

        gc.gridx = 1;
        linha.add(botaoCorrige, gc);

        gc.gridx = 2;
        gc.weightx = 1.3;
        gc.insets = new Insets(0, 0, 0, 0);
        linha.add(botaoConfirma, gc);
        return linha;
    }

    // =====================================================================
    // Ações e atalhos
    // =====================================================================

    private void configurarAcoesBotoes() {
        botaoBranco.addActionListener(e -> votarEmBranco());
        botaoCorrige.addActionListener(e -> corrigir());
        botaoConfirma.addActionListener(e -> {
            if (acaoConfirmar != null) acaoConfirmar.run();
        });
        botaoNovaVotacao.addActionListener(e -> {
            if (timerFinalizacao != null && timerFinalizacao.isRunning()) {
                timerFinalizacao.stop();
            }
            if (acaoNovaVotacao != null) acaoNovaVotacao.run();
        });
    }

    /** Permite usar o teclado do computador: dígitos, Enter (confirma), Backspace/Esc (corrige) e B (branco). */
    private void configurarAtalhosTeclado() {
        InputMap entradas = getInputMap(WHEN_IN_FOCUSED_WINDOW);
        ActionMap acoes = getActionMap();

        for (int d = 0; d <= 9; d++) {
            String digito = String.valueOf(d);
            String chave = "digito" + d;
            entradas.put(KeyStroke.getKeyStroke(KeyEvent.VK_0 + d, 0), chave);
            entradas.put(KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD0 + d, 0), chave);
            acoes.put(chave, new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    teclasNumericas.get(digito).doClick(60);
                }
            });
        }

        registrarAtalho(entradas, acoes, KeyEvent.VK_ENTER, "confirma", botaoConfirma);
        registrarAtalho(entradas, acoes, KeyEvent.VK_BACK_SPACE, "corrige", botaoCorrige);
        registrarAtalho(entradas, acoes, KeyEvent.VK_ESCAPE, "corrige", botaoCorrige);
        registrarAtalho(entradas, acoes, KeyEvent.VK_B, "branco", botaoBranco);
    }

    private void registrarAtalho(InputMap entradas, ActionMap acoes, int tecla, String chave, TeclaUrna botao) {
        entradas.put(KeyStroke.getKeyStroke(tecla, 0), chave);
        if (acoes.get(chave) == null) {
            acoes.put(chave, new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (botao.isShowing()) botao.doClick(60);
                }
            });
        }
    }

    private void alternarPainelCandidatos() {
        boolean visivel = !painelCandidatos.isVisible();
        painelCandidatos.setVisible(visivel);
        botaoPainel.setText(visivel ? "Ocultar candidatos" : "Mostrar candidatos");
        revalidate();
        repaint();
    }

    private void adicionarDigito(String digito) {
        if (cargoAtual == null || estado != Estado.AGUARDANDO
                || numeroDigitado.length() >= cargoAtual.getQuantidadeDigitos()) {
            return;
        }
        numeroDigitado.append(digito);
        if (numeroDigitado.length() == cargoAtual.getQuantidadeDigitos() && acaoNumeroCompleto != null) {
            acaoNumeroCompleto.accept(numeroDigitado.toString());
        }
        renderizar();
    }

    private void votarEmBranco() {
        if (cargoAtual == null || estado == Estado.FIM) return;
        if (numeroDigitado.length() > 0) {
            mensagemLabel.setText("Pressione CORRIGE antes de votar em branco.");
            return;
        }
        if (acaoVotoBranco != null) acaoVotoBranco.run();
    }

    private void corrigir() {
        if (estado == Estado.FIM) return;
        if (acaoCorrigir != null) acaoCorrigir.run();
        else limparEntrada();
    }

    // =====================================================================
    // API usada pelo controller
    // =====================================================================

    /** Prepara a tela para um novo cargo e atualiza a barra de progresso e o painel de candidatos. */
    public void atualizarCargo(Cargo cargo, int numeroCargo, List<Cargo> sequencia,
                               List<Candidato> candidatosDoCargo) {
        cargoAtual = cargo;
        sequenciaCargos = List.copyOf(sequencia);
        posicaoCargo = numeroCargo - 1;
        painelCandidatos.exibir(cargo, candidatosDoCargo);
        numeroDigitado.setLength(0);
        candidatoExibido = null;
        estado = Estado.AGUARDANDO;
        renderizar();
    }

    public void mostrarCandidato(Candidato candidato) {
        candidatoExibido = candidato;
        estado = Estado.CANDIDATO;
        renderizar();
    }

    public void mostrarVotoNulo(String numero) {
        candidatoExibido = null;
        estado = Estado.NULO;
        renderizar();
    }

    public void mostrarVotoBranco() {
        candidatoExibido = null;
        estado = Estado.BRANCO;
        renderizar();
    }

    public void limparEntrada() {
        numeroDigitado.setLength(0);
        candidatoExibido = null;
        if (estado != Estado.FIM) estado = Estado.AGUARDANDO;
        renderizar();
    }

    public void mostrarFinalizacao() {
        numeroDigitado.setLength(0);
        candidatoExibido = null;
        estado = Estado.FIM;
        posicaoCargo = sequenciaCargos.size();
        renderizar();
        if (timerFinalizacao != null) {
            timerFinalizacao.restart();
        }
    }

    public void setAcoes(Consumer<String> acaoNumeroCompleto, Runnable acaoVotoBranco,
                         Runnable acaoConfirmar, Runnable acaoCorrigir, Runnable acaoNovaVotacao,
                         Runnable acaoEncerrarVotacao) {
        this.acaoNumeroCompleto = acaoNumeroCompleto;
        this.acaoVotoBranco = acaoVotoBranco;
        this.acaoConfirmar = acaoConfirmar;
        this.acaoCorrigir = acaoCorrigir;
        this.acaoNovaVotacao = acaoNovaVotacao;
        this.acaoEncerrarVotacao = acaoEncerrarVotacao;
    }

    // =====================================================================
    // Renderização
    // =====================================================================

    /** Redesenha toda a tela a partir do estado atual. */
    private void renderizar() {
        boolean fim = estado == Estado.FIM;
        if (!fim && timerFinalizacao != null && timerFinalizacao.isRunning()) {
            timerFinalizacao.stop();
        }

        layoutTela.show(areaTela, fim ? CARTAO_FIM : CARTAO_VOTO);
        layoutTela.show(areaTela, fim ? CARTAO_FIM : CARTAO_VOTO);
        layoutAcoes.show(areaAcoes, fim ? CARTAO_NOVA : CARTAO_ACOES);

        barraProgresso.atualizar(sequenciaCargos, posicaoCargo);

        if (cargoAtual != null && !fim) {
            boolean branco = estado == Estado.BRANCO;
            boolean nulo = estado == Estado.NULO;
            boolean candidato = estado == Estado.CANDIDATO;

            cargoLabel.setText(cargoAtual.getNome());
            linhaNumero.setVisible(!branco);
            linhaNome.setVisible(!branco && !nulo);
            linhaPartido.setVisible(!branco && !nulo);
            avisoMenorLabel.setVisible(nulo);
            avisoGrandeLabel.setVisible(branco || nulo);
            avisoGrandeLabel.setText(branco ? "VOTO EM BRANCO" : "VOTO NULO");

            nomeLabel.setText(candidato ? candidatoExibido.getNome() : " ");
            partidoLabel.setText(candidato
                    ? candidatoExibido.getPartido().getSigla()
                    + " \u2013 " + candidatoExibido.getPartido().getNome()
                    : " ");

            if (candidato) {
                foto.mostrar(candidatoExibido, cargoAtual.getNome());
            } else {
                foto.limpar(cargoAtual.getNome());
            }
            mensagemLabel.setText(mensagemDoEstado());
            atualizarDigitos();
        }

        painelCandidatos.destacar(fim ? "" : numeroDigitado.toString());
        atualizarBotoes();
        revalidate();
        repaint();
    }

    private String mensagemDoEstado() {
        return switch (estado) {
            case AGUARDANDO -> "Digite o número do candidato.";
            case CANDIDATO -> "Confira os dados e pressione CONFIRMA.";
            case NULO -> "Número sem candidato. CONFIRMA registra voto nulo; CORRIGE reinicia.";
            case BRANCO -> "CONFIRMA registra o voto em branco; CORRIGE reinicia.";
            case FIM -> " ";
        };
    }

    private void atualizarDigitos() {
        painelDigitos.removeAll();
        painelDigitos.setLayout(new GridLayout(1, cargoAtual.getQuantidadeDigitos(), 6, 0));
        for (int i = 0; i < cargoAtual.getQuantidadeDigitos(); i++) {
            boolean preenchido = i < numeroDigitado.length();
            boolean proximo = estado == Estado.AGUARDANDO && i == numeroDigitado.length();

            JLabel caixa = new JLabel(preenchido ? String.valueOf(numeroDigitado.charAt(i)) : "",
                    SwingConstants.CENTER);
            caixa.setFont(EstiloUrna.fonte(Font.BOLD, 34f));
            caixa.setForeground(EstiloUrna.TEXTO);
            caixa.setOpaque(true);
            caixa.setBackground(Color.WHITE);
            caixa.setBorder(new LineBorder(proximo ? EstiloUrna.AZUL : new Color(0x8A, 0x90, 0x97),
                    proximo ? 3 : 2));
            caixa.setPreferredSize(new Dimension(46, 60));
            painelDigitos.add(caixa);
        }
        painelDigitos.revalidate();
        painelDigitos.repaint();
    }

    private void atualizarBotoes() {
        boolean ativo = cargoAtual != null && estado != Estado.FIM;
        boolean aguardando = ativo && estado == Estado.AGUARDANDO;
        int digitos = numeroDigitado.length();

        for (TeclaUrna tecla : teclasNumericas.values()) {
            tecla.setEnabled(aguardando && digitos < cargoAtual.getQuantidadeDigitos());
        }
        botaoBranco.setEnabled(aguardando && digitos == 0);
        botaoCorrige.setEnabled(ativo && (digitos > 0 || estado != Estado.AGUARDANDO));
        botaoConfirma.setEnabled(ativo && estado != Estado.AGUARDANDO);
    }
}
