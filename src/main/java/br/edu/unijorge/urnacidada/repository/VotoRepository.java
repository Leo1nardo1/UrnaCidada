package br.edu.unijorge.urnacidada.repository;

import br.edu.unijorge.urnacidada.model.Voto;

import java.util.List;

public interface VotoRepository {

    void salvar(Voto voto);

    List<Voto> buscarPorSessao(String sessaoId);

    /** Todos os votos já registrados, de todas as sessões (usado na apuração). */
    List<Voto> listarTodos();

    /** Texto curto para mostrar onde os votos estão sendo guardados. */
    String descricao();

}
