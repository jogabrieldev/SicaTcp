package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Inicia o servidor TCP do SiCA e aguarda conexões de clientes.
 */
public class Server {

    public static final int PORT = 5000;

    /**
     * Abre a porta do servidor e aceita conexões TCP.
     *
     * Nesta etapa, as conexões são apenas aceitas e registradas.
     * O processamento das requisições será adicionado posteriormente.
     */
    public void startServer() {
        System.out.println("===== SICA SERVIDOR =====");
        System.out.println();
        System.out.println("Servidor iniciado na porta " + PORT + ".");
        System.out.println("Aguardando conexões...");

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (!Thread.currentThread().isInterrupted()) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Cliente conectado: " + clientSocket.getRemoteSocketAddress());
                Thread clientThread = new Thread(() -> {
                    try {
                        new ClientHandler(clientSocket).run();
                    } catch (IOException exception) {
                        System.out.println("Não foi possível atender o cliente: " + exception.getMessage());
                    }
                });
                clientThread.start();
            }
        } catch (IOException exception) {
            System.out.println("Erro ao executar o servidor: " + exception.getMessage());
        }
    }

    public static void main(String[] args) {
        new Server().startServer();
    }
}
