package br.edu.unijorge.urnacidada.view;

import br.edu.unijorge.urnacidada.model.Candidato;
import br.edu.unijorge.urnacidada.model.Cargo;
import br.edu.unijorge.urnacidada.model.TipoCargo;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Painel lateral com os candidatos do cargo em votação e seus números.
 * Enquanto o eleitor digita, as linhas compatíveis com os dígitos já
 * digitados são destacadas.
 *
 * Na urna real não existe esta lista (o eleitor leva o número anotado);
 * ela é um apoio didático da simulação.
 */
class PainelCandidatos extends JPanel {

    private enum Destaque { NORMAL, ESMAECIDO, PARCIAL, EXATO }

    /** Lista que acompanha a largura do painel (só rola na vertical). */
    private static class ListaRolavel extends JPanel implements Scrollable {
        @Override
        public Dimension getPreferredScrollableViewportSize() {
            return getPreferredSize();
        }

        @Override
        public int getScrollableUnitIncrement(Rectangle area, int orientacao, int direcao) {
            return 16;
        }

        @Override
        public int getScrollableBlockIncrement(Rectangle area, int orientacao, int direcao) {
            return 64;
        }

        @Override
        public boolean getScrollableTracksViewportWidth() {
            return true;
        }

        @Override
        public boolean getScrollableTracksViewportHeight() {
            return false;
        }
    }

    private final JLabel cargoLabel = new JLabel(" ");
    private final JLabel regraLabel = new JLabel(" ");
    private final JPanel lista = new ListaRolavel();
    private final List<LinhaCandidato> linhas = new ArrayList<>();

    PainelCandidatos() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setBorder(new LineBorder(EstiloUrna.LINHA, 1, true));
        setPreferredSize(new Dimension(292, 100));

        add(criarCabecalho(), BorderLayout.NORTH);

        lista.setLayout(new BoxLayout(lista, BoxLayout.Y_AXIS));
        lista.setBackground(Color.WHITE);
        lista.setBorder(new EmptyBorder(10, 10, 10, 10));

        JScrollPane rolagem = new JScrollPane(lista,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        rolagem.setBorder(null);
        rolagem.getViewport().setBackground(Color.WHITE);
        rolagem.getVerticalScrollBar().setUnitIncrement(16);
        add(rolagem, BorderLayout.CENTER);

        add(criarRodape(), BorderLayout.SOUTH);
    }

    private JPanel criarCabecalho() {
        JPanel cabecalho = new JPanel();
        cabecalho.setLayout(new BoxLayout(cabecalho, BoxLayout.Y_AXIS));
        cabecalho.setBackground(EstiloUrna.AZUL);
        cabecalho.setBorder(new EmptyBorder(14, 16, 14, 16));

        JLabel titulo = new JLabel("CANDIDATOS");
        titulo.setFont(EstiloUrna.fonte(Font.BOLD, 12f));
        titulo.setForeground(new Color(0xBF, 0xD3, 0xEC));

        cargoLabel.setFont(EstiloUrna.fonte(Font.BOLD, 22f));
        cargoLabel.setForeground(Color.WHITE);

        regraLabel.setFont(EstiloUrna.fonte(Font.PLAIN, 12f));
        regraLabel.setForeground(new Color(0xD7, 0xE3, 0xF3));

        titulo.setAlignmentX(Component.LEFT_ALIGNMENT);
        cargoLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        regraLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        cabecalho.add(titulo);
        cabecalho.add(Box.createVerticalStrut(2));
        cabecalho.add(cargoLabel);
        cabecalho.add(Box.createVerticalStrut(4));
        cabecalho.add(regraLabel);
        return cabecalho;
    }

    private JComponent criarRodape() {
        JTextArea nota = new JTextArea(
                "Apoio da simulação: na urna de verdade esta lista não aparece. "
                        + "O eleitor deve levar o número do candidato já escolhido.");
        nota.setEditable(false);
        nota.setFocusable(false);
        nota.setLineWrap(true);
        nota.setWrapStyleWord(true);
        nota.setOpaque(false);
        nota.setFont(EstiloUrna.fonte(Font.PLAIN, 11f));
        nota.setForeground(EstiloUrna.TEXTO_SUAVE);
        nota.setBorder(new CompoundBorder(
                new javax.swing.border.MatteBorder(1, 0, 0, 0, EstiloUrna.LINHA),
                new EmptyBorder(10, 16, 12, 16)));
        return nota;
    }

    /** Mostra os candidatos do cargo, ordenados pelo número. */
    void exibir(Cargo cargo, List<Candidato> candidatos) {
        cargoLabel.setText(cargo.getNome());
        regraLabel.setText(descreverRegra(cargo));

        lista.removeAll();
        linhas.clear();

        List<Candidato> ordenados = new ArrayList<>(candidatos);
        ordenados.sort(Comparator.comparing(Candidato::getNumero));

        for (Candidato candidato : ordenados) {
            LinhaCandidato linha = new LinhaCandidato(candidato);
            linhas.add(linha);
            lista.add(linha);
            lista.add(Box.createVerticalStrut(8));
        }
        lista.add(Box.createVerticalGlue());
        lista.revalidate();
        lista.repaint();
    }

