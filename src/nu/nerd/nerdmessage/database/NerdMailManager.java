package nu.nerd.nerdmessage.database;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import nu.nerd.nerdmessage.NerdMessage;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.google.common.base.Optional;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.util.concurrent.UncheckedExecutionException;


/**
 * This class handles construction, fetching and manipulation of user mailboxes using various DAOs... 
 * So basically bloody everything.
 * 
 * NOTES: Public methods in this class take pains to ensure usernames are lowercased (probably be better if the DAOs did that), private methods generally assume 
 * names are already lowercased.
 * @author James Eastwood
 *
 */
public class NerdMailManager {

    /**
     * Number of mailboxes to keep in memory.
     */
    private int mailboxCacheSize;
    
    /**
     * Time in seconds the user need to wait before performing another action. (sending a message)
     */
    private final int cooldownPeriod;

    /**
     * DAO for retrieving, modifying and adding NerdMail objects for users. 
     */
    private final NerdMailDAO nerdMailDAO;
    /**
     * DAO for accessing per-user settings stored in the DB.
     */
    private NerdMailSettingsDAO nerdMailSettingsDAO;
    private final Cache<String, Optional<NerdMailbox>> mailboxCache;
    
    /**
     * Username=>LastAction map for throttling. This never gets cleaned out, but probably wont be a problem with regular restarts
     */
    private final Map<String, Date> lastActionMap;
    private final NerdMessage plugin;

    public NerdMailManager(NerdMessage plugin) {
        this.plugin = plugin;
        this.nerdMailDAO = new NerdMailDAO(plugin);
        this.nerdMailSettingsDAO = new NerdMailSettingsDAO(plugin);
        this.lastActionMap = Maps.newHashMap();
        this.mailboxCache = CacheBuilder.newBuilder().maximumSize(mailboxCacheSize).build(new CacheLoader<String, Optional<NerdMailbox>>() {
            @Override
            public Optional<NerdMailbox> load(String user) throws Exception {
                return Optional.fromNullable(fetchMailbox(user.toLowerCase()));
            }
        });
        this.cooldownPeriod = plugin.getConfig().getInt("cooldown");
        this.mailboxCacheSize = plugin.getConfig().getInt("mailboxcachesize");
    }

    /**
     * Delete a list of items for a user. NOTE: Does not validate that the username given for the
     * cache update is actually the owner of the deleted items.
     * @param toDelete items to delete from the db
     * @param user username for uptating the cache
     */
    public void deleteItems(final Set<NerdMail> toDelete, String user) {
        nerdMailDAO.deleteMail(toDelete);
        final Optional<NerdMailbox> mail = getMailbox(user.toLowerCase());
        if (mail.isPresent()) {
            mail.get().deleteMail(toDelete);
        }
    }

    /**
     * Delete ALL mail for a user.
     * @param user
     */
    public void deleteMailForUser(String user) {
        user = user.toLowerCase();
        nerdMailDAO.deleteMailForUser(user);
        mailboxCache.invalidate(user);
    }

    /**
     * Fetch mailbox for user from the cache, or construct a new one from the DB. 
     * 
     * NOTE: If the 'requireregistration' parameter is set, the user does not have a mailbox, and the user is not currently online,
     * this method will return Optional.absent().
     * @param user
     * @return Mailbox for user, or optional.absent() in case of an error.
     */
    public Optional<NerdMailbox> getMailbox(String user) {
        user = user.toLowerCase();

        if (plugin.getConfig().getBoolean("requireregistration") && !isUserRegistered(user)) {
            return Optional.absent();
        }

        try {
            return mailboxCache.get(user);
        } catch (ExecutionException e) {
            // Should never happen
            return Optional.absent();
        } catch (UncheckedExecutionException e) {
            // in case I really mucked up
            return Optional.absent();
        }
    }

    /**
     * Check if a user's inbox is full. {@link send} performs this check, however this method can be called 
     * first in order to provide better error messages.
     * @param user
     * @return Boolean indicating the inbox for the given user is full.
     */
    public boolean isInboxFull(String user) {
        user = user.toLowerCase();
        final Optional<NerdMailbox> mail = getMailbox(user);
        if (mail.isPresent()) {
            return mail.get().isFull();
        }
        return false;
    }

    public boolean isThrottled(CommandSender sender) {
        if (sender.hasPermission("nerdmessage.mail.bypassthrottle"))
            return false;
        final Date date = lastActionMap.get(sender.getName().toLowerCase());
        if (date != null) {
            final int secondsSinceLastAction = (int) ((new Date().getTime() - date.getTime()) / 1000);
            if (secondsSinceLastAction < cooldownPeriod) {
                return true;
            }
        }
        return false;
    }

    /**
     * This method establishes if the user has been seen by the mail system
     * before. If not, this method will attempt to register them.
     * 
     * @param user
     * @return Boolean indicating if the user is registered.
     */
    public boolean isUserRegistered(String user) {
        user = user.toLowerCase();
        final Optional<NerdMailSettings> settingsForUser = nerdMailSettingsDAO.getSettingsForUser(user);

        if (!settingsForUser.isPresent()) {
            return attemptToRegister(user);
        } else {
            return true;
        }
    }

