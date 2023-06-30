import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class Servidor {
    private static final int PORTA = 12345;
    private static final Map<String, String> USUARIOS = new HashMap<>();

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

                        // Lê as credenciais enviadas pelo cliente
                        String credentials = reader.readLine();
                        String[] parts = credentials.split(":");
                        String username = parts[0];
                        String password = parts[1];

                        // Autentica as credenciais
                        boolean authenticated = authenticate(username, password);

                        // Envia resposta de autenticação para o cliente
                        if (authenticated) {
                            outputStream.write("Autenticado\n".getBytes());
                            outputStream.flush();

                            while (true) {
                                // Lê as mensagens enviadas pelo cliente
                                String message = reader.readLine();
                                if (message == null || message.equalsIgnoreCase("sair")) {
                                    break; // Encerra o loop se o cliente digitar 'sair' ou a conexão for encerrada
                                }

                                // Processa a mensagem recebida
                                System.out.println("Mensagem recebida do cliente: " + message);
                                // Adicione aqui a lógica para processar a mensagem do cliente

                                // Envia a resposta ao cliente
                                String response = "Mensagem recebida\n";
                                outputStream.write(response.getBytes());
                                outputStream.flush();
                            }
                        } else {
                            outputStream.write("Falha na autenticacao\n".getBytes());
                            outputStream.flush();
                        }

                        // Fecha os recursos
                        reader.close();
                        outputStream.close();
                        clienteSocket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
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
}
