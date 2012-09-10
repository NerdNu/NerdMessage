package nu.nerd.nerdmessage.database;

import java.util.Date;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import com.avaje.ebean.validation.NotNull;

@Entity
@Table(name = "nerdmail")
public class NerdMail {
    
    public static final int MAX_USERNAME_LENGTH = 16;
    
    @Id
    private int mailId;
    //@Index JPA doesn't support indexes? wtf?
    @Column(length=MAX_USERNAME_LENGTH, nullable=false)
    private String to_user;
    @Column(length=MAX_USERNAME_LENGTH, nullable=false)
    private String from_user;
    @Column(columnDefinition="TEXT", nullable=false)
    private String message;
    @NotNull
    @Basic
    private boolean read;
    @NotNull
    @Temporal(TemporalType.DATE)
    private Date sent;
    
    public int getMailId() {
        return mailId;
    }
    public void setMailId(int mailId) {
        this.mailId = mailId;
    }
    public String getTo_user() {
        return to_user;
    }
    public void setTo_user(String to_user) {
        this.to_user = to_user;
    }
    public String getFrom_user() {
        return from_user;
    }
    public void setFrom_user(String from_user) {
        this.from_user = from_user;
    }
    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }
    public boolean isRead() {
        return read;
    }
    public void setRead(boolean read) {
        this.read = read;
    }
    public Date getSent() {
        return sent;
    }
    public void setSent(Date sent) {
        this.sent = sent;
    }

   
}
