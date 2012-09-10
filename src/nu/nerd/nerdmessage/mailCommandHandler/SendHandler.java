package nu.nerd.nerdmessage.mailCommandHandler;

import java.util.regex.Pattern;

import nu.nerd.nerdmessage.NerdMessage;
import nu.nerd.nerdmessage.database.NerdMailManager;

import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

//TODO: Lots of logic in here is duplicated in composeHandler, should be combined.

public class SendHandler implements MailCommandHandler {

    private static final Pattern USERNAME_PATTERN  = Pattern.compile("[a-zA-Z0-9_]{0,16}?");
    
    private final NerdMessage plugin;
    private final NerdMailManager manager;

    public SendHandler(NerdMessage plugin, NerdMailManager manager) {
        this.plugin = plugin;
        this.manager = manager;
    }

    public boolean handle(CommandSender sender, String[] args) {
        
        if (!sender.hasPermission("nerdmessage.mail.send")) {
            sender.sendMessage(ChatColor.RED + "You do not have the permissions required to send mail.");
            return true;
        }
        
        //Args must be: { "send", <username>, <message>...}
        if (args.length < 3) {
            return false;
        }
        final String recipientName = args[1];
        if (!isValidName(recipientName)) {
            sender.sendMessage(ChatColor.RED + "Invalid recipient username!");
            return true;
        }
        final String message = StringUtils.join(args, " ", 2, args.length);
        if (manager.isThrottled(sender)) {
            sender.sendMessage(ChatColor.RED + "Throttled! Wait ten seconds and try again.");
            manager.setLastActionTime(sender);
            return true;
        }
        
        //Last action time for throttling
        manager.setLastActionTime(sender);
        
        if (!manager.isUserRegistered(recipientName)) {
            sender.sendMessage(ChatColor.RED + "Could not find user: " + recipientName);
            return true;
        }
        
        if (manager.isInboxFull(recipientName)) {
            sender.sendMessage(ChatColor.RED + "Recipients inbox is full!");
            //TODO: Blocked users shouldn't be able to trigger this message :/
            notifyFullInbox(recipientName);
            
            if (!sender.hasPermission("nerdmessage.mail.ignorefull")) {
                return true;
            }
        }
        
        //TODO: Might be nice if this method came back with a returncode and message, rather than having this class do all the checks it does above.
        boolean sent = manager.send(message, recipientName, sender);
        
        //Failure to send at this point probably means the sender is blocked, dont give any hints about this.
        sender.sendMessage(ChatColor.GREEN + "Mail successfully sent to " + recipientName);
        if (sent) {
            notifyRecipient(recipientName, sender);
        }
        
        return true;
    }

    private void notifyFullInbox(String recipientName) {
        final Player recipient = plugin.getServer().getPlayer(recipientName);
        if (recipient != null) {
            recipient.sendMessage(ChatColor.YELLOW + "Someone is trying to send you mail, but your inbox is full! Use \"/mail inbox\" to view and \"/mail delete\" to clear some space.");
        }
    }

    private void notifyRecipient(String recipientName, CommandSender sender) {
        final Player recipient = plugin.getServer().getPlayer(recipientName);
        
        if (recipient != null) {
            recipient.sendMessage(ChatColor.GREEN + "New mail from " + sender.getName() + "! Use \"/mail inbox\" to view.");
        }
    }

    private boolean isValidName(String recipientName) {
        return USERNAME_PATTERN.matcher(recipientName).matches();
    }

}
