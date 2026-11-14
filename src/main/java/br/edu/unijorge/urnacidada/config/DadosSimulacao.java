package br.edu.unijorge.urnacidada.config;

import br.edu.unijorge.urnacidada.model.Candidato;
import br.edu.unijorge.urnacidada.model.Cargo;
import br.edu.unijorge.urnacidada.model.Partido;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class DadosSimulacao {

    private final List<Partido> partidos;
    private final List<Candidato> candidatos;

    public DadosSimulacao() {

        // Partidos fictícios
        Partido natureza = new Partido(
                "P001",
                "Partido da Natureza",
                "PN",
                "11"
        );

        Partido futuro = new Partido(
                "P002",
                "Partido do Futuro",
                "PF",
                "22"
        );

        Partido comunidade = new Partido(
                "P003",
                "Partido da Comunidade",
                "PC",
                "33"
        );

        partidos = List.of(
                natureza,
                futuro,
                comunidade
        );

        // Candidatos a deputado federal
        // Quatro dígitos: dois do partido + dois do candidato
        //
        // Candidatos a presidente
        // Dois dígitos: número do partido

        candidatos = List.of(

                // Partido da Natureza
                new Candidato(
                        "C001", "Leão", "1101",
                        natureza, Cargo.DEPUTADO_FEDERAL
                ),

                new Candidato(
                        "C002", "Tigre", "1102",
                        natureza, Cargo.DEPUTADO_FEDERAL
                ),

                new Candidato(
                        "C003", "Coruja", "11",
                        natureza, Cargo.PRESIDENTE
                ),

                // Partido do Futuro
                new Candidato(
                        "C004", "Águia", "2201",
                        futuro, Cargo.DEPUTADO_FEDERAL
                ),

                new Candidato(
                        "C005", "Lobo", "2202",
                        futuro, Cargo.DEPUTADO_FEDERAL
                ),

                new Candidato(
                        "C006", "Fênix", "22",
                        futuro, Cargo.PRESIDENTE
                ),

                // Partido da Comunidade
                new Candidato(
                        "C007", "Golfinho", "3301",
                        comunidade, Cargo.DEPUTADO_FEDERAL
                ),

                new Candidato(
                        "C008", "Tartaruga", "3302",
                        comunidade, Cargo.DEPUTADO_FEDERAL
                ),

                new Candidato(
                        "C009", "Panda", "33",
                        comunidade, Cargo.PRESIDENTE
                )
        );

        validarNumerosDuplicados();
    }

    public List<Partido> listarPartidos() {
        return partidos;
    }

    public List<Candidato> listarCandidatos() {
        return candidatos;
    }

    public List<Candidato> listarCandidatosPorCargo(Cargo cargo) {

        if (cargo == null) {
            throw new IllegalArgumentException("O cargo é obrigatório.");
        }

        return candidatos.stream()
                .filter(candidato -> candidato.getCargo() == cargo)
                .toList();
    }

    public Optional<Candidato> buscarCandidatoPorNumero(
            String numero,
            Cargo cargo
    ) {

        if (numero == null || numero.isBlank() || cargo == null) {
            return Optional.empty();
        }

        return candidatos.stream()
                .filter(candidato -> candidato.getCargo() == cargo)
                .filter(candidato -> candidato.getNumero().equals(numero))
                .findFirst();
    }

    private void validarNumerosDuplicados() {

        Set<String> numerosRegistrados = new HashSet<>();

        for (Candidato candidato : candidatos) {

            String identificador =
                    candidato.getCargo() + ":" + candidato.getNumero();

            if (!numerosRegistrados.add(identificador)) {

                throw new IllegalStateException(
                        "Número duplicado para o cargo "
                                + candidato.getCargo().getNome()
                                + ": " + candidato.getNumero()
                );
            }
        }
    }
}
