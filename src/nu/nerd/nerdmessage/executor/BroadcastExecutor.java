package nu.nerd.nerdmessage.executor;

import static org.bukkit.ChatColor.stripColor;
import nu.nerd.nerdmessage.NerdMessage;

import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class BroadcastExecutor implements CommandExecutor {

    private final NerdMessage plugin;
    private final Formatter formatter;

    public interface Formatter {
        String formatMessage(CommandSender sender, String[] args);
    }

    public BroadcastExecutor(NerdMessage plugin, Formatter formatter) {
        this.plugin = plugin;
        this.formatter = formatter;
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        final String message = formatter.formatMessage(sender, args);
        if (message != null) {
            plugin.getServer().broadcast(message, command.getPermission());
        }
        return true;
    }

    // lots of factory methods follow

    public static BroadcastExecutor meExecutor(NerdMessage plugin) {
        return new BroadcastExecutor(plugin, new Formatter() {
            public String formatMessage(CommandSender sender, String[] args) {
                final String message = StringUtils.join(args, " ");
                if (message == null)
                    return null;
                final StringBuilder builder = new StringBuilder(24 + message.length());
                builder.append("* ").append(ChatColor.ITALIC).append(ChatColor.stripColor(sender.getName())).append(" ").append(message);
                return builder.toString();
            }
        });
    }

    public static BroadcastExecutor broadcastExecutor(NerdMessage plugin) {
        return new BroadcastExecutor(plugin, new Formatter() {
            public String formatMessage(CommandSender sender, String[] args) {
                final String message = StringUtils.join(args, " ");
                if (message == null)
                    return null;
                final StringBuilder builder = new StringBuilder(24 + message.length());
                builder.append(ChatColor.WHITE).append("[").append(ChatColor.RED).append("Broadcast").append(ChatColor.WHITE).append("] ").append(ChatColor.GREEN).append(message);
                return builder.toString();
            }
        });
    }

    public static BroadcastExecutor adminBroadcastExecutor(NerdMessage plugin) {
        return new BroadcastExecutor(plugin, new Formatter() {
            public String formatMessage(CommandSender sender, String[] args) {
                final String message = StringUtils.join(args, " ");
                if (message == null)
                    return null;
                final StringBuilder builder = new StringBuilder(24 + message.length());
                builder.append(ChatColor.WHITE).append("[").append(ChatColor.RED).append("Admin-").append(stripColor(sender.getName())).append(ChatColor.WHITE).append("] ").append(ChatColor.GREEN).append(message);
                return builder.toString();
            }
        });
    }

    public static BroadcastExecutor modBroadcastExecutor(NerdMessage plugin) {
        return new BroadcastExecutor(plugin, new Formatter() {
            public String formatMessage(CommandSender sender, String[] args) {
                final String message = StringUtils.join(args, " ");
                if (message == null)
                    return null;
                final StringBuilder builder = new StringBuilder(24 + message.length());
                builder.append(ChatColor.WHITE).append("<").append(ChatColor.RED).append(stripColor(sender.getName())).append(ChatColor.WHITE).append("> ").append(ChatColor.GREEN).append(message);
                return builder.toString();
            }
        });
    }
}
