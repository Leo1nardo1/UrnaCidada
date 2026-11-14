package br.edu.unijorge.urnacidada.view;

import br.edu.unijorge.urnacidada.model.Candidato;

import javax.swing.*;
import java.awt.*;

/**
 * Espaço da foto do candidato. Como os candidatos são fictícios, desenha uma
 * silhueta colorida pelo número do partido no lugar da fotografia.
 */
class FotoCandidato extends JComponent {

    private Candidato candidato;
    private String legenda = "";

    FotoCandidato() {
        setPreferredSize(new Dimension(124, 168));
        setOpaque(false);
    }

    void mostrar(Candidato candidato, String legenda) {
        this.candidato = candidato;
        this.legenda = legenda == null ? "" : legenda;
        repaint();
    }

    void limpar(String legenda) {
        mostrar(null, legenda);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int largura = getWidth() - 1;
        int alturaLegenda = 26;
        int alturaFoto = getHeight() - alturaLegenda - 1;

        Color fundoFoto = new Color(0xEE, 0xEF, 0xF1);
        Color silhueta = new Color(0xB9, 0xBE, 0xC5);
        if (candidato != null) {
            float matiz = (Integer.parseInt(candidato.getPartido().getNumero()) * 37 % 360) / 360f;
            fundoFoto = Color.getHSBColor(matiz, 0.18f, 0.97f);
            silhueta = Color.getHSBColor(matiz, 0.45f, 0.62f);
        }

        g2.setColor(fundoFoto);
        g2.fillRect(0, 0, largura, alturaFoto);

        if (candidato != null) {
            g2.setColor(silhueta);
            int cabeca = largura / 3;
            g2.fillOval((largura - cabeca) / 2, alturaFoto / 6, cabeca, cabeca);
            g2.setClip(0, 0, largura, alturaFoto);
            g2.fillOval(largura / 6, alturaFoto / 6 + cabeca + 8, largura * 2 / 3, alturaFoto);
            g2.setClip(null);
        } else {
            g2.setColor(EstiloUrna.LINHA);
            g2.setFont(EstiloUrna.fonte(Font.BOLD, 14f));
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString("FOTO", (largura - fm.stringWidth("FOTO")) / 2, alturaFoto / 2);
        }

        g2.setColor(EstiloUrna.LINHA);
        g2.drawRect(0, 0, largura, alturaFoto);

        // faixa com o nome do cargo, como na urna
        g2.setColor(new Color(0xE3, 0xE5, 0xE8));
        g2.fillRect(0, alturaFoto + 1, largura + 1, alturaLegenda);
        g2.setColor(EstiloUrna.TEXTO);
        g2.setFont(EstiloUrna.fonte(Font.BOLD, 12f));
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(legenda,
                (largura - fm.stringWidth(legenda)) / 2,
                alturaFoto + 1 + (alturaLegenda - fm.getHeight()) / 2 + fm.getAscent());
        g2.dispose();
    }
}
