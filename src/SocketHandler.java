import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class SocketHandler implements Runnable {
    private final String LOGIN_RESPONSE_KEY = "LOGIN_SUCCESSFUL";
    private final String REGISTER_KEY = "REGISTER";
    private final String LOGIN_KEY = "LOGIN";

    private String name;
    private Socket socket;
    private PrintWriter writer;
    private BufferedReader reader;
    private String username;

    public SocketHandler(Socket socket) {
        this.socket = socket;
        this.name = socket.getInetAddress().toString();
    }

    @Override
    public void run() {
        try {
            System.out.println("Creating reader/writer");
            writer = new PrintWriter(socket.getOutputStream(), true);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            
            Server.writeHistory(writer);
            
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith(REGISTER_KEY)) {
                    String[] register = line.substring(REGISTER_KEY.length(), line.length() - 1).split(" ");
                    if (Server.registerUser(register[0], register[1])) {
                        this.username = register[0];
                        System.out.println("Registered user " + this.username);
                        this.write(LOGIN_RESPONSE_KEY);
                    }

                } else if (line.startsWith(LOGIN_KEY)) {
                    String[] login = line.substring(LOGIN_KEY.length(), line.length() - 1).split(" ");
                    if (Server.loginUser(login[0], login[1])) {
                        this.username = login[0];
                        System.out.println("User " + this.username + " logged in.");
                        this.write(LOGIN_RESPONSE_KEY);
                    }
                    
                } else {
                    System.out.println("Read " + line + " from socket");
                    Server.writeMessage(line);
                }

            }
            
            Server.clearClient(this);
        } catch (IOException e) {
            Server.clearClient(this);
            e.printStackTrace();
        }
    }

    public void write(String message) {
        writer.write(message + "\r\n");
        writer.flush();
    }

    public String getName() {
        return name;
    }
}