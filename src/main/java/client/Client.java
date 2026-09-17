package client;

import shared.Protocol;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

/**
 * Cliente do SiCA, responsável por conectar-se ao servidor e executar operações de arquivos.
 */
public class Client {

    private static final String HOST = "localhost";
    private static final int PORT = 5000;
    private static final int BUFFER_SIZE = 4096;

    /**
     * Estabelece a conexão TCP e processa o menu até o usuário encerrá-la.
     */
    public void connect() {
        try (Socket socket = new Socket(HOST, PORT);
             DataInputStream input = new DataInputStream(socket.getInputStream());
             DataOutputStream output = new DataOutputStream(socket.getOutputStream());
             Scanner scanner = new Scanner(System.in)) {
            System.out.println("Conectado ao servidor.");
            showMenu(scanner, input, output);
        } catch (IOException exception) {
            System.out.println("Não foi possível conectar ao servidor.");
        }
    }

    private void showMenu(Scanner scanner, DataInputStream input, DataOutputStream output) throws IOException {
        boolean running = true;
        while (running) {
            System.out.println();
            System.out.println("===== SICA =====");
            System.out.println();
            System.out.println("1 - Enviar arquivo");
            System.out.println("2 - Listar arquivos");
            System.out.println("3 - Baixar arquivo");
            System.out.println("0 - Sair");
            System.out.println();
            System.out.print("Escolha uma opção: ");

            String option = scanner.nextLine();
            switch (option) {
                case "1" -> uploadFile(scanner, input, output);
                case "2" -> listFiles(input, output);
                case "3" -> downloadFile(scanner, input, output);
                case "0" -> {
                    output.writeUTF(Protocol.EXIT);
                    output.flush();
                    running = false;
                    System.out.println("Conexão encerrada.");
                }
                default -> System.out.println("Opção inválida.");
            }
        }
    }

    /** Envia o nome, tamanho e bytes de um arquivo local ao servidor. */
    private void uploadFile(Scanner scanner, DataInputStream input, DataOutputStream output) throws IOException {
        System.out.print("Digite o caminho do arquivo: ");
        Path source = Paths.get(scanner.nextLine());
        if (!Files.isRegularFile(source)) {
            System.out.println("Arquivo não encontrado.");
            return;
        }

        output.writeUTF(Protocol.UPLOAD);
        output.writeUTF(source.getFileName().toString());
        output.writeLong(Files.size(source));
        try (InputStream fileInput = Files.newInputStream(source)) {
            fileInput.transferTo(output);
        }
        output.flush();
        System.out.println(input.readUTF());
    }

    /** Solicita ao servidor a lista de arquivos compartilhados. */
    private void listFiles(DataInputStream input, DataOutputStream output) throws IOException {
        output.writeUTF(Protocol.LIST);
        output.flush();

        int amount = input.readInt();
        if (amount == 0) {
            System.out.println("Nenhum arquivo disponível no servidor.");
            return;
        }

        System.out.println("===== ARQUIVOS DISPONÍVEIS =====");
        for (int index = 1; index <= amount; index++) {
            System.out.println(index + " - " + input.readUTF());
        }
    }

    /** Solicita um arquivo e grava os bytes recebidos na pasta downloads/. */
    private void downloadFile(Scanner scanner, DataInputStream input, DataOutputStream output) throws IOException {
        System.out.print("Digite o nome do arquivo: ");
        String fileName = scanner.nextLine();
        output.writeUTF(Protocol.DOWNLOAD);
        output.writeUTF(fileName);
        output.flush();

        if (!input.readBoolean()) {
            System.out.println("Arquivo não encontrado no servidor.");
            return;
        }

        long fileSize = input.readLong();
        Path destination = Paths.get("downloads").resolve(Paths.get(fileName).getFileName());
        Files.createDirectories(destination.getParent());
        byte[] buffer = new byte[BUFFER_SIZE];
        long remaining = fileSize;
        try (var fileOutput = Files.newOutputStream(destination)) {
            while (remaining > 0) {
                int bytesRead = input.read(buffer, 0, (int) Math.min(buffer.length, remaining));
                if (bytesRead == -1) {
                    throw new IOException("Conexão encerrada durante o download.");
                }
                fileOutput.write(buffer, 0, bytesRead);
                remaining -= bytesRead;
            }
        }
        System.out.println("Download realizado com sucesso.");
        System.out.println("Arquivo salvo em: " + destination);
    }

    public static void main(String[] args) {
        new Client().connect();
    }
}
