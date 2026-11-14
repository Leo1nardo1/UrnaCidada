# 🗳️ Urna Cidadã

**Simulação Educativa do Voto Eletrônico**

Este projeto é uma aplicação desktop desenvolvida em Java com o objetivo de simular o funcionamento da urna eletrônica brasileira. Trata-se de um projeto de extensão acadêmica focado em educação cívica e tecnológica, permitindo que os usuários compreendam o fluxo de votação de forma segura, simples e interativa.

## 🎯 Objetivo do Projeto
O sistema foi projetado para ser utilizado como ferramenta de apoio didático. Ele simula a votação para cargos como Deputado Federal e Presidente, incluindo recursos reais como:
- Interface gráfica idêntica à urna eletrônica;
- Votação nominal, em branco e nulo;
- Tela de "FIM" com transição automática;
- Tela de apuração com gráficos e estatísticas;
- Exportação de relatórios em CSV.

## 🛠️ Tecnologias Utilizadas
- **Java 21** (Linguagem base);
- **Java Swing** (Interface Gráfica);
- **Maven** (Gerenciamento de dependências e build);
- **Firebase Admin SDK / Cloud Firestore** (Banco de dados na nuvem para salvar os votos - opcional);
- **Padrão de Arquitetura**: MVC adaptado (Model, View, Controller, Service, Repository).

## 📁 Estrutura do Projeto
```text
src/main/java/br/edu/unijorge/urnacidada/
├── config/       # Configurações do Firebase e dados simulados
├── controller/   # Regras de controle da interface e fluxo de telas
├── model/        # Entidades (Candidato, Voto, Cargo, Sessão, etc.)
├── repository/   # Classes de persistência (Memória Local e Firestore)
├── service/      # Regras de negócio (Votação, Apuração, Exportação)
└── view/         # Telas do sistema (Swing)
