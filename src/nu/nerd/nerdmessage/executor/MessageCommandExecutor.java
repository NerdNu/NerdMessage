package nu.nerd.nerdmessage.executor;

import java.util.Set;

import nu.nerd.nerdmessage.NerdMessage;

import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import com.google.common.collect.Sets;

public class MessageCommandExecutor implements CommandExecutor {

    private static final Set<String> REPLY_ALIAS = Sets.newHashSet("reply", "r");

    private final NerdMessage plugin;

    public MessageCommandExecutor(final NerdMessage messagePlugin) {
        this.plugin = messagePlugin;
    }

    public boolean onCommand(CommandSender sender, Command command, String invokedAlias, String[] args) {
        if (!validArgs(args, invokedAlias)) return false;


        // check if the command was invoked as /r or /reply
        String recipientName = null;
        String message = null;
        if (REPLY_ALIAS.contains(StringUtils.lowerCase(invokedAlias))) {
            message = StringUtils.join(args, " ", 0, args.length);
            recipientName = plugin.getReplyTo(sender.getName());
            if (recipientName == null) {
                sender.sendMessage(ChatColor.RED + "No user to reply to.");
                return true;
            }
        } else {
            recipientName = args[0];
            message = StringUtils.join(args, " ", 1, args.length);
        }

        CommandSender recipient = null;
        final ConsoleCommandSender console = plugin.getServer().getConsoleSender();
        if ("console".equalsIgnoreCase(recipientName)) {
            // Check perms? Is this actually useful?
            recipient = console;
        } else {
            recipient = plugin.getServer().getPlayer(recipientName);
        }

        if (recipient == null) {
            sender.sendMessage(ChatColor.RED + "User is not online.");
            plugin.setReplyTo(sender.getName(), null);
            return true;
        }

        plugin.setReplyTo(recipient.getName(), sender.getName());

        sender.sendMessage(formatForSender(message, recipient));
        recipient.sendMessage(formatForRecipient(message, sender));

        // no need to log if the console has already seen the message
        if (recipient != console && sender != console) {
            plugin.getLogger().info(formatMessage(message, sender.getName(), recipient.getName()));
        }
        return true;
    }

    private String formatForSender(String message, CommandSender recipient) {
        return formatMessage(message, "me", recipient.getName());
    }

    private String formatForRecipient(String message, CommandSender sender) {
        return formatMessage(message, sender.getName(), "me");
    }

    private String formatMessage(String message, String sender, String recipient) {
        final StringBuilder builder = new StringBuilder(32 + sender.length()+ recipient.length() + message.length());
        builder.append(ChatColor.WHITE).append("[").append(ChatColor.RED).append(sender).append(ChatColor.WHITE).append(" -> ");
        builder.append(ChatColor.GOLD).append(recipient).append(ChatColor.WHITE).append("] ");
        builder.append(message);
        return builder.toString();
    }

    private boolean validArgs(String[] args, String invokedAlias) {
        // in the case of reply, only a message is needed
        if ("r".equalsIgnoreCase(invokedAlias) || "reply".equalsIgnoreCase(invokedAlias)) {
            return args.length >= 1;
        }
        // otherwise need message AND username
        return args.length >= 2;
    }

}
