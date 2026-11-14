package br.edu.unijorge.urnacidada.repository;

import br.edu.unijorge.urnacidada.model.Voto;

import java.util.ArrayList;
import java.util.List;

public class VotoMemoriaRepository implements VotoRepository {

    private final List<Voto> votos = new ArrayList<>();

    @Override
    public void salvar(Voto voto) {

        if (voto == null) {
            throw new IllegalArgumentException(
                    "Não é possível salvar um voto vazio."
            );
        }

        boolean votoJaRegistrado = votos.stream()
                .anyMatch(v ->
                        v.getSessaoId().equals(voto.getSessaoId())
                                && v.getCargo() == voto.getCargo()
                );

        if (votoJaRegistrado) {
            throw new IllegalStateException(
                    "Já existe um voto registrado para este cargo nesta sessão."
            );
        }

        votos.add(voto);
    }

    @Override
    public List<Voto> buscarPorSessao(String sessaoId) {

        if (sessaoId == null || sessaoId.isBlank()) {
            throw new IllegalArgumentException(
                    "O ID da sessão é obrigatório."
            );
        }

        return votos.stream()
                .filter(v -> v.getSessaoId().equals(sessaoId))
                .toList();
    }

    @Override
    public List<Voto> listarTodos() {
        return List.copyOf(votos);
    }

    @Override
    public String descricao() {
        return "memória local (os votos somem ao fechar o programa)";
    }
}
