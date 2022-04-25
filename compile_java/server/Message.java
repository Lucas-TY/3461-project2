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
    public String getSubject(){
        return this.subject;
    }

    public String getContent(){
        return this.content;
    }

    public void setId(int id) {
        this.id = id;
    }
    public int getId() {
        return this.id;
    }
    public String getDate() {
        return this.date;
    }
    public String getSender() {
        return this.sender;
    }
}
