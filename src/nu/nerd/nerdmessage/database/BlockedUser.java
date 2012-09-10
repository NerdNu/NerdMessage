package nu.nerd.nerdmessage.database;

import javax.persistence.Entity;
import javax.persistence.Id;

import com.avaje.ebean.validation.NotNull;

/**
 * This was originally going to have a manyToOne relationship with nerdmail settings,
 * however, the persistence library bukkit is using insisted on adding FKs with 
 * ALTER TABLE statements, which sqlite doesn't support. :/
 * 
 */
@Entity
public class BlockedUser {
    
    @Id
    private int id;
    
    @NotNull
    private String blockingUser;
    
    @NotNull
    private String username;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getBlockingUser() {
        return blockingUser;
    }

    public void setBlockingUser(String blockingUser) {
        this.blockingUser = blockingUser;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
