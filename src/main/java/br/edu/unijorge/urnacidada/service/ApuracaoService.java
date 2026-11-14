package br.edu.unijorge.urnacidada.service;

import br.edu.unijorge.urnacidada.config.DadosSimulacao;
import br.edu.unijorge.urnacidada.model.Apuracao;
import br.edu.unijorge.urnacidada.model.Candidato;
import br.edu.unijorge.urnacidada.model.Cargo;
import br.edu.unijorge.urnacidada.model.ResultadoCandidato;
import br.edu.unijorge.urnacidada.model.ResultadoCargo;
import br.edu.unijorge.urnacidada.model.TipoVoto;
import br.edu.unijorge.urnacidada.model.Voto;
import br.edu.unijorge.urnacidada.repository.VotoRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Conta os votos guardados no repositório e monta o resultado de cada cargo. */
public class ApuracaoService {

    private final DadosSimulacao dadosSimulacao;
    private final VotoRepository votoRepository;

    public ApuracaoService(DadosSimulacao dadosSimulacao, VotoRepository votoRepository) {

        if (dadosSimulacao == null || votoRepository == null) {
            throw new IllegalArgumentException(
                    "Os dados da simulação e o repositório são obrigatórios."
            );
        }

        this.dadosSimulacao = dadosSimulacao;
        this.votoRepository = votoRepository;
    }

    /** Lê todos os votos e calcula o resultado. Pode demorar se o repositório for remoto. */
    public Apuracao apurar() {

        List<Voto> votos = votoRepository.listarTodos();

        Set<String> sessoes = new HashSet<>();
        for (Voto voto : votos) {
            sessoes.add(voto.getSessaoId());
        }

        List<ResultadoCargo> resultados = new ArrayList<>();
        for (Cargo cargo : Cargo.values()) {
            resultados.add(apurarCargo(cargo, votos));
        }

        return new Apuracao(resultados, sessoes.size(), LocalDateTime.now());
    }

    public String getOrigemDados() {
        return votoRepository.descricao();
    }

    private ResultadoCargo apurarCargo(Cargo cargo, List<Voto> todosVotos) {

        Map<String, Integer> contagemPorCandidato = new HashMap<>();
        int brancos = 0;
        int nulos = 0;

        for (Voto voto : todosVotos) {

            if (voto.getCargo() != cargo) {
                continue;
            }

            if (voto.getTipo() == TipoVoto.BRANCO) {
                brancos++;
            } else if (voto.getTipo() == TipoVoto.NULO) {
                nulos++;
            } else {
                contagemPorCandidato.merge(voto.getCandidatoId(), 1, Integer::sum);
            }
        }

        // Todos os candidatos aparecem, mesmo os que não receberam votos.
        List<ResultadoCandidato> candidatos = new ArrayList<>();
        for (Candidato candidato : dadosSimulacao.listarCandidatosPorCargo(cargo)) {
            candidatos.add(new ResultadoCandidato(
                    candidato,
                    contagemPorCandidato.getOrDefault(candidato.getId(), 0)
            ));
        }

        candidatos.sort(
                Comparator.comparingInt(ResultadoCandidato::votos).reversed()
                        .thenComparing(r -> r.candidato().getNumero())
        );

        return new ResultadoCargo(cargo, candidatos, brancos, nulos);
    }
}
