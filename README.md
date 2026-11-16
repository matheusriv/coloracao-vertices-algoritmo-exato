# Algoritmo Exato para o Problema de Coloração de Vértices

Implementação em Java puro de um algoritmo exato para o Problema de Coloração de Vértices. O projeto utiliza a técnica de Branch-and-Bound guiada dinamicamente pelo grau de saturação (DSATUR, proposto por Brélaz, 1979 e Brown, 1972), combinada com uma heurística gulosa de Clique Maximal Inicial na raiz da árvore de busca.


## Pré-requisitos

* Java Development Kit (JDK): Versão 8 ou superior (testado em OpenJDK 11, 17 e 21).

Para verificar se o compilador e a máquina virtual estão disponíveis no sistema:
```bash
javac -version
java -version
```


## Como Compilar e Executar

Abra o terminal (Linux) ou Prompt de Comando / PowerShell (Windows) diretamente na pasta raiz do projeto.

### Passo 1: Compilar todos os códigos-fonte
Execute o comando abaixo para compilar todas as classes de uma só vez:

```bash
javac Grafo.java AlgoritmoExato.java Principal.java GeradorInstancias.java
```
*(Em terminais Linux/macOS ou PowerShell: `javac *.java`)*

### Passo 2: Gerar ou Popular as Instâncias de Teste (Opcional)
Caso a pasta `instancias/` esteja vazia ou você deseje recriar o pacote padrão de casos de teste sintéticos e da literatura, execute o gerador:

```bash
java GeradorInstancias
```
No menu do gerador:
* Digite `1` para criar automaticamente as instâncias padrão na pasta `instancias/`.
* Digite `2` para gerar um grafo aleatório personalizado segundo o modelo de Erdős–Rényi $G(n, p)$.
* Digite `3` para gerar um grafo $k$-partido com número cromático ótimo conhecido $\chi(G) = k$.
* Digite `0` para sair do gerador.

### Passo 3: Executar o Algoritmo Exato e Gerar os Relatórios
Inicie a aplicação principal com o comando:

```bash
java Principal
```
*(Para instâncias muito grandes, você pode alocar memória extra para a JVM usando `java -Xmx2G Principal`).*

No menu interativo principal:
* **Opção `1`**: Lista todos os arquivos `.col` ou `.txt` presentes na pasta `instancias/`. Basta digitar o número do caso de teste desejado para executá-lo, visualizar o relatório completo na tela e salvá-lo automaticamente em `relatorios/relatorio_<nome_da_instancia>.txt`.
* **Opção `2`**: Executa em lote todas as instâncias disponíveis na pasta `instancias/` e salva um relatório consolidado em `relatorios/relatorio_bateria_completa.txt`.
* **Opção `3`**: Permite ajustar o tempo limite (*timeout*) por instância.
* **Opção `0`**: Encerra o programa.

## Conjunto de Casos de Teste

Todas as instâncias seguem a especificação **DIMACS Standard Format (`.col`)**:
* Linhas iniciadas por `c`: Comentários e metadados da instância.
* Linha `p edge <V> <E>`: Declaração da quantidade de vértices $\vert{}V\vert{}$ e arestas $\vert{}E\vert{}$.
* Linhas `e <u> <v>`: Declaração de aresta entre os vértices $u$ e $v$ (indexados de $1$ a $\vert{}V\vert{}$).
