import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

public class AlgoritmoExato {

    private Grafo grafo;
    private int nVertices;
    private boolean[][] matAdj;

    // Estruturas do Branch-and-Bound
    private int[] corVertice;
    private int[] melhorSolucao;
    private int menorNumCores;    // Limite Superior (Upper bound)
    private int tamCliqueInicial;  // Limite Inferior (Lower bound )

    // Métricas do experimento
    private long nosExplorados;
    private long ramosPodados;
    private long instanteInicial;
    private long tempoExecucaoMs;
    private long timeoutMs;
    private boolean parouPorTimeout;

    public AlgoritmoExato(Grafo grafo, long timeoutMs) {
        this.grafo = grafo;
        this.nVertices = grafo.getNumVertices();
        this.matAdj = grafo.getMatAdj();
        this.timeoutMs = timeoutMs;

        this.corVertice = new int[nVertices];
        this.melhorSolucao = new int[nVertices];
    }

    public void executarBuscaExata() {
        this.nosExplorados = 0;
        this.ramosPodados = 0;
        this.parouPorTimeout = false;
        this.instanteInicial = System.currentTimeMillis();

        // Inicializa o limite superior com o pior valor possível (nVertices + 1)
        this.menorNumCores = nVertices + 1;

        // Fixa as cores da clique maximal inicial na raiz da arvore
        this.tamCliqueInicial = fixarCliqueInicial();

        // Inicia a recursão do branch and bound
        buscarRamo(tamCliqueInicial, tamCliqueInicial);

        this.tempoExecucaoMs = System.currentTimeMillis() - instanteInicial;
    }

    private int fixarCliqueInicial() {
        Arrays.fill(corVertice, 0);
        int[] membrosClique = new int[nVertices];
        int qtdMembros = 0;

        // Seleciona o vértice de maior grau para iniciar a clique
        int vInicial = -1;
        int maxGrau = -1;
        for (int i = 0; i < nVertices; i++) {
            if (grafo.getGrau(i) > maxGrau) {
                maxGrau = grafo.getGrau(i);
                vInicial = i;
            }
        }

        if (vInicial == -1) return 0;

        membrosClique[qtdMembros] = vInicial;
        corVertice[vInicial] = 1;
        qtdMembros++ ;

        boolean expandiu;
        do {
            expandiu = false;
            int candidatoEscolhido = -1;
            int maiorGrauCandidato = -1;

            for (int v = 0; v < nVertices; v++) {
                if (corVertice[v] == 0) {
                    boolean conectadoATodos = true;
                    for (int i = 0; i < qtdMembros; i++) {
                        int uClique = membrosClique[i];
                        if (!matAdj[v][uClique]) {
                            conectadoATodos = false;
                            break;
                        }
                    }

                    if (conectadoATodos && grafo.getGrau(v) > maiorGrauCandidato) {
                        maiorGrauCandidato = grafo.getGrau(v);
                        candidatoEscolhido = v;
                    }
                }
            }

            if (candidatoEscolhido != -1) {
                membrosClique[qtdMembros] = candidatoEscolhido;
                qtdMembros++;
                corVertice[candidatoEscolhido] = qtdMembros;
                expandiu = true;
            }
        }  while (expandiu);

        return qtdMembros;
    }

    private void buscarRamo(int qtdColoridos, int maiorCorAtribuida) {
        if (System.currentTimeMillis() - instanteInicial > timeoutMs) {
            parouPorTimeout = true;
            return;
        }

        nosExplorados++;

        // Caso base: todos os vértices receberam cores válidas 
        if (qtdColoridos == nVertices) {
            menorNumCores = maiorCorAtribuida;
            System.arraycopy(corVertice, 0, melhorSolucao, 0, nVertices);
            return;
        }

        // Condição de otimalidade imediata: limite superior atingiu o Lower Bound
        if (menorNumCores == tamCliqueInicial) {
            return;
        }

        // Seleciona o vértice não colorido mais restrito (regra DSATUR)
        int vAtual = selecionarVerticeMaisSaturado();

        // Define o limite de cores para ramificação (Quebra de Simetria + Poda por Limite)
        int limiteCor = maiorCorAtribuida + 1;
        if (limiteCor >= menorNumCores) {
            ramosPodados += (limiteCor - (menorNumCores - 1));
            limiteCor = menorNumCores - 1;
        }

        for (int cor = 1; cor <= limiteCor; cor++) {
            if (corDisponivelParaVertice(vAtual, cor)){
                corVertice[vAtual] = cor;
                int novoMaximo = Math.max(maiorCorAtribuida, cor);

                buscarRamo(qtdColoridos + 1, novoMaximo);

                corVertice[vAtual] = 0; // Backtracking

                if (parouPorTimeout || menorNumCores == tamCliqueInicial || menorNumCores <= maiorCorAtribuida) {
                    return;
                }
            }
        }
    }

