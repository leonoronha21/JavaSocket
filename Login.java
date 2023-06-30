import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;

public class Login {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 12345;

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
                    	  System.out.print("Conexão encerrada");
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

        // Envia as credenciais para o servidor
        String credentials = username + ":" + password + "\n";
        outputStream.write(credentials.getBytes());
        outputStream.flush();

        // Lê a resposta do servidor
        String response = reader.readLine();

        return response.equals("Autenticado");
    }
}
