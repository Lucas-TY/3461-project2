public class Group {
    private int id;
    private String name;
    private Set<String> userNames = new HashSet<>();
    private Set<User> users = new HashSet<>();
    private Map<Integer, Message> messages = new HashMap<>();
    public static int messageID = 0;
    public Group(int id,String name){
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
        Group.messageID++;
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
            if (id>Group.messageID){
                result=new Message("error", "invalid id(too large)");
            }
        
            return result;
    }
}
