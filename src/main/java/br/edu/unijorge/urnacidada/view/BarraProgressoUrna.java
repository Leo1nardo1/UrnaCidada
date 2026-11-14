package br.edu.unijorge.urnacidada.view;

import br.edu.unijorge.urnacidada.model.Cargo;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Barra de progresso fixa no topo da tela, como na urna de 2026:
 * mostra quais cargos já foram votados, qual é o atual e quais faltam.
 */
class BarraProgressoUrna extends JComponent {

    private List<Cargo> etapas = List.of();
    private int atual;

    BarraProgressoUrna() {
        setPreferredSize(new Dimension(100, 38));
        setOpaque(false);
    }

    /** @param indiceAtual posição do cargo atual (0-based); igual ao total quando tudo foi votado. */
    void atualizar(List<Cargo> etapas, int indiceAtual) {
        this.etapas = List.copyOf(etapas);
        this.atual = indiceAtual;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        if (etapas.isEmpty()) {
            return;
        }
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int espaco = 8;
        int total = etapas.size();
        int largura = (getWidth() - espaco * (total - 1)) / total;
        int altura = getHeight() - 4;

        for (int i = 0; i < total; i++) {
            int x = i * (largura + espaco);
            Color fundo;
            Color texto;
            String rotulo = etapas.get(i).getNome();

            if (i < atual) {
                fundo = EstiloUrna.VERDE;
                texto = Color.WHITE;
                rotulo = "\u2713  " + rotulo;
            } else if (i == atual) {
                fundo = EstiloUrna.AZUL;
                texto = Color.WHITE;
            } else {
                fundo = EstiloUrna.CINZA_ETAPA;
                texto = EstiloUrna.TEXTO_SUAVE;
            }

            g2.setColor(fundo);
            g2.fillRoundRect(x, 0, largura, altura, 12, 12);

            float tamanho = 14f;
            Font fonte = EstiloUrna.fonte(Font.BOLD, tamanho);
            FontMetrics fm = g2.getFontMetrics(fonte);
            while (fm.stringWidth(rotulo) > largura - 12 && tamanho > 9f) {
                tamanho -= 1f;
                fonte = EstiloUrna.fonte(Font.BOLD, tamanho);
                fm = g2.getFontMetrics(fonte);
            }
            g2.setFont(fonte);
            g2.setColor(texto);
            g2.drawString(rotulo,
                    x + (largura - fm.stringWidth(rotulo)) / 2,
                    (altura - fm.getHeight()) / 2 + fm.getAscent());
        }
        g2.dispose();
    }
}
