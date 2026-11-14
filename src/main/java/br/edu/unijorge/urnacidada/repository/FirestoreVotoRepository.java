package br.edu.unijorge.urnacidada.repository;

import br.edu.unijorge.urnacidada.config.FirebaseConfig;
import br.edu.unijorge.urnacidada.model.Cargo;
import br.edu.unijorge.urnacidada.model.TipoVoto;
import br.edu.unijorge.urnacidada.model.Voto;

import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Guarda os votos no Cloud Firestore, na coleção "votos".
 *
 * Cada voto vira um documento com o ID "{sessaoId}_{CARGO}". Assim o próprio
 * banco impede dois votos para o mesmo cargo na mesma sessão (mesma regra do
 * repositório em memória), mesmo que várias urnas gravem ao mesmo tempo.
 *
 * Sigilo: o documento guarda sessão, cargo, tipo, candidato e horário.
 * Nenhum dado do eleitor é coletado.
 */
public class FirestoreVotoRepository implements VotoRepository {

    private static final String COLECAO = "votos";
    private static final long TEMPO_LIMITE_SEGUNDOS = 15;

    private final Firestore firestore;

    public FirestoreVotoRepository(Firestore firestore) {

        if (firestore == null) {
            throw new IllegalArgumentException("A conexão com o Firestore é obrigatória.");
        }

        this.firestore = firestore;
    }

    /** Conecta usando a chave configurada; vazio se não houver chave. */
    public static Optional<FirestoreVotoRepository> conectar() {
        return FirebaseConfig.conectar().map(FirestoreVotoRepository::new);
    }

    @Override
    public void salvar(Voto voto) {

        if (voto == null) {
            throw new IllegalArgumentException("Não é possível salvar um voto vazio.");
        }

        Map<String, Object> dados = new HashMap<>();
        dados.put("id", voto.getId());
        dados.put("sessaoId", voto.getSessaoId());
        dados.put("cargo", voto.getCargo().name());
        dados.put("tipo", voto.getTipo().name());
        dados.put("candidatoId", voto.getCandidatoId());
        dados.put("dataHora", voto.getDataHora().toString());

        String idDocumento = voto.getSessaoId() + "_" + voto.getCargo().name();

        try {
            // create() falha se o documento já existir
            firestore.collection(COLECAO)
                    .document(idDocumento)
                    .create(dados)
                    .get(TEMPO_LIMITE_SEGUNDOS, TimeUnit.SECONDS);

        } catch (ExecutionException e) {

            if (jaExiste(e)) {
                throw new IllegalStateException(
                        "Já existe um voto registrado para este cargo nesta sessão.", e
                );
            }

            throw new IllegalStateException(
                    "Não foi possível gravar o voto no Firebase: " + causaDe(e), e
            );

        } catch (TimeoutException e) {
            throw new IllegalStateException(
                    "O Firebase não respondeu a tempo. Verifique a conexão com a internet.", e
            );

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("A gravação do voto foi interrompida.", e);
        }
    }

    @Override
    public List<Voto> buscarPorSessao(String sessaoId) {

        if (sessaoId == null || sessaoId.isBlank()) {
            throw new IllegalArgumentException("O ID da sessão é obrigatório.");
        }

        try {
            return converter(firestore.collection(COLECAO)
                    .whereEqualTo("sessaoId", sessaoId)
                    .get()
                    .get(TEMPO_LIMITE_SEGUNDOS, TimeUnit.SECONDS)
                    .getDocuments());

        } catch (ExecutionException | TimeoutException e) {
            throw new IllegalStateException(
                    "Não foi possível ler os votos da sessão no Firebase: " + causaDe(e), e
            );

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("A leitura dos votos foi interrompida.", e);
        }
    }

    @Override
    public List<Voto> listarTodos() {

        try {
            CollectionReference colecao = firestore.collection(COLECAO);

            return converter(colecao
                    .get()
                    .get(TEMPO_LIMITE_SEGUNDOS, TimeUnit.SECONDS)
                    .getDocuments());

        } catch (ExecutionException | TimeoutException e) {
            throw new IllegalStateException(
                    "Não foi possível ler os votos no Firebase: " + causaDe(e), e
            );

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("A leitura dos votos foi interrompida.", e);
        }
    }

    @Override
    public String descricao() {
        return "Firebase (Cloud Firestore)";
    }

    // ------------------------------------------------------------------

    private List<Voto> converter(List<QueryDocumentSnapshot> documentos) {

        List<Voto> votos = new ArrayList<>();

        for (QueryDocumentSnapshot documento : documentos) {
            votos.add(Voto.reconstituir(
                    documento.getString("id"),
                    documento.getString("sessaoId"),
                    Cargo.valueOf(documento.getString("cargo")),
                    TipoVoto.valueOf(documento.getString("tipo")),
                    documento.getString("candidatoId"),
                    LocalDateTime.parse(documento.getString("dataHora"))
            ));
        }

        return votos;
    }

    private boolean jaExiste(ExecutionException e) {
        String texto = String.valueOf(e.getCause());
        return texto.contains("ALREADY_EXISTS");
    }

    private String causaDe(Exception e) {
        Throwable causa = e.getCause() != null ? e.getCause() : e;
        return causa.getMessage() != null ? causa.getMessage() : causa.getClass().getSimpleName();
    }
}
