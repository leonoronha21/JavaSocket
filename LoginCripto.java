import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

public class LoginCripto {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 12345;
    private static final String ENCRYPTION_KEY = "myEncryptionKey"; // Chave de criptografia

    public static void main(String[] args) {
        try {
            // Estabelece a conexão com o servidor
            Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
            System.out.println("Conectado ao servidor: " + SERVER_ADDRESS);

            // Obtém os streams de entrada e saída
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            OutputStream outputStream = socket.getOutputStream();

            // Autenticação do usuário
            boolean authenticated = authenticate(reader, outputStream);

            if (authenticated) {
                System.out.println("Usuário autenticado com sucesso.");

                // Loop para enviar mensagens ao servidor
                BufferedReader userInputReader = new BufferedReader(new InputStreamReader(System.in));
                String userInput;
                while (true) {
                    System.out.print("Digite a mensagem (ou 'sair' para encerrar): ");
                    userInput = userInputReader.readLine();

                    // Envia a mensagem para o servidor
                    outputStream.write(userInput.getBytes());
                    outputStream.write('\n');
                    outputStream.flush();

                    if (userInput.equalsIgnoreCase("sair")) {
                        System.out.println("Conexão encerrada");
                        break; // Encerra o loop se o usuário digitar 'sair'
                    }
                }
            } else {
                System.out.println("Falha na autenticação do usuário. Encerrando conexão.");
            }

            // Fecha os recursos
            reader.close();
            outputStream.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static boolean authenticate(BufferedReader reader, OutputStream outputStream) throws IOException {
        BufferedReader userInputReader = new BufferedReader(new InputStreamReader(System.in));

        System.out.println("=== Autenticação ===");
        System.out.print("Digite o nome de usuário: ");
        String username = userInputReader.readLine();
        System.out.print("Digite a senha: ");
        String password = userInputReader.readLine();

        // Criptografa as credenciais
        String encryptedCredentials = encryptCredentials(username + ":" + password);

        // Envia as credenciais criptografadas para o servidor
        outputStream.write(encryptedCredentials.getBytes());
        outputStream.flush();

        // Lê a resposta de autenticação criptografada do servidor
        String encryptedResponse = reader.readLine();

        // Descriptografa a resposta de autenticação
        String decryptedResponse = decryptCredentials(encryptedResponse);

        return decryptedResponse.equals("Autenticado");
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
