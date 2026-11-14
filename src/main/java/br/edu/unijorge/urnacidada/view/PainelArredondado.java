package br.edu.unijorge.urnacidada.view;

import javax.swing.*;
import java.awt.*;

/** Painel com fundo e borda arredondados, usado para a carcaça da urna. */
class PainelArredondado extends JPanel {

    private final Color cor;
    private final Color borda;
    private final int raio;

    PainelArredondado(LayoutManager layout, Color cor, Color borda, int raio) {
        super(layout);
        this.cor = cor;
        this.borda = borda;
        this.raio = raio;
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(cor);
        g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, raio, raio);
        if (borda != null) {
            g2.setColor(borda);
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, raio, raio);
        }
        g2.dispose();
        super.paintComponent(g);
    }
}