    private int selecionarVerticeMaisSaturado() {
        int escolhido = -1;
        int maxSaturacao = -1;
        int maxGrau = -1;

        for (int v = 0; v < nVertices; v++) {
            if (corVertice[v] == 0) {
                int sat = obterGrauSaturacao(v);
                int grauV = grafo.getGrau(v);

                if (sat > maxSaturacao || (sat == maxSaturacao && grauV > maxGrau)) {
                    maxSaturacao = sat;
                    maxGrau = grauV;
                    escolhido = v;
                }
            }
        }
        return escolhido;
    }

    private int obterGrauSaturacao(int v) {
        boolean[] coresNaVizinhanca = new boolean[nVertices + 1];
        int qtdDistintas = 0;

        for (int u = 0; u < nVertices; u++) {
            if (matAdj[v][u] && corVertice[u] != 0) {
                int c = corVertice[u];
                if (!coresNaVizinhanca[c]) {
                    coresNaVizinhanca[c] = true;
                    qtdDistintas++;
                }
            }
        }
        return qtdDistintas;
    }

    private boolean corDisponivelParaVertice(int v, int cor) {
        for (int u = 0; u < nVertices; u++) {
            if (matAdj[v][u] && corVertice[u] == cor) {
                return false;
            }
        }
        return true;
    }

    public boolean conferirSolucao(){
        for (int u = 0; u < nVertices; u++) {
            if (melhorSolucao[u] <= 0 || melhorSolucao[u] > menorNumCores) {
                return false;
            }
            for (int v = u + 1; v < nVertices; v++) {
                if (matAdj[u][v] && melhorSolucao[u] == melhorSolucao[v]) {
                    return false;
                }
            }
        }
        return true;
    }

    public String gerarTextoRelatorio() {  
        boolean solucaoCorreta = conferirSolucao();
        String dataAtual = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date());

        StringBuilder rel = new StringBuilder();
        rel.append("======================================================================\n");
        rel.append("         RELATORIO METODO EXATO - COLORACAO DE VERTICES               \n");
        rel.append("======================================================================\n");
        rel.append("Data/Hora                : ").append(dataAtual).append("\n");
        rel.append("Arquivo de Instancia     : ").append(grafo.getNomeInstancia()).append("\n");
        rel.append("Total de Vertices (|V|)  : ").append(nVertices).append("\n");
        rel.append("Total de Arestas  (|E|)  : ").append(grafo.getNumArestas()).append("\n");
        rel.append("----------------------------------------------------------------------\n");
        rel.append("ESTATISTICAS DA ARVORE DE BUSCA:\n");
        rel.append("Clique Inicial (LB)      : ").append(tamCliqueInicial).append(" cores fixadas\n");
        rel.append("Nos Visitados            : ").append(nosExplorados).append("\n");
        rel.append("Podas por Limite (Bound) : ").append(ramosPodados).append("\n");
        rel.append("Tempo de Processamento   : ").append(tempoExecucaoMs).append(" ms\n");
        rel.append("Condicao de Parada       : ").append(parouPorTimeout ? "LIMITE DE TEMPO ATINGIDO" : "SOLUCAO OTIMA COMPROVADA").append("\n");
        rel.append("Verificacao de Arestas   : ").append(solucaoCorreta ? "VALIDA (Sem conflitos de cor)" : "INVALIDA").append("\n");
        rel.append("----------------------------------------------------------------------\n");
        rel.append("MELHOR SOLUCAO ENCONTRADA:\n");
        rel.append(">>> Valor Associado (Numero Cromatico Obtido): ").append(menorNumCores).append(" cores <<<\n\n");
        rel.append("Atribuicao de Cores por Vertice (Vertice -> Cor):\n");

        for (int i = 0; i < nVertices; i++) {
            rel.append(String.format("V%-3d -> Cor %-2d", (i + 1), melhorSolucao[i]));
            if ((i + 1) % 5 == 0 || i == nVertices - 1) {
                rel.append("\n");
            } else {
                rel.append("  |  ");
            }
        }
        rel.append("======================================================================\n");
        return rel.toString();
    }
}