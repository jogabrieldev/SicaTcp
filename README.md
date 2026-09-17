# Projeto SiCA

O SiCA (Sistema de Compartilhamento de Arquivos) é uma aplicação acadêmica simples para transferência de arquivos entre um cliente e um servidor usando sockets TCP.

## Tecnologias

- Java
- TCP Sockets (`ServerSocket` e `Socket`)
- Java I/O e NIO
- `DataInputStream` e `DataOutputStream`

## Arquitetura

```text
Cliente <-- TCP --> Servidor <--> arquivos/
                         |
                     ClientHandler
```

O cliente envia comandos ao servidor. O servidor processa as solicitações e mantém os arquivos compartilhados na pasta `arquivos/`.

## Funcionalidades

- Upload de arquivos para o servidor;
- Listagem dos arquivos disponíveis;
- Download de arquivos para a pasta `downloads/`;
- Encerramento da conexão.

## Como executar

### 1. Compilar

Na raiz do projeto, execute:

```bash
mvn compile
```

### 2. Iniciar o servidor

Em um terminal:

```bash
java -cp target/classes server.Server
```

O servidor será iniciado em `localhost:5000`.

### 3. Iniciar o cliente

Em outro terminal:

```bash
java -cp target/classes client.Client
```

### 4. Usar o sistema

No menu do cliente:

1. Informe o caminho de um arquivo local para fazer upload;
2. Escolha a opção de listagem para consultar `arquivos/`;
3. Informe o nome de um arquivo disponível para fazer download;
4. Escolha `0` para encerrar a conexão.

## Funcionamento do protocolo

O cliente envia um dos comandos `UPLOAD`, `LIST`, `DOWNLOAD` ou `EXIT` através do socket TCP. Para upload e download, além do comando são transmitidos o nome, o tamanho e os bytes do arquivo. Os dados são transferidos em blocos de 4096 bytes, evitando carregar o arquivo inteiro na memória.

Os nomes recebidos pelo servidor são mantidos dentro da pasta `arquivos/`, evitando acesso a caminhos externos ao diretório compartilhado.
