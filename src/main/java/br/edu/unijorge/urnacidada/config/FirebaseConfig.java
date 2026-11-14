package br.edu.unijorge.urnacidada.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

/**
 * Conexão com o Cloud Firestore.
 *
 * A chave da conta de serviço (arquivo JSON baixado no console do Firebase)
 * é procurada nesta ordem:
 *   1. variável de ambiente URNA_FIREBASE_CHAVE (caminho completo do arquivo);
 *   2. arquivo "firebase-chave.json" na pasta de onde o programa é executado.
 *
 * Se nenhum arquivo for encontrado, retorna vazio e a urna usa a memória local.
 * ATENÇÃO: esse arquivo dá acesso total ao projeto. Nunca o envie ao GitHub.
 */
public final class FirebaseConfig {

    public static final String VARIAVEL_AMBIENTE = "URNA_FIREBASE_CHAVE";
    public static final String ARQUIVO_PADRAO = "firebase-chave.json";

    private FirebaseConfig() {
    }

    /**
     * @return o Firestore conectado, ou vazio se não houver chave configurada
     * @throws IllegalStateException se a chave existir mas não puder ser usada
     */
    public static Optional<Firestore> conectar() {

        Optional<Path> chave = localizarChave();

        if (chave.isEmpty()) {
            return Optional.empty();
        }

        try (InputStream entrada = new FileInputStream(chave.get().toFile())) {

            // O app do Firebase só pode ser criado uma vez por execução.
            if (FirebaseApp.getApps().isEmpty()) {

                FirebaseOptions opcoes = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(entrada))
                        .build();

                FirebaseApp.initializeApp(opcoes);
            }

            return Optional.of(FirestoreClient.getFirestore());

        } catch (IOException | RuntimeException e) {
            throw new IllegalStateException(
                    "Não foi possível conectar ao Firebase com a chave "
                            + chave.get().toAbsolutePath() + ": " + e.getMessage(), e
            );
        }
    }

    private static Optional<Path> localizarChave() {

        String caminhoAmbiente = System.getenv(VARIAVEL_AMBIENTE);

        if (caminhoAmbiente != null && !caminhoAmbiente.isBlank()) {

            Path caminho = Path.of(caminhoAmbiente);

            if (!Files.isRegularFile(caminho)) {
                throw new IllegalStateException(
                        "A variável " + VARIAVEL_AMBIENTE
                                + " aponta para um arquivo que não existe: " + caminho
                );
            }

            return Optional.of(caminho);
        }

        Path padrao = Path.of(ARQUIVO_PADRAO);

        return Files.isRegularFile(padrao) ? Optional.of(padrao) : Optional.empty();
    }
}
