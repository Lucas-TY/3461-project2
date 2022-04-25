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
    private Group defaultGroup=new Group("Server","defaultGroup");
    public static int messageID = 0;

    public Server(int port) {
        this.port = port;
    }
    /**
     * Waiting for new client to connect
    */
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
    /**
    * Main method
    */
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
     * init all groups
    */
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
    /**
    * Get a group in server
    */
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
        private String messageBuffer;
        private boolean quit = false;
        private boolean join = false;

        public User(Socket socket, Server server) {
            this.socket = socket;
            this.server = server;
        }
        /**
        * User child thread
        */
        public void run() {
            try {
                JSONObject receive;
                String command = "";
                String userName="";
                this.reader = socket.getInputStream();

                OutputStream output = socket.getOutputStream();
                this.writer = new PrintWriter(output, true);
                do {
                    receive = this.readMessage(this);
                    if (this.quit == false){
                        command = receive.getString("1");
                        userName = receive.getString("0");
                        handleCommand(userName,command, receive);
                    }
                    
                    // server.broadcast(serverMessage, this);

                } while (this.quit == false);

                socket.close();

            } catch (IOException ex) {
                System.out.println("Error in UserThread: " + ex.getMessage());
                ex.printStackTrace();
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
         * Sends a message to the client.
         */
        void sendMessage(String message) {
            writer.println(message);
        }
        /**
         * Execute command
         */
        void handleCommand(String userName,String command, JSONObject receive) {
            Group result;
            String subject;
            String  content;
            Message newMes;
            String groupFind;
            System.out.println(userName + " executing: " + command);
            switch (command) {
                case "%help":
                    this.help();
                    break;
                case "%join":
                    groupFind = receive.getString("2");
                    result = server.defaultGroup;
                    
                    
                    result.addUser(userName, this);
                    result.printHistory(this);
                    printUsers(result);
                    System.out.println(userName + " joined in");
                    messageBuffer = "New user connected: " + userName;
                    result.broadcast(messageBuffer, this);
                    break;
                case "%groupjoin":
                    groupFind = receive.getString("2");
                    result = server.getGroup(groupFind);
                    if (result != null) {
                        result.addUser(userName, this);
                        result.printHistory(this);
                        this.sendMessage("connected to [Group: "+result.id+"("+result.name+")]");
                        printUsers(result);
                        System.out.println(userName + " joined in");
                        messageBuffer = "New user connected: " + userName;
                        result.broadcast(messageBuffer, this);

                    }

                    break;
                case "%groups":
                    this.sendMessage(server.groups.values().toString());
                    break;
                case "%grouppost":
                    subject = receive.getString("3");
                    content = receive.getString("4");
                    newMes = new Message(userName,LocalDate.now().toString(),subject, content);
                    result = server.getGroup(receive.getString("2"));
                    if (result != null) {
                        result.addMessage(newMes);
                        this.messageBuffer = "[Group: "+result.id+"("+result.name+")] Message ID: " + newMes.getId() + ", Sender: [" + newMes.getSender()
                                + "], Post Date: "
                                + newMes.getDate() + ", Subject: " + newMes.getSubject();
                        result.broadcast(messageBuffer, this);

                    }
                    break;
                case "%post":
                        subject = receive.getString("2");
                        content = receive.getString("3");
                        newMes = new Message(userName,LocalDate.now().toString(),subject, content);
                        result = server.defaultGroup;
                        result.addMessage(newMes);
                        this.messageBuffer = "Message ID: " + newMes.getId() + ", Sender: [" + newMes.getSender()
                                + "], Post Date: "
                                + newMes.getDate() + ", Subject: " + newMes.getSubject();
                        result.broadcast(messageBuffer, this);                        
                    break;
                case "%users":
                    result=server.defaultGroup;
                    this.printUsers(result);
                    break;
                
                case "%groupusers":
                    result = server.getGroup(receive.getString("2"));
                    if (result != null) {
                        this.printUsers(result);
                    }
                    break;
                case "%leave":
                    result = server.defaultGroup;
                
                    this.leaveChat(result,userName);
                
                    break;              
                case "%groupleave":
                    result = server.getGroup(receive.getString("2"));
                    if (result != null) {
                        this.leaveChat(result,userName);
                    }
                    break;
                case "%message":
                    String mesId = receive.getString("2");
                    result=server.defaultGroup;
                    Message mes = result.getMessage(mesId);
                    if (mes != null) {
                        this.messageBuffer = "Message ID: " + mes.getId() + ", Sender: [" + userName + "], Content: " + mes.getContent();
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
                            this.messageBuffer = "[Group: "+result.id+"("+result.name+")] Message ID: " + mes.getId() + ", Sender: [" + mes.getSender()
                                + "], Post Date: "
                                + mes.getDate() + ", Subject: " + mes.getSubject();
                            this.sendMessage(messageBuffer);
                        } else {
                            this.sendMessage("Wrong Message ID or Group ID");
                        }
                    }
                    break;
                case "%exit":
                    leaveChat(server.defaultGroup,userName);
                    for (Group x : server.groups.values()) {
                        leaveChat(x,userName);
                    }
                    this.sendMessage("Disconnect");
                    this.quit = true;
                    break;
                default:
                    writer.println("Invalid command, please run %help for more information");
                    System.out.println("invalid command");
            }

        }
        /**
         * Leave the current Group chat
         */
        void leaveChat(Group group,String userName) {
            group.removeUser(userName, this);
            
        }
        /**
         * User guide
         */
        void help(){
            try {
                BufferedReader in = new BufferedReader(new FileReader("server/help.txt"));
                String str;
                while ((str = in.readLine()) != null) {
                    this.sendMessage(str);
                    System.out.println(str);
                }
                in.close();
            } catch (IOException e) {
                System.out.println("file error"+e);
            }
            
        }

        /**
         * Reads a Json message from the client.
         */
        JSONObject readMessage(User aUser) {
            String result = "";
            byte[] temp = new byte[1024];
            try {

                reader.read(temp);
                result = new String(temp, "ascii");
                System.out.println("result: " + result);

            } catch (IOException e) {
                System.out.println("Client Interrupt" + e.getMessage());
                aUser.quit=true;
            }
            return JSON.parseObject(result);

        }
    }

}
