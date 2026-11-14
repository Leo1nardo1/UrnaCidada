package br.edu.unijorge.urnacidada.service;

import br.edu.unijorge.urnacidada.model.Apuracao;
import br.edu.unijorge.urnacidada.model.ResultadoCandidato;
import br.edu.unijorge.urnacidada.model.ResultadoCargo;

import java.util.Locale;

/**
 * Gera o CSV da apuração para usar no Relatório Final.
 * Usa ponto e vírgula e vírgula decimal, que é o formato que o Excel
 * em português abre direto.
 */
public final class ExportadorCsv {

    private static final Locale PT_BR = Locale.of("pt", "BR");

    private ExportadorCsv() {
    }

    public static String gerar(Apuracao apuracao) {

        StringBuilder csv = new StringBuilder();
        csv.append("cargo;numero;nome;partido;votos;percentual_validos;percentual_total\n");

        for (ResultadoCargo resultado : apuracao.cargos()) {

            String cargo = resultado.cargo().getNome();

            for (ResultadoCandidato linha : resultado.candidatos()) {
                csv.append(campo(cargo)).append(';')
                        .append(campo(linha.candidato().getNumero())).append(';')
                        .append(campo(linha.candidato().getNome())).append(';')
                        .append(campo(linha.candidato().getPartido().getSigla())).append(';')
                        .append(linha.votos()).append(';')
                        .append(percentual(resultado.percentualDosValidos(linha.votos()))).append(';')
                        .append(percentual(resultado.percentualDoTotal(linha.votos())))
                        .append('\n');
            }

            adicionarLinhaTotal(csv, cargo, "BRANCOS", resultado.brancos(), resultado);
            adicionarLinhaTotal(csv, cargo, "NULOS", resultado.nulos(), resultado);
            adicionarLinhaTotal(csv, cargo, "TOTAL", resultado.totalVotos(), resultado);
        }

        return csv.toString();
    }

    private static void adicionarLinhaTotal(StringBuilder csv, String cargo, String rotulo,
                                            int votos, ResultadoCargo resultado) {
        csv.append(campo(cargo)).append(";;")
                .append(rotulo).append(";;")
                .append(votos).append(";;")
                .append(percentual(resultado.percentualDoTotal(votos)))
                .append('\n');
    }

    private static String percentual(double valor) {
        return String.format(PT_BR, "%.2f", valor);
    }

    // Protege campos com ; aspas ou quebra de linha
    private static String campo(String texto) {
        if (texto.contains(";") || texto.contains("\"") || texto.contains("\n")) {
            return "\"" + texto.replace("\"", "\"\"") + "\"";
        }
        return texto;
    }
}
