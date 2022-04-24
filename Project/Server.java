import java.io.*;
import java.net.*;
import java.util.*;
import com.alibaba.fastjson.*;

/**
 * This is the chat server program.
 * Press Ctrl + C to terminate the program.
 *
 * @author www.codejava.net
 */
public class Server {
    private int port;
    private Set<String> userNames = new HashSet<>();
    private Set<User> users = new HashSet<>();
 
    public Server(int port) {
        this.port = port;
    }
 
    public void execute() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
 
            System.out.println("Chat Server is listening on port " + port);
 
            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("New user connected");
 
                User newUser = new User(socket, this);
                users.add(newUser);
                newUser.start();
 
            }
 
        } catch (IOException ex) {
            System.out.println("Error in the server: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
 
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Syntax: java ChatServer <port-number>");
            System.exit(0);
        }
 
        int port = Integer.parseInt(args[0]);
 
        Server server = new Server(port);
        server.execute();
    }
 
    /**
     * Delivers a message from one user to others (broadcasting)
     */
    void broadcast(String message, User excludeUser) {
        for (User aUser : users) {
            if (aUser != excludeUser) {
                aUser.sendMessage(message);
            }
        }
    }
 
    /**
     * Stores username of the newly connected client.
     */
    void addUserName(String userName) {
        userNames.add(userName);
    }
 
    /**
     * When a client is disconneted, removes the associated username and UserThread
     */
    void removeUser(String userName, User aUser) {
        boolean removed = userNames.remove(userName);
        if (removed) {
            users.remove(aUser);
            System.out.println("The user " + userName + " quitted");
        }
    }
 
    Set<String> getUserNames() {
        return this.userNames;
    }
 
    /**
     * Returns true if there are other users connected (not count the currently connected user)
     */
    boolean hasUsers() {
        return !this.userNames.isEmpty();
    }
    public class User extends Thread {
        private Socket socket;
        private Server server;
        private PrintWriter writer;
        private BufferedReader reader;
     
        public User(Socket socket, Server server) {
            this.socket = socket;
            this.server = server;
        }
     
        public void run() {
            try {
                InputStream input = socket.getInputStream();
                this.reader = new BufferedReader(new InputStreamReader(input));
     
                OutputStream output = socket.getOutputStream();
                this.writer = new PrintWriter(output, true);
     
                printUsers();
                JSONObject receive; 
                String command = "";
                String userName;
                do{
                   
                    receive = this.readMessage();
                    command= receive.getString("1");
                }
                while (!command.equals("join"));
                userName = receive.getString("0");
                server.addUserName(userName);
     
                String serverMessage = "New user connected: " + userName;
                server.broadcast(serverMessage, this);
     
                String clientMessage;
     
                do {
                    clientMessage = reader.readLine();
                    serverMessage = "[" + userName + "]: " + clientMessage;
                    server.broadcast(serverMessage, this);
     
                } while (!clientMessage.equals("bye"));
     
                server.removeUser(userName, this);
                socket.close();
     
                serverMessage = userName + " has quitted.";
                server.broadcast(serverMessage, this);
     
            } catch (IOException ex) {
                System.out.println("Error in UserThread: " + ex.getMessage());
                ex.printStackTrace();
            }
        }
     
        /**
         * Sends a list of online users to the newly connected user.
         */
        void printUsers() {
            if (server.hasUsers()) {
                writer.println("Connected users: " + server.getUserNames());
            } else {
                writer.println("No other users connected");
            }
        }
     
        /**
         * Sends a message to the client.
         */
        void sendMessage(String message) {
            writer.println(message);
        }

        JSONObject readMessage() {
            String temp="";
            try {
                temp = reader.readLine();
                
                
            } catch (IOException e) {
                System.out.println("Error in UserThread: " + e.getMessage());
                e.printStackTrace();
            }
            return JSON.parseObject(temp);

        }
    }
    
}

