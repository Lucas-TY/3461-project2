package server;
import java.io.*;
import java.net.*;
import java.nio.charset.Charset;
import java.util.*;
import java.util.stream.Stream;
import java.time.LocalDate;
import com.alibaba.fastjson.*;
import com.alibaba.fastjson.JSONObject;

import server.Server.User;
public class Group {
    public String id;
    public String name;
    private Set<String> userNames = new HashSet<>();
    private Set<User> users = new HashSet<>();
    private Map<Integer, Message> messages = new HashMap<>();
    public int messageID = 0;
    public Group(String id,String name){
        this.id=id;
        this.name=name;
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
     * Add a new user ingroup
     */
    void addUser(String userName,User newUser) {
        userNames.add(userName);
        users.add(newUser);
    }
    /**
     * When a client is disconneted, removes the associated username and UserThread
     */
    void removeUser(String userName, User aUser) {
        boolean removed = userNames.remove(userName);
        if (removed) {
            users.remove(aUser);
            System.out.println("The user " + userName + " quitted");
            broadcast(userName + " has quitted Group:"+this.name, aUser);
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
     * Returns true if user is in this group
     */
    boolean hasUser(String userNames) {
        return this.userNames.contains(userNames);
    }
    /**
    * Add a new message to the server record
    */
    void addMessage(Message mes) {
        this.messageID++;
        Integer id = Integer.valueOf(this.messageID);
        mes.setId(id);
        this.messages.put(id, mes);

    }

    /**
    * Tostring for groups command
    */
    @Override
    public String toString() {
        return "\n\t(ID: "+ this.id+", Name: "+this.name+")";
    }
    /**
    * Send history mssage to user
    */
    void printHistory(User aUser){
        String messageBuffer;
        Message mes;
        Integer currentId = this.messageID;
        Integer lastId = this.messageID - 1;
        if (this.messageID >= 2) {
            mes=getMessage(lastId.toString());
            messageBuffer = "[Group:"+this.id+"("+this.name+")] Message ID: " + mes.getId() + ", Sender: [" + mes.getSender()  + "], Subject: "+mes.getSubject();
            aUser.sendMessage(messageBuffer);
            mes=getMessage(currentId.toString());
            messageBuffer = "[Group:"+this.id+"("+this.name+")] Message ID: " + mes.getId() + ", Sender: [" + mes.getSender()  + "], Subject: "+mes.getSubject();
            aUser.sendMessage(messageBuffer);
            
        }else if(messageID==1){
            mes=getMessage(currentId.toString());
            messageBuffer = "[Group:"+this.id+"("+this.name+")] Message ID: " + mes.getId() + ", Sender: [" + mes.getSender()  + "], Subject: "+mes.getSubject();
            aUser.sendMessage(messageBuffer);
        }
    }
    /**
    * Return the message that has that id
    */
    Message getMessage(String id) {

        Integer num = Integer.parseInt(id);
        Message result = this.messages.get(num);
        if (num > this.messageID) {
            result=new Message("System",LocalDate.now().toString(),"invalid id(too large)", "invalid id(too large)");
        }

        return result;
    }
    
}
