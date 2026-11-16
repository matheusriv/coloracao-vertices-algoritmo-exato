import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Random;
import java.util.Scanner;

public class GeradorInstancias {

    private static final String DIR_INSTANCIAS = "instancias";

    private static void salvarMatrizDIMACS(String nomeArq, String cabecalho, boolean[][] matAdj) {
        File pasta = new File(DIR_INSTANCIAS);
        if (!pasta.exists()) pasta.mkdirs();

        int nVertices = matAdj.length;
        int totalArestas = 0;
        for (int u = 0; u < nVertices; u++) {
            for (int v = u + 1; v < nVertices; v++) {
                if (matAdj[u][v]) totalArestas++;
            }
        }

        File destino = new File(pasta, nomeArq);
        try (PrintWriter pw = new PrintWriter(new FileWriter(destino))) {
            pw.println("c " + cabecalho);
            pw.println("p edge " + nVertices + " " + totalArestas);
            for (int u = 0; u < nVertices; u++) {
                for (int v = u + 1; v < nVertices; v++) {
                    if (matAdj[u][v]) {
                        pw.println("e " + (u + 1) + " " + (v + 1));
                    }
                }
            }
            System.out.println(">>> Arquivo gerado: " + destino.getPath() + " (|V|=" + nVertices + ", |E|=" + totalArestas + ")");
        } catch (IOException e) {
            System.out.println("Falha ao criar arquivo: " + e.getMessage());
        }
    }

    public static void gerarMycielski(String nomeArq, int cromaticoDesejado) {
        boolean[][] matAdj = new boolean[2][2];
        matAdj[0][1] = true;
        matAdj[1][0] = true;

        for (int i = 3; i <= cromaticoDesejado; i++) {
            int nAnt = matAdj.length;
            int nNovo = 2 * nAnt + 1;
            boolean[][] novaMat = new boolean[nNovo][nNovo];

            for (int r = 0; r < nAnt; r++) {
                for (int c = 0; c < nAnt; c++) {
                    if (matAdj[r][c]) {
                        novaMat[r][c] = true;
                        novaMat[r][nAnt + c] = true;
                        novaMat[nAnt + c][r] = true;
                    }
                }
            }

            int w = 2 * nAnt;
            for (int c = 0; c < nAnt; c++) {
                novaMat[nAnt + c][w] = true;
                novaMat[w][nAnt + c] = true;
            }
            matAdj = novaMat;
        }

        salvarMatrizDIMACS(nomeArq, "Instancia DIMACS Mycielski (Otimo Exato = " + cromaticoDesejado + ")", matAdj);
    }

    public static void gerarQueen(String nomeArq, int n) {
        int nVertices = n * n;
        boolean[][] matAdj = new boolean[nVertices][nVertices];

        for (int r1 = 0; r1 < n; r1++) {
            for (int c1 = 0; c1 < n; c1++) {
                int u = r1 * n + c1;
                for (int r2 = 0; r2 < n; r2++) {
                    for (int c2 = 0; c2 < n; c2++) {
                        int v = r2 * n + c2;
                        if (u < v) {
                            boolean mesmaLinha = (r1 == r2);
                            boolean mesmaColuna = (c1 == c2);
                            boolean mesmaDiagonal = (Math.abs(r1 - r2) == Math.abs(c1 - c2));
                            if (mesmaLinha || mesmaColuna || mesmaDiagonal) {
                                matAdj[u][v] = true;
                                matAdj[v][u] = true;
                            }
                        }
                    }
                }
            }
        }

        salvarMatrizDIMACS(nomeArq, "Instancia DIMACS Queen " + n + "x" + n, matAdj);
    }

    public static void gerarErdosRenyi(String nomeArq, int nVertices, double probAresta, long semente) {
        Random rng = new Random(semente);
        boolean[][] matAdj = new boolean[nVertices][nVertices];

        for (int u = 0; u < nVertices; u++) {
            for (int v = u + 1; v < nVertices; v++) {
                if (rng.nextDouble() < probAresta) {
                    matAdj[u][v] = true;
                    matAdj[v][u] = true;
                }
            }
        }

        String info = "Instancia sintetica Erdos-Renyi G(n, p) - Parametros: |V|=" + nVertices + ", p=" + probAresta + ", seed=" + semente;
        salvarMatrizDIMACS(nomeArq, info, matAdj);
    }

