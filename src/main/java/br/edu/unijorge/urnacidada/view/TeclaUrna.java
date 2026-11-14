package br.edu.unijorge.urnacidada.view;

import javax.swing.*;
import java.awt.*;

/** Tecla do teclado da urna: desenhada manualmente para imitar as teclas físicas. */
class TeclaUrna extends JButton {

    private final Color fundo;
    private final Color corTexto;
    private final boolean pontoTatil;

    TeclaUrna(String texto, Color fundo, Color corTexto, float tamanhoFonte, boolean pontoTatil) {
        super(texto);
        this.fundo = fundo;
        this.corTexto = corTexto;
        this.pontoTatil = pontoTatil;
        setFont(EstiloUrna.fonte(Font.BOLD, tamanhoFonte));
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setFocusable(false);
        setOpaque(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        boolean ativa = isEnabled();
        boolean pressionada = ativa && getModel().isPressed();

        int largura = getWidth() - 2;
        int altura = getHeight() - 5;
        int deslocamento = pressionada ? 2 : 0;

        // sombra da tecla
        g2.setColor(new Color(0, 0, 0, 70));
        g2.fillRoundRect(1, 4, largura, altura, 16, 16);

        Color base = ativa ? fundo : EstiloUrna.misturar(fundo, EstiloUrna.PLACA_TECLADO, 0.55);
        if (ativa && getModel().isRollover() && !pressionada) {
            base = EstiloUrna.misturar(base, Color.WHITE, 0.12);
        }
        g2.setColor(base);
        g2.fillRoundRect(0, deslocamento, largura, altura, 16, 16);

        Color texto = ativa ? corTexto : EstiloUrna.misturar(corTexto, base, 0.55);
        g2.setColor(texto);
        g2.setFont(getFont());
        FontMetrics fm = g2.getFontMetrics();
        int x = (largura - fm.stringWidth(getText())) / 2;
        int y = deslocamento + (altura - fm.getHeight()) / 2 + fm.getAscent();
        g2.drawString(getText(), x, y);

        if (pontoTatil) {
            // O número 5 da urna real tem um ponto em relevo para orientação tátil.
            g2.fillOval(largura / 2 - 3, deslocamento + altura - 13, 6, 6);
        }
        g2.dispose();
    }
}
