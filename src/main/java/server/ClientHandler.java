package server;

import shared.Protocol;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * Processa os comandos enviados por um cliente conectado ao servidor.
 */
public class ClientHandler implements Runnable {

    private final Socket socket;
    private final FileService fileService;

    public ClientHandler(Socket socket) throws IOException {
        this.socket = socket;
        this.fileService = new FileService();
    }

    /**
     * Lê comandos da conexão e direciona cada operação ao serviço de arquivos.
     */
    @Override
    public void run() {
        try (Socket client = socket;
             DataInputStream input = new DataInputStream(client.getInputStream());
             DataOutputStream output = new DataOutputStream(client.getOutputStream())) {

            boolean connected = true;
            while (connected) {
                String command = input.readUTF();
                switch (command) {
                    case Protocol.UPLOAD -> handleUpload(input, output);
                    case Protocol.LIST -> handleList(output);
                    case Protocol.DOWNLOAD -> handleDownload(input, output);
                    case Protocol.EXIT -> connected = false;
                    default -> output.writeUTF("Comando inválido.");
                }
                output.flush();
            }
        } catch (IOException exception) {
            System.out.println("Conexão encerrada: " + exception.getMessage());
        }
    }

    private void handleUpload(DataInputStream input, DataOutputStream output) throws IOException {
        String fileName = input.readUTF();
        long fileSize = input.readLong();
        fileService.receiveFile(input, fileName, fileSize);
        output.writeUTF("Arquivo enviado com sucesso.");
    }

    private void handleList(DataOutputStream output) throws IOException {
        var files = fileService.listFiles();
        output.writeInt(files.size());
        for (String file : files) {
            output.writeUTF(file);
        }
    }

    private void handleDownload(DataInputStream input, DataOutputStream output) throws IOException {
        String fileName = input.readUTF();
        fileService.sendFile(output, fileName);
    }
}
