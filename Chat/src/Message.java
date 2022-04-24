public class Message {
    private String subject;
    private String content;
    private int id;
    
    public  Message(String subject,String content){
        this.subject=subject;
        this.content=content;

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
}
