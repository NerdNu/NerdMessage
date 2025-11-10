package nu.nerd.nerdmessage.commands;

import java.util.*;
import java.util.stream.Collectors;
import nu.nerd.nerdmessage.NMUser;
import nu.nerd.nerdmessage.NerdMessage;
import nu.nerd.nerdmessage.StringUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


public class ChatCommands implements CommandExecutor, TabCompleter {


    private NerdMessage plugin;


    public ChatCommands(NerdMessage plugin) {
        this.plugin = plugin;
        plugin.getCommand("msg").setExecutor(this);
        plugin.getCommand("cmsg").setExecutor(this);
        plugin.getCommand("me").setExecutor(this);
        plugin.getCommand("s").setExecutor(this);
    }


    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 0) return false;
        if (cmd.getName().equalsIgnoreCase("msg")) {
            if (label.equalsIgnoreCase("r") || label.equalsIgnoreCase("reply")) {
                reply(sender, StringUtil.join(args), false, false, false);
            } else if (label.equalsIgnoreCase("m") || label.equalsIgnoreCase("msg") || label.equalsIgnoreCase("t") || label.equalsIgnoreCase("tell")) {
                msg(sender, args[0], StringUtil.join(args, 1), false, false, false);
            } else if (label.equalsIgnoreCase("ms") || label.equalsIgnoreCase("msgs") || label.equalsIgnoreCase("ts") || label.equalsIgnoreCase("tells")) {
                msg(sender, args[0], StringUtil.join(args, 1), false, true, false);
            } else if (label.equalsIgnoreCase("mme") || label.equalsIgnoreCase("msgme") || label.equalsIgnoreCase("tme") || label.equalsIgnoreCase("tellme")) {
                msg(sender, args[0], StringUtil.join(args, 1), false, false, true);
            } else if (label.equalsIgnoreCase("msme") || label.equalsIgnoreCase("msgsme") || label.equalsIgnoreCase("tsme") || label.equalsIgnoreCase("tellsme")) {
                msg(sender, args[0], StringUtil.join(args, 1), false, true, true);
            } else if (label.equalsIgnoreCase("rs") || label.equalsIgnoreCase("replys")) {
                reply(sender, StringUtil.join(args), false, true, false);
            } else if (label.equalsIgnoreCase("rme") || label.equalsIgnoreCase("replyme")) {
                reply(sender, StringUtil.join(args), false, false, true);
            } else if (label.equalsIgnoreCase("rsme") || label.equalsIgnoreCase("replysme")) {
                reply(sender, StringUtil.join(args), false, true, true);
            } else {
                return false;
            }
            return true;
        }
        else if (cmd.getName().equalsIgnoreCase("cmsg")) {
            msg(sender, args[0], StringUtil.join(args, 1), true, false, false);
            return true;
        }
        else if (cmd.getName().equalsIgnoreCase("me")) {
                me(sender, StringUtil.join(args));
            return true;
        }
        else if (cmd.getName().equalsIgnoreCase("s")) {
                sarcasm(sender, StringUtil.join(args));
            return true;
        }
        else {
            return false;
        }
    }


    /**
     * Send a private message to a player
     * @param sender The CommandSender object from onCommand()
     * @param recipientNames The username strings of the recipients
     * @param message The message to send
     * @param green Whether this is a staff green message from /cmsg
     * @param sarcastic Whether this message will be italic
     * @param action Whether this message will be an action
     */
    public void msg(CommandSender sender, String recipientNames, String message, boolean green, boolean sarcastic, boolean action) {

        NMUser user = plugin.getOrCreateUser(sender.getName());
        CommandSender recipient = null;

        // Find recipients from recipientNames
        Set<String> recipients = new HashSet<>();
        for (String recipientName : recipientNames.split(",")) {
            if (recipientName.equalsIgnoreCase("console")) {
                recipients.add("CONSOLE");
            } else {
                recipient = plugin.getPlayer(recipientName);
                if (recipient != null) {
                    recipients.add(recipient.getName());
                } else {
                    // If a recipient couldn't be found, notify the sender
                    sender.sendMessage(ChatColor.RED + recipientName +" is not online.");
                }
            }
        }

        // Add the sender to the recipients to simplify later logic
        recipients.add(sender.getName());

        // Limit to 6 recipients (7 because sender is one of them)
        if (recipients.size() > 6) {
            sender.sendMessage(ChatColor.RED + "Sorry, you can send to a maximum of 6 recipients. Try a clanchat.");
            return;
        }

        // Set reply-to for involved players
        for (String recipientName : recipients) {
            plugin.getOrCreateUser(recipientName).setReplyTo(recipients);
        }

        // Send the messages
        // It is possible for the player to have no other valid recipients.
        // The message is sent anyway to emulate past behavior of msg when you messaged yourself.
        for (String recipientName : recipients) {
            // Whether to actually send the message to the player or not (for /ignore).
            if (!sender.hasPermission("nerdmessage.ignore.bypass-msg") && (plugin.getOrCreateUser(recipientName).isIgnoringPlayer(sender.getName()))) {
                System.out.println("Message blocked by /ignore from:" + sender.getName() + " to:" + recipientName);
                continue;
            }

            // Set the appropriate tag for the message
            String tag = tag(
                recipientName.equals(sender.getName()) ? "Me" : sender.getName(), // left side
                recipients.stream()
                    .map(name -> {
                        // recipient sees their name as "Me"
                        if (name.equals(recipientName)) return "Me";
                        // sender name in the list of recipients is always "Me". If there is a duplicate "Me" we should only get one because recipients is a set
                        if (name.equals(sender.getName())) return "Me";
                        // all other names are unchanged
                        return name;
                    })
                    // If there is more than 1 recipient (aka non-self message), don't include "Me" in recipients list for the sender
                    .filter(name -> !(name.equals("Me") && recipientName.equals(sender.getName()) && recipients.size() > 1))
                    .distinct()
                    .collect(Collectors.joining(", ")),
                action
            );

            // Find the recipient of this message
            if (recipientName.equalsIgnoreCase("console")) {
                recipient = plugin.getServer().getConsoleSender();
            }
            else {
                recipient = plugin.getPlayer(recipientName);
            }

            // And now we actually send the message...
            if (sarcastic) {
                recipient.sendMessage(tag + ChatColor.ITALIC + message);
            } else if (green) {
                recipient.sendMessage(tag + ChatColor.GREEN + message);
            } else {
                recipient.sendMessage( tag + message);
            }
        }

        // Logs
        System.out.println(user.getName() + ":/msg " + String.join(",", recipients) + " " + message);
        System.out.println("[" + sender.getName() + " -> " + String.join(",", recipients) + "] " + message);

    }


    /**
     * Reply to the user the sender is currently conversing with, from the /r command.
     * The "reply-to" recipient is either the last person you messaged or the last person to message you,
     * depending on which is the newest event.
     */
    public void reply(CommandSender sender, String message, boolean green, boolean sarcastic, boolean action) {
        NMUser user = plugin.getUser(sender.getName());
        if (user == null || user.getReplyTo() == null) {
            sender.sendMessage(ChatColor.RED + "No user to reply to.");
            return;
        }
        msg(sender, String.join(",", user.getReplyTo()), message, green, sarcastic, action);
    }


    /**
     * Handle IRC-style /me messages
     */
    public void me(CommandSender sender, String message) {
        if (StringUtil.isAllCaps(message)) {
            sender.sendMessage(ChatColor.RED + "Please don't chat in all caps.");
            return;
        }
        message = "* " + ChatColor.stripColor(sender.getName()) + " " + message;
        sendPublicMessage(sender, message);
        plugin.getServer().getLogger().info(message);
    }


    /**
     * Handle italicized messages from the /s command
     */
    public void sarcasm(CommandSender sender, String message) {
        if (StringUtil.isAllCaps(message)) {
            sender.sendMessage(ChatColor.RED + "All-caps? How original...");
            return;
        }
        message = "<" + sender.getName() + "> " + ChatColor.ITALIC + message;
        sendPublicMessage(sender, message);
        plugin.getServer().getLogger().info(message);
    }


    /**
     * Format a [sender -> recipient] tag for a private message
     * @param leftUser The username on the left of the arrow (e.g. "Me" or "redwall_hp")
     * @param rightUser The username on the right of the arrow
     * @param action Wheter the message should be formated as an action
     */
    private String tag(String leftUser, String rightUser, boolean action) {
        if (action) {
            return String.format("[*%s%s%s -> %s%s%s] ", ChatColor.RED, leftUser, ChatColor.WHITE, ChatColor.GOLD, rightUser, ChatColor.WHITE);

        } else {
            return String.format("[%s%s%s -> %s%s%s] ", ChatColor.RED, leftUser, ChatColor.WHITE, ChatColor.GOLD, rightUser, ChatColor.WHITE);
        }
    }


    /**
     * Sends an arbitrary message string to every player online
     */
    private void sendPublicMessage(CommandSender sender, String message) {
        for (Player p : plugin.getServer().getOnlinePlayers()) {
            NMUser recipient = plugin.getUser(ChatColor.stripColor(p.getName()));
            if (recipient == null || !recipient.isIgnoringPlayer(sender.getName())) {
                p.sendMessage(message);
            }
        }
    }


    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if(strings.length == 1) {
            String input = strings[0];

            String[] parts = input.split(",", -1);
            String lastPart = parts[parts.length - 1].trim();

            List<String> suggestions = Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(lastPart.toLowerCase()))
                    .toList();

            String prefix = input.contains(",") ? input.substring(0, input.lastIndexOf(',') + 1) : "";
            return suggestions.stream().map(string -> prefix + string).toList();
        }
        return List.of();
    }
}