    public static void gerarKPartidoOtimoConhecido(String nomeArq, int nVertices, int kCores, double densEntreParticoes, long semente) {
        Random rng = new Random(semente);
        boolean[][] matAdj = new boolean[nVertices][nVertices];

        for (int u = 0; u < kCores && u < nVertices; u++) {
            for (int v = u + 1; v < kCores && v < nVertices; v++) {
                matAdj[u][v] = true;
                matAdj[v][u] = true;
            }
        }

        for (int u = 0; u < nVertices; u++) {
            for (int v = u + 1; v < nVertices; v++) {
                if (u < kCores && v < kCores) continue;
                if ((u % kCores) != (v % kCores)) {
                    if (rng.nextDouble() < densEntreParticoes) {
                        matAdj[u][v] = true;
                        matAdj[v][u] = true;
                    }
                }
            }
        }

        String info = "Grafo k-partido com clique embutida (Otimo Exato = " + kCores + ") - |V|=" + nVertices + ", p=" + densEntreParticoes + ", seed=" + semente;
        salvarMatrizDIMACS(nomeArq, info, matAdj);
    }

    public static void gerarConjuntoPadrao() {
        File pasta = new File(DIR_INSTANCIAS);
        if (!pasta.exists()) pasta.mkdirs();

        gerarMycielski("myciel3.col", 4);
        gerarMycielski("myciel4.col", 5);
        gerarMycielski("myciel5.col", 6);
        gerarQueen("queen5_5.col", 5);
        gerarQueen("queen6_6.col", 6);

        gerarKPartidoOtimoConhecido("kpartido_100v_otimo8.col", 100, 8, 0.45, 202L);
        gerarKPartidoOtimoConhecido("kpartido_250v_otimo12.col", 250, 12, 0.50, 505L);
        gerarKPartidoOtimoConhecido("kpartido_400v_otimo15.col", 400, 15, 0.55, 606L);

        gerarErdosRenyi("erdos_60v.col", 60, 0.15, 101L);
        gerarErdosRenyi("erdos_400v.col", 400, 0.02, 303L);
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        while (true) {
            System.out.println("\n=======================================================");
            System.out.println("     GERADOR DE INSTANCIAS DIMACS (.col) - GRAFOS      ");
            System.out.println("=======================================================");
            System.out.println("1 - Criar pacote padrao de testes (10 instancias)");
            System.out.println("2 - Criar grafo aleatorio Erdos-Renyi G(n, p)");
            System.out.println("3 - Criar grafo k-partido com Otimo Conhecido");
            System.out.println("0 - Sair");
            System.out.print("Opcao: ");

            String op = sc.nextLine().trim();
            if (op.equals("0")) {
                break;
            } else if (op.equals("1")) {
                gerarConjuntoPadrao();
            } else if (op.equals("2")) {
                try {
                    System.out.print("Nome do arquivo (ex: grafo100.col): ");
                    String nome = sc.nextLine().trim();
                    if (!nome.endsWith(".col")) nome += ".col";

                    System.out.print("Quantidade de vertices |V|: ");
                    int n = Integer.parseInt(sc.nextLine().trim());

                    System.out.print("Probabilidade de aresta [0.0 a 1.0]: ");
                    double p = Double.parseDouble(sc.nextLine().trim().replace(",", "."));

                    System.out.print("Semente (seed) numerica: ");
                    long semente = Long.parseLong(sc.nextLine().trim());

                    gerarErdosRenyi(nome, n, p, semente);
                } catch (Exception e) {
                    System.out.println("Entrada invalida.");
                }
            } else if (op.equals("3")) {
                try {
                    System.out.print("Nome do arquivo (ex: kpartido250.col): ");
                    String nome = sc.nextLine().trim();
                    if (!nome.endsWith(".col")) nome += ".col";

                    System.out.print("Quantidade de vertices |V|: ");
                    int n = Integer.parseInt(sc.nextLine().trim());

                    System.out.print("Numero Cromatico Otimo k desejado: ");
                    int k = Integer.parseInt(sc.nextLine().trim());

                    System.out.print("Densidade entre grupos [0.1 a 1.0]: ");
                    double p = Double.parseDouble(sc.nextLine().trim().replace(",", "."));

                    System.out.print("Semente (seed) numerica: ");
                    long semente = Long.parseLong(sc.nextLine().trim());

                    gerarKPartidoOtimoConhecido(nome, n, k, p, semente);
                } catch (Exception e) {
                    System.out.println("Entrada invalida.");
                }
            } else {
                System.out.println("Opcao invalida.");
            }
        }
        sc.close();
    }
}