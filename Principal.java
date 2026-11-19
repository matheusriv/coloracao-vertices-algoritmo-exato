import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Scanner;

public class Principal {

    private static final String DIR_INSTANCIAS = "instancias";
    private static final String DIR_RELATORIOS = "relatorios";

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        long timeoutMs = 300000;

        new File(DIR_INSTANCIAS).mkdirs();
        new File(DIR_RELATORIOS).mkdirs();

        while (true) {
            System.out.println("\n=======================================================");
            System.out.println("   EXECUCAO DO ALGORITMO EXATO - COLORACAO DE GRAFOS   ");
            System.out.println("=======================================================");
            System.out.println("1 - Executar todas as instancias da pasta");
            System.out.println("2 - Selecionar e resolver uma instancia da pasta '" + DIR_INSTANCIAS + "/'");
            System.out.println("3 - Configurar tempo limite (Atual: " + (timeoutMs / 1000) + " s)");
            System.out.println("0 - Encerrar") ;
            System.out.print("Opcao: ");

            String opcao = sc.nextLine().trim();

            if (opcao.equals("0")) {
                System.out.println("Programa finalizado.");
                break;
            } else if (opcao.equals("2")) { 
                File[] listaArquivos = listarArquivosInstancia();
                if (listaArquivos == null || listaArquivos.length == 0) {
                    exibirAvisoPastaVazia();
                    continue;
                }

                System.out.println("\n--- Casos de teste encontrados da pasta '" + DIR_INSTANCIAS + "/' ---");
                for (int i = 0; i < listaArquivos.length; i++) {
                    System.out.printf("[%d] %s\n", (i + 1), listaArquivos[i].getName());
                }
                System.out.print("Escolha o numero do caso de teste (0 para cancelar): ");

                try {
                    int idx = Integer.parseInt(sc.nextLine().trim());
                    if (idx < 1 || idx > listaArquivos.length) {
                        continue;
                    }

                    File arquivoSelecionado = listaArquivos[idx - 1];
                    Grafo grafo = new Grafo();

                    System.out.println("\nProcessando '" + arquivoSelecionado.getName() + "'...");
                    if (grafo.carregarArquivoDIMACS(arquivoSelecionado)) {
                        AlgoritmoExato solver = new AlgoritmoExato(grafo, timeoutMs);
                        solver.executarBuscaExata();

                        String textoRelatorio = solver.gerarTextoRelatorio();
                        System.out.println("\n" + textoRelatorio);

                        String nomeSemExtensao = arquivoSelecionado.getName().replaceFirst("[.][^.]+$", "");
                        salvarArquivoRelatorio("relatorio_" + nomeSemExtensao + ".txt", textoRelatorio);
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Entrada invalida.");
                }

            } else if (opcao.equals("1"))  {
                File[] listaArquivos = listarArquivosInstancia();
                if (listaArquivos == null || listaArquivos.length == 0)  {
                    exibirAvisoPastaVazia();
                    continue;
                }

                StringBuilder relatorioUnificado = new StringBuilder();
                for (File arq : listaArquivos) {
                    System.out.println("Resolvendo: " + arq.getName() + "...");
                    Grafo grafo = new Grafo();
                    if (grafo.carregarArquivoDIMACS(arq)) {
                        AlgoritmoExato solver = new AlgoritmoExato(grafo, timeoutMs);
                        solver.executarBuscaExata();

                        String rel = solver.gerarTextoRelatorio();
                        System.out.println(rel);
                        relatorioUnificado.append(rel).append("\n\n");
                    }
                }
                salvarArquivoRelatorio("relatorio_bateria_completa.txt", relatorioUnificado.toString());

            } else if (opcao.equals("3")){
                try {
                    System.out.print("Informe o novo tempo limite em segundos: "); 
                    long seg = Long.parseLong(sc.nextLine().trim()) ;
                    if (seg > 0) {
                        timeoutMs = seg * 1000;
                        System.out.println("Tempo limite ajustado para " + seg + " segundos.");
                    }
                }  catch (NumberFormatException e) {
                    System.out.println("Valor invalido.");
                }
            } else {
                System.out.println("Opcao inexistente.");
            }
        }
        sc.close() ;
    }

    private static File[] listarArquivosInstancia() {
        File dir = new File(DIR_INSTANCIAS);
        File[] arquivos = dir.listFiles((d, nome) -> nome.endsWith(".col") || nome.endsWith(".txt"));
        if (arquivos != null) {
            Arrays.sort(arquivos);
        }
        return arquivos;
    } 

    private static void exibirAvisoPastaVazia() {
        System.out.println("\n[!] Nenhuma instancia (.col ou .txt) encontrada em '" + DIR_INSTANCIAS + "/'.");
        System.out.println("Adicione arquivos da literatura DIMACS na pasta ou execute 'java GeradorInstancias'.");
    }

    private static void salvarArquivoRelatorio(String nomeArquivo, String conteudo)  {
        File destino = new File(DIR_RELATORIOS, nomeArquivo);
        try (PrintWriter pw = new PrintWriter(new FileWriter(destino))) {
            pw.print(conteudo);
            System.out.println(">>> Relatorio gravado em arquivo: " + destino.getPath());
        } catch (IOException e) { 
            System.out.println("Erro ao gravar arquivo de relatorio: " + e.getMessage());
        }
    }
}