    /** Destaca os candidatos cujo número começa pelos dígitos já digitados. */
    void destacar(String digitado) {
        for (LinhaCandidato linha : linhas) {
            if (digitado == null || digitado.isEmpty()) {
                linha.aplicar(Destaque.NORMAL);
            } else if (linha.numero.equals(digitado)) {
                linha.aplicar(Destaque.EXATO);
            } else if (linha.numero.startsWith(digitado)) {
                linha.aplicar(Destaque.PARCIAL);
            } else {
                linha.aplicar(Destaque.ESMAECIDO);
            }
        }
    }

    private String descreverRegra(Cargo cargo) {
        int digitos = cargo.getQuantidadeDigitos();
        if (cargo.getTipo() == TipoCargo.PROPORCIONAL) {
            return digitos + " dígitos: 2 do partido + " + (digitos - 2) + " do candidato";
        }
        return digitos + " dígitos: o número do partido";
    }

    // ------------------------------------------------------------------

    private static class LinhaCandidato extends JPanel {

        private final String numero;
        private final JLabel numeroLabel;
        private final JLabel nomeLabel;
        private final JLabel partidoLabel;

        LinhaCandidato(Candidato candidato) {
            super(new BorderLayout(10, 0));
            this.numero = candidato.getNumero();

            numeroLabel = new JLabel(formatarNumero(numero), SwingConstants.CENTER);
            numeroLabel.setFont(EstiloUrna.fonte(Font.BOLD, 22f));
            numeroLabel.setOpaque(true);
            numeroLabel.setBackground(new Color(0xF3, 0xF4, 0xF6));
            numeroLabel.setBorder(new CompoundBorder(
                    new LineBorder(new Color(0xC4, 0xC8, 0xCE), 1, true),
                    new EmptyBorder(6, 6, 6, 6)));
            numeroLabel.setPreferredSize(new Dimension(76, 46));

            nomeLabel = new JLabel(candidato.getNome());
            nomeLabel.setFont(EstiloUrna.fonte(Font.BOLD, 16f));

            partidoLabel = new JLabel(candidato.getPartido().getSigla()
                    + " \u2013 " + candidato.getPartido().getNome());
            partidoLabel.setFont(EstiloUrna.fonte(Font.PLAIN, 11f));

            JPanel textos = new JPanel();
            textos.setOpaque(false);
            textos.setLayout(new BoxLayout(textos, BoxLayout.Y_AXIS));
            textos.add(Box.createVerticalGlue());
            textos.add(nomeLabel);
            textos.add(partidoLabel);
            textos.add(Box.createVerticalGlue());

            add(numeroLabel, BorderLayout.WEST);
            add(textos, BorderLayout.CENTER);

            setAlignmentX(Component.LEFT_ALIGNMENT);
            aplicar(Destaque.NORMAL);
            setMaximumSize(new Dimension(Integer.MAX_VALUE, getPreferredSize().height));
        }

        private static String formatarNumero(String numero) {
            // Deputados: separa os 2 dígitos do partido dos 2 do candidato.
            return numero.length() == 4
                    ? numero.substring(0, 2) + " " + numero.substring(2)
                    : numero;
        }

        void aplicar(Destaque destaque) {
            Color fundo = Color.WHITE;
            Color borda = new Color(0xE1, 0xE4, 0xE8);
            Color textoPrincipal = EstiloUrna.TEXTO;
            Color textoSecundario = EstiloUrna.TEXTO_SUAVE;

            switch (destaque) {
                case PARCIAL -> {
                    fundo = new Color(0xFF, 0xF5, 0xD9);
                    borda = new Color(0xE9, 0xB6, 0x3F);
                }
                case EXATO -> {
                    fundo = new Color(0xDD, 0xF4, 0xE3);
                    borda = EstiloUrna.VERDE;
                }
                case ESMAECIDO -> {
                    textoPrincipal = new Color(0xA2, 0xA7, 0xAE);
                    textoSecundario = new Color(0xB5, 0xB9, 0xBF);
                }
                default -> { }
            }

            setBackground(fundo);
            setBorder(new CompoundBorder(
                    new LineBorder(borda, destaque == Destaque.EXATO ? 2 : 1, true),
                    new EmptyBorder(8, 8, 8, 8)));
            nomeLabel.setForeground(textoPrincipal);
            numeroLabel.setForeground(textoPrincipal);
            partidoLabel.setForeground(textoSecundario);
            repaint();
        }
    }
}
