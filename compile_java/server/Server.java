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
    private Set<String> userNames = new HashSet<>();
    private Set<User> users = new HashSet<>();
    private Map<Integer, Message> messages = new HashMap<>();
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
     * When a client is disconneted, removes the associated username and UserThread
     */
    void removeUser(String userName, User aUser) {
        boolean removed = userNames.remove(userName);
        if (removed) {
            users.remove(aUser);
            System.out.println("The user " + userName + " quitted");
        }
    }
    /**
     * Return all current userNames
     */
    Set<String> getUserNames() {
        return this.userNames;
    }
    /**
     * Returns true if there are other users connected (not count the currently connected user)
     */
    boolean hasUsers() {
        return !this.userNames.isEmpty();
    }
    /**
    * Add a new message to the server record
    */
    void addMessage(Message mes) {
        Server.messageID++;
        Integer id = Integer.valueOf(Server.messageID);
        mes.setId(id);
        this.messages.put(id, mes);
        System.out.println("sender:==="+mes.getSender());

    }
    /**
    * Return the message that has that id
    */
    Message getMessage(String id) {

        Integer num = Integer.parseInt(id);
        Message result=this.messages.get(num);
        if (num>Server.messageID){
            result=new Message("System",LocalDate.now().toString(),"invalid id(too large)", "invalid id(too large)");
        }
    
        return result;
    }

    /**
     * Stores username of the newly connected client.
     */
    void addUserName(String userName) {
        userNames.add(userName);
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
        public String userName;
        private String messageBuffer;
        private boolean quit = false;
        private boolean join = false;

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
                do {
                    if (this.join == false) {
                        tryJoin();
                    }
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
        * Send history mssage to user
        */
        void printHistory(User aUser){
            String messageBuffer;
            Message mes; 
            Integer currentId = messageID;
            Integer lastId = messageID - 1;
            if (messageID >= 2) {
                mes=getMessage(lastId.toString());
                messageBuffer = "Message ID: " + mes.getId() + ", Sender: [" + mes.getSender()  + "], Content: " + mes.getContent();
                aUser.sendMessage(messageBuffer);
                mes=getMessage(currentId.toString());
                messageBuffer = "Message ID: " + mes.getId() + ", Sender: [" + mes.getSender()  + "], Content: " + mes.getContent();
                aUser.sendMessage(messageBuffer);
            }else if(messageID==1){
                mes=getMessage(currentId.toString());
                messageBuffer = "Message ID: " + mes.getId() + ", Sender: [" + mes.getSender()  + "], Content: " + mes.getContent();
                aUser.sendMessage(messageBuffer);
            }
        }
        /**
         * Sends a list of online users to the newly connected user(Group)
         */
        void printUsers(Group group) {
            if (group.hasUsers()) {
                writer.println("Connected users: " + group.getUserNames());
            } else {
                writer.println("No other users connected");
            }
        }
        /**
         * Sends a list of online users to the newly connected user(Server).
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

        void handleCommand(String command, JSONObject receive) {
            Group result;
            String subject;
            String  content;
            Message newMes;
            System.out.println(this.userName + " executing: " + command);
            switch (command) {
                case "%groupjoin":
                    String groupFind = receive.getString("2");
                    this.userName = receive.getString("0");
                    result = server.getGroup(groupFind);
                    if (result != null) {
                        result.printHistory(this);
                        result.addUser(this.userName, this);
                        System.out.println(this.userName + " joined in");
                        messageBuffer = "New user connected: " + this.userName;
                        result.broadcast(messageBuffer, this);

                    }

                    break;
                case "%groups":
                    this.sendMessage(server.groups.values().toString());
                    break;
                case "%grouppost":
                    subject = receive.getString("3");
                    content = receive.getString("4");
                    newMes = new Message(this.userName,LocalDate.now().toString(),subject, content);
                    result = server.getGroup(receive.getString("2"));
                    if (result != null) {
                        result.addMessage(newMes);
                        this.messageBuffer = "Message ID: " + newMes.getId() + ", Sender: [" + newMes.getSender()
                                + "], Post Date: "
                                + newMes.getDate() + ", Subject: " + newMes.getSubject();
                        result.broadcast(messageBuffer, this);

                    }
                    break;
                case "%post":
                        subject = receive.getString("2");
                        content = receive.getString("3");
                        newMes = new Message(this.userName,LocalDate.now().toString(),subject, content);
                        server.addMessage(newMes);
                        this.messageBuffer = "Message ID: " + newMes.getId() + ", Sender: [" + newMes.getSender()
                        + "], Post Date: "
                        + newMes.getDate() + ", Subject: " + newMes.getSubject();
                        server.broadcast(messageBuffer, this);
                             // Todo: display recent history
                        

                    break;
                case "%users":
                    
                    this.printUsers();
                    
                    break;
                
                case "%groupusers":
                    result = server.getGroup(receive.getString("2"));
                    if (result != null) {
                        this.printUsers(result);
                    }
                    break;
                case "%leave":
                    leaveChat();
                    break;              
                case "%groupleave":
                    result = server.getGroup(receive.getString("2"));
                    if (result != null) {
                        this.leaveChat(result);
                    }
                    break;
                case "%message":
                    String mesId = receive.getString("2");
                    Message mes = server.getMessage(mesId);
                    if (mes != null) {
                        this.messageBuffer = "Message ID: " + mes.getId() + ", Sender: [" + this.userName + "], Content: " + mes.getContent();
                        this.sendMessage(messageBuffer);
                    } else {
                        this.sendMessage("Wrong Message ID");
                    }
                    
                    break;       
                case "%groupmessage":
                    String id = receive.getString("3");
                    result = server.getGroup(receive.getString("2"));
                    if (result != null) {

                        mes = result.getMessage(id);
                        if (mes != null) {
                            this.messageBuffer = "Message ID: " + mes.getId() + ", Sender: [" + mes.getSender()
                                + "], Post Date: "
                                + mes.getDate() + ", Subject: " + mes.getSubject();
                            this.sendMessage(messageBuffer);
                        } else {
                            this.sendMessage("Wrong Message ID or Group ID");
                        }
                    }
                    break;
                case "%exit":
                    leaveChat();
                    for (Group x : server.groups.values()) {
                        x.removeUser(this.userName, this);
                    }
                    this.quit = true;
                    break;
                default:
                    writer.println("Invalid command, please run %help for more information");
                    System.out.println("invalid command");
            }

        }
         /**
         *  Leave the current Server chat
         */
        void leaveChat() {
            server.removeUser(userName, this);
            messageBuffer = userName + " has quitted.";
            server.broadcast(messageBuffer, this);
            this.join = false;
        }
        /**
         * Leave the current Group chat
         */
        void leaveChat(Group group) {
            group.removeUser(userName, this);
            messageBuffer = userName + " has quitted Group:"+group.id;
            group.broadcast(messageBuffer, this);
        }
        /**
         * Wait client to join chat
         */
        void tryJoin() {
            JSONObject receive; 
            String command = "";
            String name;
            do{
                writer.println("Please use %join to join the server");
                receive = this.readMessage();
                command = receive.getString("1");
                System.out.println("command: "+command);
                if (command.equals("%join")) {
                    
                    this.join = true;
                }
            } while (this.join == false);
            

            name = receive.getString("0");
            this.userName = name;
            users.add(this);
            server.addUserName(name);
            printUsers();
            printHistory(this);
            System.out.println(this.userName+" joined in");
            messageBuffer = "New user connected: " + this.userName;
            server.broadcast(messageBuffer, this);
        }

        /**
         * Reads a Json message from the client.
         */
        JSONObject readMessage() {
            String result = "";
            byte[] temp = new byte[1024];
            try {

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
