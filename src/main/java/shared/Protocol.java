package shared;

/**
 * Define os comandos utilizados na comunicação entre cliente e servidor.
 */
public final class Protocol {

    public static final String UPLOAD = "UPLOAD";
    public static final String LIST = "LIST";
    public static final String DOWNLOAD = "DOWNLOAD";
    public static final String EXIT = "EXIT";

    private Protocol() {
        // Classe utilitária: não deve ser instanciada.
    }
}
