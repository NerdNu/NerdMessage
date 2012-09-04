package nu.nerd.nerdmessage.database;

import java.util.Collection;
import java.util.List;

import nu.nerd.nerdmessage.NerdMessage;

public class NerdMailDAO {
    
    private final  NerdMessage plugin;
    
    public NerdMailDAO(NerdMessage plugin) {
        this.plugin = plugin;
    }
    
    public List<NerdMail> getMailForUser(String user) {
        final List<NerdMail> queryResult = plugin.getDatabase().find(NerdMail.class).where().eq("to_user", user).orderBy().desc("sent").findList();
        return queryResult;
    }
    
    public void deleteMailForUser(String user) {
        plugin.getDatabase().beginTransaction();
        try {
            final List<NerdMail> mailForUser = getMailForUser(user);
            
            for (NerdMail nerdMail : mailForUser) {
                plugin.getDatabase().delete(nerdMail);
            }
            plugin.getDatabase().commitTransaction();
        } finally {
            plugin.getDatabase().endTransaction();
        }
    }
    
    public void storeMail(NerdMail message) {
        plugin.getDatabase().save(message);
    }
    
    public void updateMail(NerdMail message) {
        plugin.getDatabase().update(message);
    }
    
    public void deleteMail(Collection<NerdMail> messages) {
        plugin.getDatabase().delete(messages);
    }
}
