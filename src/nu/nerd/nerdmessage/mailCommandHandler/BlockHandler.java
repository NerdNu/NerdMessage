package nu.nerd.nerdmessage.mailCommandHandler;

import java.util.Set;
import java.util.regex.Pattern;

import nu.nerd.nerdmessage.NerdMessage;
import nu.nerd.nerdmessage.database.NerdMailManager;
import nu.nerd.nerdmessage.database.NerdMailbox;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import com.google.common.base.Optional;
import com.google.common.collect.Sets;

public class BlockHandler implements MailCommandHandler {

    private final NerdMailManager manager;
    
    private static final Pattern USERNAME_PATTERN  = Pattern.compile("[a-zA-Z0-9_]{0,16}?");

    public BlockHandler(NerdMessage plugin, NerdMailManager manager) {
        this.manager = manager;
    }

    public boolean handle(CommandSender sender, String[] args) {
        final Optional<NerdMailbox> fromCache = manager.getMailbox(sender.getName());
        if (!fromCache.isPresent()) {
            return true;
        }
        final NerdMailbox mailbox = fromCache.get();
        
        if (args.length == 1) {
            sender.sendMessage(ChatColor.GREEN + "You have blocked:");
            for (String user : mailbox.getBlockedUsers()) {
                sender.sendMessage(ChatColor.RED + user);
            }
            return true;
        }
        
        final Set<String> usersToBlockOrUnblock = Sets.newHashSetWithExpectedSize(args.length);
        for (int i = 1; i < args.length; i++) {
            if (isValidName(args[i])) {
                usersToBlockOrUnblock.add(args[i].toLowerCase());
            }
        }
        
        manager.blockOrUnblock(usersToBlockOrUnblock, sender.getName());
        
        return true;
    }
    
    //Really need to stop duplicating this all over the place.
    private boolean isValidName(String name) {
        return USERNAME_PATTERN.matcher(name).matches();
    }

}
