import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

public class Grafo {
    private String nomeInstancia;
    private int nVertices;
    private int nArestas;
    private boolean[][] matAdj;
    private int[] grau;

    public Grafo() {
        this.nVertices = 0;
        this.nArestas = 0;
    }

    public boolean carregarArquivoDIMACS(File arquivo)  {
        this.nomeInstancia = arquivo.getName();
        try (BufferedReader br = new BufferedReader(new FileReader(arquivo))) {
            String linha;
            int arestasContabilizadas = 0; 

            while ((linha = br.readLine()) != null) {
                linha = linha.trim();
                if (linha.isEmpty() || linha.startsWith("c")) {
                    continue; //ignora comentarios
                }

                if(linha.startsWith("p")) { 
                    String[] tokens = linha.split("\\s+");
                    this.nVertices = Integer.parseInt(tokens[2]);
                    this.nArestas = Integer.parseInt(tokens[3]);
                    this.matAdj = new boolean[nVertices][nVertices];
                    this.grau = new int[nVertices];
                } else if (linha.startsWith("e")) {
                    String[] tokens = linha.split("\\s+");
                    int u = Integer.parseInt(tokens[1]) - 1;
                    int v = Integer.parseInt(tokens[2]) - 1;
                    if (u >= 0 && u < nVertices && v >= 0 && v < nVertices && u != v) {
                        if (!matAdj[u][v]) {
                            matAdj[u][v] = true;
                            matAdj[v][u] = true;
                            grau[u]++;
                            grau[v]++;
                            arestasContabilizadas++;  
                        }
                    }
                }
            }
            this.nArestas = arestasContabilizadas;
            return this.nVertices > 0;  
        } catch (Exception e) {
            System.out.println("Falha ao ler instancia '" + arquivo.getName() + "': " + e.getMessage());
            return false;
        }
    }

    public boolean saoAdjacentes(int u, int v) {
        return matAdj[u][v];
    }

    public int getGrau(int v) {  
        return grau[v];
    }

    public int getNumVertices() {
        return nVertices;
    }

    public int getNumArestas() {
        return nArestas;
    }

    public String getNomeInstancia() {
        return nomeInstancia;
    }

    public boolean[][] getMatAdj() {
        return matAdj;
    }
}