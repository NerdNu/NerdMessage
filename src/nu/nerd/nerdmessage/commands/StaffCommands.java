package nu.nerd.nerdmessage.commands;

import nu.nerd.nerdmessage.NerdMessage;
import nu.nerd.nerdmessage.StringUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class StaffCommands implements CommandExecutor {


    private NerdMessage plugin;
    private final String redisError = ChatColor.RED + "Error: could not deliver message, as the Redis server could not be reached.";


    public StaffCommands(NerdMessage plugin) {
        this.plugin = plugin;
        plugin.getCommand("mb").setExecutor(this);
        plugin.getCommand("mbs").setExecutor(this);
        plugin.getCommand("mbme").setExecutor(this);
        plugin.getCommand("ab").setExecutor(this);
        plugin.getCommand("abs").setExecutor(this);
        plugin.getCommand("abme").setExecutor(this);
        plugin.getCommand("broadcast").setExecutor(this);
        plugin.getCommand("o").setExecutor(this);
        plugin.getCommand("mbg").setExecutor(this);
        plugin.getCommand("abg").setExecutor(this);
        plugin.getCommand("global-broadcast").setExecutor(this);
    }


    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (args.length == 0) return false;
        switch (cmd.getName().toUpperCase(Locale.ROOT)) {
            case "MB":
                mb(sender, StringUtil.join(args), "NORMAL");
                return true;
            case "MBS":
                mb(sender, StringUtil.join(args), "SARCASTIC");
                return true;
            case "MBME":
                mb(sender, StringUtil.join(args), "ME");
                return true;
            case "AB":
                ab(sender, StringUtil.join(args), "NORMAL");
                return true;
            case "ABS":
                ab(sender, StringUtil.join(args), "SARCASTIC");
                return true;
            case "ABME":
                ab(sender, StringUtil.join(args), "ME");
                return true;
            case "BROADCAST":
                broadcast(sender, StringUtil.join(args));
                return true;
            case "O":
                o(sender, StringUtil.join(args));
                return true;
            case "MBG":
                mbg(sender, StringUtil.join(args));
                return true;
            case "ABG":
                abg(sender, StringUtil.join(args));
                return true;
            case "GLOBAL-BROADCAST":
                globalBroadcast(sender, StringUtil.join(args));
                return true;
            default:
                return false;
        }
    }


    public void mb(CommandSender sender, String message, String type) {
        switch (type) {
            case "NORMAL" -> message = tag("Mod - " + sender.getName()) + ChatColor.GREEN + message;
            case "SARCASTIC" -> message = tag("Mod - " + sender.getName()) + ChatColor.GREEN + ChatColor.ITALIC + message;
            case "ME" -> message = tag("Mod") + ChatColor.GREEN + "*" + sender.getName() + " " + ChatColor.GREEN + message;
        }
        for(Player player : plugin.getPlayersWithPerm("nerdmessage.mb")) {
            player.sendMessage(message);
        }
    }


    public void ab(CommandSender sender, String message, String type) {
        switch (type) {
            case "NORMAL" -> message = tag("Admin - " + sender.getName()) + ChatColor.GOLD + message;
            case "SARCASTIC" -> message = tag("Admin - " + sender.getName()) + ChatColor.GOLD + ChatColor.ITALIC + message;
            case "ME" -> message = tag("Admin") + ChatColor.GOLD + "*" + sender.getName() + " " + ChatColor.GOLD + message;
        }
        for(Player player : plugin.getPlayersWithPerm("nerdmessage.ab")) {
            player.sendMessage(message);
        }
    }

    public void broadcast(CommandSender sender, String message) {
        message = tag("Broadcast") + ChatColor.GREEN + message;
        for(Player player : Bukkit.getOnlinePlayers()) {
            System.out.println(player.getName());
            player.sendMessage(message);
        }
    }


    public void o(CommandSender sender, String message) {
        message = String.format("<%s%s%s>%s %s", ChatColor.RED, sender.getName(), ChatColor.WHITE, ChatColor.GREEN, message);
        for(Player player : Bukkit.getOnlinePlayers()) {
            player.sendMessage(message);
        }
    }


    public void mbg(CommandSender sender, String message) {
        message = globalTag("MBG", sender.getName()) + ChatColor.GREEN + message;
        if (plugin.crossServerEnabled()) {
            plugin.redisPublish(sender, "mbg", message);
        } else {
            sender.sendMessage(ChatColor.RED + "Cross-server messaging is not configured.");
        }
    }


    public void abg(CommandSender sender, String message) {
        message = globalTag("ABG", sender.getName()) + ChatColor.GOLD + message;
        if (plugin.crossServerEnabled()) {
            plugin.redisPublish(sender, "abg", message);
        } else {
            sender.sendMessage(ChatColor.RED + "Cross-server messaging is not configured.");
        }
    }


    public void globalBroadcast(CommandSender sender, String message) {
        String tag = String.format("[%sGlobal %sBroadcast%s] ", ChatColor.DARK_PURPLE, ChatColor.RED, ChatColor.WHITE);
        message = tag + ChatColor.GREEN + message;
        if (plugin.crossServerEnabled()) {
            plugin.redisPublish(sender, "globalbroadcast", message);
        } else {
            sender.sendMessage(ChatColor.RED + "Cross-server messaging is not configured.");
        }
    }


    private String tag(String str) {
        return String.format("[%s%s%s] ", ChatColor.RED, str, ChatColor.WHITE);
    }


    private String globalTag(String prefix, String name) {
        if (plugin.getServerName() != null) {
            return String.format("[%s%s(%s)%s - %s%s] ", ChatColor.DARK_PURPLE, prefix, plugin.getServerName(), ChatColor.RED, name, ChatColor.WHITE);
        } else {
            return String.format("[%s%s%s - %s%s] ", ChatColor.DARK_PURPLE, prefix, ChatColor.RED, name, ChatColor.WHITE);
        }
    }


}
