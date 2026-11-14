package br.edu.unijorge.urnacidada.model;

public class Candidato {

    private final String id;
    private final String nome;
    private final String numero;
    private final Partido partido;
    private final Cargo cargo;

    public Candidato(
            String id,
            String nome,
            String numero,
            Partido partido,
            Cargo cargo
    ) {

        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("O ID do candidato é obrigatório.");
        }

        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("O nome do candidato é obrigatório.");
        }

        if (partido == null) {
            throw new IllegalArgumentException("O partido é obrigatório.");
        }

        if (cargo == null) {
            throw new IllegalArgumentException("O cargo é obrigatório.");
        }

        if (numero == null ||
                !numero.matches("\\d{" + cargo.getQuantidadeDigitos() + "}")) {

            throw new IllegalArgumentException(
                    "O número do candidato deve possuir "
                            + cargo.getQuantidadeDigitos() + " dígitos."
            );
        }

        if (cargo.getTipo() == TipoCargo.PROPORCIONAL
                && !numero.startsWith(partido.getNumero())) {

            throw new IllegalArgumentException(
                    "Os dois primeiros dígitos do candidato devem "
                            + "corresponder ao número do partido."
            );
        }

        if (cargo.getTipo() == TipoCargo.MAJORITARIO
                && !numero.equals(partido.getNumero())) {

            throw new IllegalArgumentException(
                    "O número do candidato majoritário deve "
                            + "corresponder ao número do partido."
            );
        }

        this.id = id;
        this.nome = nome;
        this.numero = numero;
        this.partido = partido;
        this.cargo = cargo;
    }

    public String getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getNumero() {
        return numero;
    }

    public Partido getPartido() {
        return partido;
    }

    public Cargo getCargo() {
        return cargo;
    }
}
