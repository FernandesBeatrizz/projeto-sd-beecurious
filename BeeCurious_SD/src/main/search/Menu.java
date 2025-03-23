package main.search;
import java.util.Scanner;

public class Menu {
    private Cliente cliente;

    public Menu(Cliente cliente) {
        this.cliente = cliente;
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int opçao;

        do {
            System.out.println("\nEscolha uma opção");
            System.out.println("1 - indexar URLs");
            System.out.println("2 - indexar URLs recursivamente");
            System.out.println("3 - Pesquisar paginas");
            System.out.println("4 - Ordenar resultados por importância");
            System.out.println("5 - Consultar ligações para uma página");
            System.out.println("6 - Aprendizagem de palavras vazias");
            System.out.println("0- Sair");
            System.out.print("Qual opção quer: ");

            opçao = sc.nextInt();
            sc.nextLine();

            switch (opçao) {
                case 1:
                    break;
                case 2:
                    break;
                //case 3:
                    //pesquisarportermos(sc);
                    //break;
                case 4:
                    break;
                case 5:
                    break;
                case 6:
                    break;
                case 0:
                    System.exit(0);
                    break;
                default:
                    System.out.println("Opção inválida");
            }
        } while (opçao != 0);
        sc.close();
    }
}

/*
    private void pesquisarportermos(Scanner sc) {
        System.out.print();
        String termos=sc.nextLine();
        cliente.pesquisarconjtermos(sc);
    }
}*/
