import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class ServidorCripto {
    private static final int PORTA = 12345;
    private static final Map<String, String> USUARIOS = new HashMap<>();
    private static final String ENCRYPTION_KEY = "myEncryptionKey"; // Chave de criptografia

    static {
        USUARIOS.put("leonoronha", "12345");
    }

    public static void main(String[] args) {
        try {
            ServerSocket servidorSocket = new ServerSocket(PORTA);
            System.out.println("Servidor iniciado. Aguardando conexões...");

            while (true) {
                Socket clienteSocket = servidorSocket.accept();
                System.out.println("Cliente conectado: " + clienteSocket.getInetAddress().getHostAddress());

                // Thread separada para cada cliente
                Thread thread = new Thread(() -> {
                    try {
                        BufferedReader reader = new BufferedReader(new InputStreamReader(clienteSocket.getInputStream()));
                        OutputStream outputStream = clienteSocket.getOutputStream();

                        // Lê as credenciais criptografadas enviadas pelo cliente
                        String encryptedCredentials = reader.readLine();

                        // Descriptografa as credenciais
                        String decryptedCredentials = decryptCredentials(encryptedCredentials);
                        String[] parts = decryptedCredentials.split(":");
                        String username = parts[0];
                        String password = parts[1];

                        // Autentica as credenciais
                        boolean authenticated = authenticate(username, password);

                        // Envia resposta de autenticação criptografada para o cliente
                        String encryptedResponse = encryptCredentials(authenticated ? "Autenticado" : "Falha na autenticação");
                        outputStream.write(encryptedResponse.getBytes());
                        outputStream.flush();

                        if (authenticated) {
                            while (true) {
                                // Lê as mensagens enviadas pelo cliente (em texto simples)
                                String message = reader.readLine();
                                if (message == null || message.equalsIgnoreCase("sair")) {
                                    break; // Encerra o loop se o cliente digitar 'sair' ou a conexão for encerrada
                                }

                                // Processa a mensagem recebida
                                System.out.println("Mensagem recebida do cliente: " + message);
                                // Adicione aqui a lógica para processar a mensagem do cliente

                                // Envia a resposta ao cliente (em texto simples)
                                String response = "Mensagem recebida\n";
                                outputStream.write(response.getBytes());
                                outputStream.flush();
                            }
                        }

                        // Fecha os recursos
                        reader.close();
                        outputStream.close();
                        clienteSocket.close();
                    } catch (IOException e) {
                        if (e instanceof java.net.SocketException) {
                            System.out.println("Conexão encerrada pelo cliente.");
                        } else {
                            e.printStackTrace();
                        }
                    }
                });

                // Inicia a thread do cliente
                thread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static boolean authenticate(String username, String password) {
        String storedPassword = USUARIOS.get(username);
        return storedPassword != null && storedPassword.equals(password);
    }

    private static String encryptCredentials(String message) {
        try {
            SecretKeySpec secretKey = new SecretKeySpec(ENCRYPTION_KEY.getBytes(), "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] encryptedBytes = cipher.doFinal(message.getBytes());
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static String decryptCredentials(String encryptedMessage) {
        try {
            SecretKeySpec secretKey = new SecretKeySpec(ENCRYPTION_KEY.getBytes(), "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            byte[] encryptedBytes = Base64.getDecoder().decode(encryptedMessage);
            byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
            return new String(decryptedBytes);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
