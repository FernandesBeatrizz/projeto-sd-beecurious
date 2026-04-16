# BeeCurious_SD - Motor de Pesquisa Distribuído

O **BeeCurious_SD** é um sistema distribuído que replica o funcionamento de um motor de busca, integrando tecnologias como Java RMI, Spring Boot e APIs de IA.

## Funcionalidades Principais
* **Indexação:** Processamento de páginas web e extração de metadados (títulos, citações, links).
* **Pesquisa:** Procura de termos num índice invertido com ordenação por relevância.
* **IA:** Geração de análises contextuais das pesquisas via OpenRouter API.
* **Hacker News:** Indexação automática das "Top Stories" externas.

## Arquitetura
O projeto divide-se nos seguintes componentes distribuídos:
1.  **Gateway:** Coordenador central do sistema.
2.  **Barrels:** Servidores de armazenamento do índice invertido.
3.  **Downloaders:** Responsáveis pelo crawling e processamento de URLs.
4.  **Queue:** Fila RMI de URLs para processamento.
5.  **Frontend:** Interface web desenvolvida em Spring Boot.

## Como Executar
1.  **Backend:** Inicie a `Gateway`, seguida dos `Barrels`, `Queue` e `Downloaders` via terminal Java.
2.  **Frontend:** No diretório raiz, execute `mvn spring-boot:run`.
3.  **Acesso:** Abra `http://localhost:8080/` no seu navegador.

---

## Documentação Adicional
Para detalhes técnicos aprofundados, diagramas de arquitetura, especificações de métodos RMI e relatórios de testes, consulte o ficheiro **relatorio sd.pdf** incluído na raiz do projeto.

---
**Autores:** Beatriz Fernandes & Diana Martins