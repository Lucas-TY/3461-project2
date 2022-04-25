package server;
public class Message {
    private String subject;
    private String content;
    private int id;
    private String date;
    private String sender;
    
    public  Message(String name,String date,String subject,String content){
        this.subject=subject;
        this.content=content;
        this.date=date;
        this.sender=name;

    }
    /**
     * Return subject string
    */
    public String getSubject(){
        return this.subject;
    }
    /**
     * Return content string
    */
    public String getContent(){
        return this.content;
    }
    /**
     * Set message ID
    */
    public void setId(int id) {
        this.id = id;
    }
    /**
     * Return ID
    */
    public int getId() {
        return this.id;
    }
    /**
     * Return date string
    */
    public String getDate() {
        return this.date;
    }
    /**
     * Return senderName string
    */
    public String getSender() {
        return this.sender;
    }
}
