# Configurando o Firebase (Cloud Firestore)

A urna funciona sem Firebase (votos só na memória). Para guardar os votos na nuvem:

1. Acesse https://console.firebase.google.com e crie um projeto (ex.: `urna-cidada`).
2. Menu **Build > Firestore Database > Criar banco de dados**.
   - Edição: Standard, modo **produção** (regras bloqueadas; o programa usa a conta de serviço e não precisa de regras abertas).
   - Local sugerido: `southamerica-east1` (São Paulo).
3. **Configurações do projeto > Contas de serviço > Gerar nova chave privada**. Será baixado um arquivo `.json`.
4. Salve esse arquivo como `firebase-chave.json` na pasta de onde o programa é executado
   (no IntelliJ: *Run > Edit Configurations > Working directory*; normalmente a raiz do projeto),
   **ou** defina a variável de ambiente `URNA_FIREBASE_CHAVE` com o caminho completo do arquivo.
5. Execute o `Main`. Na tela inicial deve aparecer **"Votos guardados em: Firebase (Cloud Firestore)"**.

## Cuidados

- `firebase-chave.json` dá acesso total ao projeto. Ele já está no `.gitignore`: **nunca** envie ao GitHub nem compartilhe.
  Use um projeto Firebase exclusivo para a extensão e apague a chave depois da oficina.
- Se a chave não for encontrada, ou a conexão falhar na abertura, a urna avisa e continua com a memória local.
- Os votos ficam na coleção `votos`. Cada documento tem: `id`, `sessaoId`, `cargo`, `tipo`, `candidatoId`, `dataHora`.
  Nenhum dado pessoal do eleitor é guardado.
- O documento de cada voto tem ID `{sessaoId}_{CARGO}`, o que impede dois votos para o mesmo cargo na mesma sessão.
- Para zerar a simulação entre turmas, apague a coleção `votos` no console.
