package server;

import java.io.*;
import java.net.*;
import java.nio.charset.Charset;
import java.util.*;
import java.util.stream.Stream;
import java.time.LocalDate;
import com.alibaba.fastjson.*;
import com.alibaba.fastjson.JSONObject;

/**
 * This is the chat server program.
 * Press Ctrl + C to terminate the program.
 *
 * @author Lucas Wu/Leon Cai
 */
public class Server {
    private int port;
    private Set<User> users = new HashSet<>();
    private Map<Integer, Group> groups = new HashMap<>();
    public static int messageID = 0;

    public Server(int port) {
        this.port = port;
    }

    public void execute() {
        initGroups();
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

    void initGroups() {
        Group group1 = new Group("1", "final");
        Group group2 = new Group("2", "need sleep");
        Group group3 = new Group("3", "cse-3461");
        Group group4 = new Group("4", "A please");
        Group group5 = new Group("5", "buckeye");
        this.groups.put(1, group1);
        this.groups.put(2, group2);
        this.groups.put(3, group3);
        this.groups.put(4, group4);
        this.groups.put(5, group5);
    }

    Group getGroup(String find) {
        for (Group x : this.groups.values()) {
            if (x.id.equals(find) || x.name.equals(find)) {
                return x;
            }
        }
        System.out.println("not found");
        return null;
    }

    public class User extends Thread {
        private Socket socket;
        private Server server;
        private PrintWriter writer;
        private InputStream reader;
        private String userName;
        private String messageBuffer;
        private boolean quit = false;

        public User(Socket socket, Server server) {
            this.socket = socket;
            this.server = server;
        }

        public void run() {
            try {
                JSONObject receive;
                String command = "";
                this.reader = socket.getInputStream();

                OutputStream output = socket.getOutputStream();
                this.writer = new PrintWriter(output, true);
                System.out.println("running");
                do {

                    receive = this.readMessage();
                    command = receive.getString("1");
                    handleCommand(command, receive);
                    // server.broadcast(serverMessage, this);

                } while (this.quit == false);

                socket.close();

            } catch (IOException ex) {
                System.out.println("Error in UserThread: " + ex.getMessage());
                ex.printStackTrace();
            }
        }

        /**
         * Sends a list of online users to the newly connected user.
         */
        void printUsers(Group group) {
            if (group.hasUsers()) {
                writer.println("Connected users: " + group.getUserNames());
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

        void handleCommand(String command, JSONObject receive) {
            Group result;
            System.out.println(this.userName + " executing: " + command);
            switch (command) {
                case "groupjoin":
                    String groupFind = receive.getString("2");
                    this.userName = receive.getString("0");
                    result = server.getGroup(groupFind);
                    if (result != null) {
                        // this.hold = false;
                        result.addUser(this.userName, this);
                        System.out.println(this.userName + " joined in");
                        messageBuffer = "New user connected: " + this.userName;
                        result.broadcast(messageBuffer, this);

                    }

                    break;
                case "groups":
                    this.sendMessage(server.groups.values().toString());
                    break;
                case "grouppost":
                    String subject = receive.getString("3");
                    String content = receive.getString("4");
                    Message newMes = new Message(subject, content);
                    result = server.getGroup(receive.getString("2"));
                    if (result != null) {
                        result.addMessage(newMes);
                        this.messageBuffer = "Message ID: " + newMes.getId() + ", Sender: [" + this.userName
                                + "], Post Date: "
                                + LocalDate.now().toString() + ", Subject: " + newMes.getSubject();
                        result.broadcast(messageBuffer, this);

                    }
                    break;
                case "groupusers":
                    result = server.getGroup(receive.getString("2"));
                    if (result != null) {
                        this.printUsers(result);
                    }
                    break;
                case "groupleave":
                    result = server.getGroup(receive.getString("2"));
                    if (result != null) {
                        this.leaveChat(result);
                    }
                    break;
                case "groupmessage":
                    String id = receive.getString("3");
                    result = server.getGroup(receive.getString("2"));
                    if (result != null) {

                        Message mes = result.getMessage(id);
                        if (mes != null) {
                            this.messageBuffer = "Message ID: " + mes.getId() + ", Sender: [" + this.userName
                                    + "], Content: " + mes.getContent();
                            this.sendMessage(messageBuffer);
                        } else {
                            this.sendMessage("Wrong Message ID or Group ID");
                        }
                    }
                    break;
                case "exit":
                    for (Group x : server.groups.values()) {
                        x.removeUser(this.userName, this);
                    }
                    this.quit = true;
                    break;
                default:
                    System.out.println("invalid command");
            }

        }

        /**
         * Leave the current chat
         */
        void leaveChat(Group group) {
            group.removeUser(userName, this);
            messageBuffer = userName + " has quitted.";
            group.broadcast(messageBuffer, this);
        }

        /**
         * Reads a Json message from the client.
         */
        JSONObject readMessage() {
            String result = "";
            byte[] temp = new byte[1024];
            try {
                System.out.println("reading");

                reader.read(temp);
                result = new String(temp, "ascii");

                System.out.println("temp: " + temp);
                System.out.println("result: " + result);

            } catch (IOException e) {
                System.out.println("Error in UserThread: " + e.getMessage());
                e.printStackTrace();
            }
            return JSON.parseObject(result);

        }
    }

}
