package br.edu.unijorge.urnacidada.model;

public enum Cargo {

    DEPUTADO_FEDERAL("Deputado Federal", 4, TipoCargo.PROPORCIONAL),

    PRESIDENTE("Presidente", 2, TipoCargo.MAJORITARIO);

    private final String nome;
    private final int quantidadeDigitos;
    private final TipoCargo tipo;

    Cargo(String nome, int quantidadeDigitos, TipoCargo tipo) {
        this.nome = nome;
        this.quantidadeDigitos = quantidadeDigitos;
        this.tipo = tipo;
    }

    public String getNome() {
        return nome;
    }

    public int getQuantidadeDigitos() {
        return quantidadeDigitos;
    }

    public TipoCargo getTipo() {
        return tipo;
    }
}
