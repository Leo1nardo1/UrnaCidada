package br.edu.unijorge.urnacidada.view;

import java.awt.Color;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.util.Arrays;

/**
 * Paleta de cores e fontes compartilhadas pelas telas da urna.
 *
 * A urna de 2026 adotou a fonte Atkinson Hyperlegible. Se ela estiver
 * instalada no computador, é usada; caso contrário, cai para a fonte
 * sans-serif padrão do sistema.
 */
final class EstiloUrna {

    // Carcaça e teclado
    static final Color FUNDO_JANELA = new Color(0xE9, 0xEB, 0xEE);
    static final Color CARCACA = new Color(0xD9, 0xD6, 0xCE);
    static final Color CARCACA_BORDA = new Color(0xB4, 0xB0, 0xA6);
    static final Color MOLDURA_TELA = new Color(0x26, 0x29, 0x2D);
    static final Color PLACA_TECLADO = new Color(0xC9, 0xC6, 0xBD);

    // Teclas
    static final Color TECLA_NUMERO = new Color(0x1E, 0x1F, 0x21);
    static final Color TECLA_BRANCO = Color.WHITE;
    static final Color TECLA_CORRIGE = new Color(0xF3, 0x7B, 0x1B);
    static final Color TECLA_CONFIRMA = new Color(0x2F, 0xA5, 0x45);

    // Tela
    static final Color TELA = Color.WHITE;
    static final Color TEXTO = new Color(0x17, 0x1A, 0x1D);
    static final Color TEXTO_SUAVE = new Color(0x5C, 0x62, 0x69);
    static final Color LINHA = new Color(0xD3, 0xD6, 0xDA);
    static final Color AZUL = new Color(0x1F, 0x4E, 0x8C);
    static final Color VERDE = new Color(0x2F, 0xA5, 0x45);
    static final Color CINZA_ETAPA = new Color(0xDD, 0xE0, 0xE4);

    private static final String FAMILIA = escolherFamilia();

    private EstiloUrna() {
    }

    static Font fonte(int estilo, float tamanho) {
        return new Font(FAMILIA, estilo, 12).deriveFont(estilo, tamanho);
    }

    static Color misturar(Color a, Color b, double peso) {
        double p = Math.max(0, Math.min(1, peso));
        return new Color(
                (int) Math.round(a.getRed() * (1 - p) + b.getRed() * p),
                (int) Math.round(a.getGreen() * (1 - p) + b.getGreen() * p),
                (int) Math.round(a.getBlue() * (1 - p) + b.getBlue() * p)
        );
    }

    private static String escolherFamilia() {
        String[] instaladas = GraphicsEnvironment.getLocalGraphicsEnvironment()
                .getAvailableFontFamilyNames();
        boolean temAtkinson = Arrays.asList(instaladas).contains("Atkinson Hyperlegible");
        return temAtkinson ? "Atkinson Hyperlegible" : Font.SANS_SERIF;
    }
}
