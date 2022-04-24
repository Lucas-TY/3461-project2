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
    public static int messageID = 0;
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

    }
    /**
    * Return the message that has that id
    */
    Message getMessage(String id) {

            Integer num = Integer.parseInt(id);
            Message result=this.messages.get(num);
            if (id>Server.messageID){
                result=new Message("error", "invalid id(too large)");
            }
        
            return result;
    }
        
    


    public class User extends Thread {
        private Socket socket;
        private Server server;
        private PrintWriter writer;
        private InputStream reader;
        private String userName;
        private String messageBuffer;
        private boolean hold = true;
        private boolean quit=false;
        private int groupID;
     
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
                    if (this.hold == true) {
                        tryJoin();
                    }
                    receive = this.readMessage();
                    command= receive.getString("1");
                    handleCommand(command,receive);
                    //server.broadcast(serverMessage, this);
     
                } while (this.quit==false);
     
            
                socket.close();
     
                
     
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

        void handleCommand(String command, JSONObject receive) {
            System.out.println(this.userName+"executing: " + command);
            switch (command) {
                case "post":
                    String subject = receive.getString("2");
                    String content = receive.getString("3");
                    Message newMes = new Message(subject, content);
                    server.addMessage(newMes);
                    this.messageBuffer = "Message ID: " + newMes.getId() + ", Sender: [" + this.userName + "], Post Date: "
                            + LocalDate.now().toString() + ", Subject: " + newMes.getSubject();
                    server.broadcast(messageBuffer, this);
                    break;
                case "users":
                    this.printUsers();
                    break;
                case "leave":
                    leaveChat();
                    break;
                case "message":
                    String id = receive.getString("2");
                    Message mes=server.getMessage(id);
                    if (mes!=null){
                        this.messageBuffer="Message ID: " + mes.getId() + ", Sender: [" + this.userName + "], Content: " + mes.getContent();
                        this.sendMessage(messageBuffer);
                    }
                    break;
                case "exit":
                    leaveChat();
                    this.quit = true;
                    break;
                default:
                    System.out.println("invalid command");
            }

        }
        /**
         *  Leave the current chat
         */
        void leaveChat() {
            server.removeUser(userName, this);
            messageBuffer = userName + " has quitted.";
            server.broadcast(messageBuffer, this);
            this.hold = true;
        }
        /**
         * Wait client to join chat
         */
        void tryJoin() {
            JSONObject receive; 
            String command = "";
            do{
                   
                receive = this.readMessage();
                command = receive.getString("1");
                System.out.println("command: "+command);
                if (command.equals("join")) {
                   
                    this.hold = false;
                }
            }
            while (this.hold == true);
            printUsers();
            this.userName = receive.getString("0");
            server.addUserName(userName);
            System.out.println(this.userName+" joined in");
            messageBuffer = "New user connected: " + this.userName;
            server.broadcast(messageBuffer, this);
        }
        /**
         * Reads a Json message from the client.
         */
        JSONObject readMessage() {
            String result = "";
            byte[] temp=new byte[1024];
            try{
            System.out.println("reading");
            
            reader.read(temp);
             result = new String(temp, "ascii");
           
          
                
             System.out.println("temp: " + temp);
             System.out.println("result: "+result);
                
            } catch (IOException e) {
                System.out.println("Error in UserThread: " + e.getMessage());
                e.printStackTrace();
            }
            return JSON.parseObject(result);

        }
    }
    
}

