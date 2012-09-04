package nu.nerd.nerdmessage;

import static org.bukkit.ChatColor.stripColor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;

import javax.persistence.PersistenceException;

import nu.nerd.nerdmessage.database.BlockedUser;
import nu.nerd.nerdmessage.database.NerdMail;
import nu.nerd.nerdmessage.database.NerdMailManager;
import nu.nerd.nerdmessage.database.NerdMailSettings;
import nu.nerd.nerdmessage.database.NerdMailbox;
import nu.nerd.nerdmessage.executor.BroadcastExecutor;
import nu.nerd.nerdmessage.executor.MailCommandExecutor;
import nu.nerd.nerdmessage.executor.MessageCommandExecutor;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import com.google.common.base.Optional;
import com.google.common.collect.Maps;

public class NerdMessage extends JavaPlugin implements Listener {
  
    /**
     * ReplyToMap tracks username=>username mapping indicating who /reply should message
     */
    private final HashMap<String, String> replyToMap = Maps.newHashMap();
    private NerdMailManager manager;

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        manager = new NerdMailManager(this);
        initDb();
    	getCommand("msg").setExecutor(new MessageCommandExecutor(this));
    	getCommand("broadcast").setExecutor(BroadcastExecutor.broadcastExecutor(this));
    	getCommand("me").setExecutor(BroadcastExecutor.meExecutor(this));
    	getCommand("op-broadcast").setExecutor(BroadcastExecutor.modBroadcastExecutor(this));
    	getCommand("admin-broadcast").setExecutor(BroadcastExecutor.adminBroadcastExecutor(this));
    	getCommand("mail").setExecutor(new MailCommandExecutor(this, manager));
    }
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        Optional<NerdMailbox> mailbox = manager.getMailbox(player.getName());
        
        if (mailbox.isPresent()) {
            if (mailbox.get().hasNewMail()) {
                player.sendMessage(ChatColor.GREEN + "You have new mail! Use \"/mail inbox\" to view.");
            }
        } else {
            player.sendMessage("lolwut");
        }
        
    }
    
    @Override
    public void onDisable() {
        super.onDisable();
        replyToMap.clear();
    }
    
    public String getReplyTo(final String username) {
        return replyToMap.get(stripColor(username));
    }
    
    public void setReplyTo(final String username, String replyTo) {
        replyToMap.put(stripColor(username), stripColor(replyTo));
    }
    
    private void initDb() {
        //well this isn't at all dodgy
        try {
            getDatabase().find(NerdMail.class).findRowCount();
        } catch (PersistenceException e) {
            getLogger().log(Level.INFO, "First run, initializing database.");
            installDDL();
        }
        
    }
    
    @Override
    public ArrayList<Class<?>> getDatabaseClasses() {
        final ArrayList<Class<?>> clazzList = new ArrayList<Class<?>>();
        clazzList.add(NerdMail.class);
        clazzList.add(NerdMailSettings.class);
        clazzList.add(BlockedUser.class);
        return clazzList;
    }
}