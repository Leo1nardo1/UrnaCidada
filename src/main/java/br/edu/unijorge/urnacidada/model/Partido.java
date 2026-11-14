
package br.edu.unijorge.urnacidada.model;

public class Partido {

    private final String id;
    private final String nome;
    private final String sigla;
    private final String numero;

    public Partido(String id, String nome, String sigla, String numero) {

        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("O ID do partido é obrigatório.");
        }

        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("O nome do partido é obrigatório.");
        }

        if (sigla == null || sigla.isBlank()) {
            throw new IllegalArgumentException("A sigla do partido é obrigatória.");
        }

        if (numero == null || !numero.matches("\\d{2}")) {
            throw new IllegalArgumentException(
                    "O número do partido deve possuir dois dígitos."
            );
        }

        this.id = id;
        this.nome = nome;
        this.sigla = sigla;
        this.numero = numero;
    }

    public String getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getSigla() {
        return sigla;
    }

    public String getNumero() {
        return numero;
    }
}
