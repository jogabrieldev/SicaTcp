package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

/**
 * Centraliza as operações de arquivos realizadas pelo servidor.
 */
public class FileService {

    private static final int BUFFER_SIZE = 4096;

    private final Path sharedDirectory;

    public FileService() throws IOException {
        this(Paths.get("arquivos"));
    }

    public FileService(Path sharedDirectory) throws IOException {
        this.sharedDirectory = sharedDirectory;
        Files.createDirectories(sharedDirectory);
    }

    /**
     * Lista os arquivos disponíveis na pasta compartilhada do servidor.
     * Diretórios e outros tipos de entrada não são incluídos no resultado.
     *
     * @return nomes dos arquivos ordenados alfabeticamente
     * @throws IOException se a pasta não puder ser consultada
     */
    public List<String> listFiles() throws IOException {
        try (Stream<Path> files = Files.list(sharedDirectory)) {
            return files
                    .filter(Files::isRegularFile)
                    .map(path -> path.getFileName().toString())
                    .sorted(Comparator.naturalOrder())
                    .toList();
        }
    }

    /**
     * Recebe os bytes de um arquivo enviado pelo cliente e grava-os no servidor.
     * O tamanho informado controla exatamente quantos bytes são lidos do socket.
     *
     * @param input fluxo de entrada da conexão TCP
     * @param fileName nome do arquivo a ser salvo
     * @param fileSize quantidade de bytes esperada
     * @throws IOException se o nome ou a transferência forem inválidos
     */
    public void receiveFile(DataInputStream input, String fileName, long fileSize) throws IOException {
        if (fileSize < 0) {
            throw new IOException("Tamanho de arquivo inválido.");
        }

        Path destination = resolveSharedFile(fileName);

        byte[] buffer = new byte[BUFFER_SIZE];
        long remaining = fileSize;
        try (OutputStream output = Files.newOutputStream(destination)) {
            while (remaining > 0) {
                int bytesToRead = (int) Math.min(buffer.length, remaining);
                int bytesRead = input.read(buffer, 0, bytesToRead);
                if (bytesRead == -1) {
                    throw new IOException("Conexão encerrada durante o recebimento.");
                }
                output.write(buffer, 0, bytesRead);
                remaining -= bytesRead;
            }
        }
    }

    /**
     * Envia um arquivo ao cliente, informando antes se ele foi encontrado.
     * Quando encontrado, o tamanho e os bytes são enviados pelo socket.
     *
     * @param output fluxo de saída da conexão TCP
     * @param fileName nome do arquivo solicitado
     * @throws IOException se o arquivo ou a transferência forem inválidos
     */
    public void sendFile(DataOutputStream output, String fileName) throws IOException {
        Path source = resolveSharedFile(fileName);
        if (!Files.isRegularFile(source)) {
            output.writeBoolean(false);
            output.flush();
            return;
        }

        output.writeBoolean(true);
        output.writeLong(Files.size(source));

        byte[] buffer = new byte[BUFFER_SIZE];
        try (var input = Files.newInputStream(source)) {
            int bytesRead;
            while ((bytesRead = input.read(buffer)) != -1) {
                output.write(buffer, 0, bytesRead);
            }
        }
        output.flush();
    }

    private Path resolveSharedFile(String fileName) throws IOException {
        if (fileName == null || fileName.isBlank()) {
            throw new IOException("Nome de arquivo inválido.");
        }

        Path destination = sharedDirectory.resolve(fileName).normalize();
        Path normalizedDirectory = sharedDirectory.toAbsolutePath().normalize();
        Path normalizedDestination = destination.toAbsolutePath().normalize();
        if (!normalizedDestination.getParent().equals(normalizedDirectory)) {
            throw new IOException("Nome de arquivo inválido.");
        }
        return destination;
    }
}