    /**
     * Marks a list of items as read and updates the cache.
     * @param readMail
     * @param user
     */
    public void markItemsRead(List<NerdMail> readMail, String user) {
        for (NerdMail nerdMail : readMail) {
            nerdMail.setRead(true);
            nerdMailDAO.updateMail(nerdMail);
        }
        Optional<NerdMailbox> mailbox = getMailbox(user);
        if (mailbox.isPresent()) {
            mailbox.get().setSorted(false);
        }
    }

    /**
     * This method constructs a new NerdMail object, and persists it if the recipient does not have a full inbox and the sender
     * is not on the recipients block-list. 
     * @param message
     * @param recipientName
     * @param sender
     * @return Boolean indicating if the message was sent.
     */
    public boolean send(String message, String recipientName, CommandSender sender) {
        recipientName = recipientName.toLowerCase();
        // Enforce in app... because fuck JPA
        if (!isInboxFull(recipientName) || sender.hasPermission("nerdmessage.mail.ignorefull")) {
            final NerdMail nerdMail = new NerdMail();
            nerdMail.setTo_user(recipientName);
            nerdMail.setFrom_user(sender.getName().toLowerCase());
            nerdMail.setSent(new Date());
            nerdMail.setRead(false);
            nerdMail.setMessage(message);

            final Optional<NerdMailbox> mail = getMailbox(recipientName);
            if (mail.isPresent()) {
                if (!mail.get().isBlocked(sender.getName().toLowerCase()) || sender.hasPermission("nerdmessage.mail.ignoreblock")) {
                    nerdMailDAO.storeMail(nerdMail);
                    // update the cache by hand
                    mail.get().addMail(nerdMail);
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Set the last action time, the basis for throttling.
     * @param sender
     */
    public void setLastActionTime(CommandSender sender) {
        setLastActionTime(sender.getName().toLowerCase());
    }

    public boolean userHasNewMail(String user) {
        user = user.toLowerCase();
        final Optional<NerdMailbox> mail = getMailbox(user);
        if (mail.isPresent()) {
            return mail.get().hasNewMail();
        }
        return false;
    }

    /**
     * Create a new settings object for the user and save it. It is important
     * that this method is called only when the user is not already registered.
     * 
     * @param user
     * @return boolean indicating if the user was registered.
     */
    private boolean attemptToRegister(String user) {
        final Player player = plugin.getServer().getPlayer(user);

        if (player != null) {
            createNewSettings(player);
            return true;
        } else if (plugin.getConfig().getBoolean("requireregistration")) {
            return false;
        } else {
            createNewSettings(player);
            return true;
        }

    }

    private void createNewSettings(final Player player) {
        NerdMailSettings mailSettings = NerdMailSettings.constructDefault(plugin.getConfig());
        mailSettings.setUser_name(player.getName().toLowerCase());
        nerdMailSettingsDAO.storeSettings(mailSettings);
    }

    /**
     * Fetches mail and settings from the DB and constructs a mailbox for the
     * user. Returns null if the user has not been seen before.
     * 
     * @param user
     * @return mailbox for the user of null.
     */
    private NerdMailbox fetchMailbox(String user) {
        final Optional<NerdMailSettings> settingsForUser = nerdMailSettingsDAO.getSettingsForUser(user);
        if (settingsForUser.isPresent()) {
            return new NerdMailbox(nerdMailDAO.getMailForUser(user), settingsForUser.get(), getBlockedUsers(user));
        } else {
            return null;
        }
    }

    private Set<String> getBlockedUsers(String user) {
        final Set<BlockedUser> users = plugin.getDatabase().find(BlockedUser.class).where().eq("blockingUser", user).findSet();
        final Set<String> userNames = Sets.newHashSetWithExpectedSize(users.size());
        for (BlockedUser blockedUser: users) {
            userNames.add(blockedUser.getUsername());
        }
        return userNames;
    }
    
    private void setLastActionTime(String name) {
        lastActionMap.put(name, new Date());
    }

    public void blockOrUnblock(Set<String> usersToBlockOrUnblock, String user) {
        user = user.toLowerCase();
        
        final Set<BlockedUser> users = plugin.getDatabase().find(BlockedUser.class).where().eq("blockingUser", user).findSet();
        
        for (BlockedUser blockedUser : users) {
            String username = blockedUser.getUsername();
            if (usersToBlockOrUnblock.contains(username)) {
                plugin.getDatabase().delete(blockedUser);
                usersToBlockOrUnblock.remove(username);
            }
        }
        for (String toBlock : usersToBlockOrUnblock) {
            BlockedUser blockedUser = new BlockedUser();
            blockedUser.setBlockingUser(user);
            blockedUser.setUsername(toBlock);
            plugin.getDatabase().save(blockedUser);
        }
        mailboxCache.invalidate(user);
    }

    public void saveSettings(NerdMailSettings settingsForUser, String target) {
        nerdMailSettingsDAO.updateSettings(settingsForUser);
        mailboxCache.invalidate(target.toLowerCase());
    }
}
