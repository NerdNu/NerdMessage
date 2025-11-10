package nu.nerd.nerdmessage.commands;

import java.util.*;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import nu.nerd.nerdmessage.NerdMessage;
import nu.nerd.nerdmessage.StringUtil;
import nu.nerd.nerdmessage.alerts.AlertMessage;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static nu.nerd.nerdmessage.ColourUtils.color;
import static nu.nerd.nerdmessage.ColourUtils.colorList;


public class AlertCommands implements CommandExecutor, TabCompleter {


    private final NerdMessage plugin;


    public AlertCommands(NerdMessage plugin) {
        this.plugin = plugin;
        plugin.getCommand("alert").setExecutor(this);
    }


    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!cmd.getName().equalsIgnoreCase("alert")) return false;
        // Help text
        if (args.length == 0) {
            printHelp(sender);
            return true;
        }
        // List command
        if (args[0].equalsIgnoreCase("list")) {
            listCommand(sender);
            return true;
        }
        // Administrative commands
        if (sender.hasPermission("nerdmessage.alert.admin")) {
            if (args[0].equalsIgnoreCase("add")) {
                addCommand(sender, args);
                return true;
            }
            else if (args[0].equalsIgnoreCase("insert")) {
                insertCommand(sender, args);
                return true;
            }
            else if(args[0].equalsIgnoreCase("edit")) {
                editCommand(sender, args);
                return true;
            }
            else if (args[0].equalsIgnoreCase("remove")) {
                removeCommand(sender, args);
                return true;
            }
            else if (args[0].equalsIgnoreCase("interval")) {
                intervalCommand(sender, args);
                return true;
            }
            else if (args[0].equalsIgnoreCase("reload")) {
                reloadCommand(sender);
                return true;
            }
        }
        return false;
    }


    private void printHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.LIGHT_PURPLE + "Usage:");
        sender.sendMessage(ChatColor.LIGHT_PURPLE + "    /alert list");
        sender.sendMessage("        List all broadcast messages.");
        sender.sendMessage(ChatColor.LIGHT_PURPLE + "    /alert add [color] <message>");
        sender.sendMessage("        Add the message to the broadcast rotation.");
        sender.sendMessage(ChatColor.LIGHT_PURPLE + "    /alert insert <index> [color] <message>");
        sender.sendMessage("        Insert the message into the broadcast rotation.");
        sender.sendMessage(ChatColor.LIGHT_PURPLE + "    /alert edit <index> [color] <message>");
        sender.sendMessage("        Edit the message at the specified index.");
        sender.sendMessage(ChatColor.LIGHT_PURPLE + "    /alert remove <number>");
        sender.sendMessage("        Remove a message by number.");
        sender.sendMessage(ChatColor.LIGHT_PURPLE + "    /alert interval [seconds]");
        sender.sendMessage("        Get or set the interval between broadcasts in seconds.");
        sender.sendMessage(ChatColor.LIGHT_PURPLE + "    /alert reload");
        sender.sendMessage("        Reload the alerts from the YAML file.");
    }


    private void listCommand(CommandSender sender) {
        List<AlertMessage> alerts = plugin.getAlertHandler().getAlerts();
        sender.sendMessage(String.format("%sThere are %d alerts.", ChatColor.LIGHT_PURPLE, alerts.size()));
        for (AlertMessage alert : alerts) {
            sender.sendMessage(Component.text(alert.getColor().toString(), alert.getColor())
                    .append(Component.text(" (" + (alerts.indexOf(alert) + 1) + ") "))
                    .append(alert.getText()));
        }
    }


    /**
     * Add an alert
     */
    private void addCommand(CommandSender sender, String[] args) {

        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /alert add [color] <message>");
            return;
        }

        String possibleColor = args[1].toUpperCase();
        AlertMessage alert;
        if (colorList().contains(possibleColor)) {
            alert = new AlertMessage(StringUtil.join(args, 2), color(possibleColor));
        } else {
            alert = new AlertMessage(StringUtil.join(args, 1));
        }

        plugin.getAlertHandler().addAlert(alert, plugin.getAlertHandler().getAlerts().size());
        sender.sendMessage(String.format("%sAlert #%d added.", ChatColor.LIGHT_PURPLE, plugin.getAlertHandler().getAlerts().size()));

    }


    /**
     * Insert an alert at a given index
     */
    private void insertCommand(CommandSender sender, String[] args) {

        if (args.length < 3) {
            sender.sendMessage(ChatColor.RED + "Usage: /alert insert <index> [color] <message>");
            return;
        }

        int max = plugin.getAlertHandler().getAlerts().size() + 1;
        Integer index;
        try {
            index = Integer.parseInt(args[1]);
        } catch (NumberFormatException ex) {
            sender.sendMessage(ChatColor.RED + "A numerical index must be specified.");
            return;
        }
        if (index < 1 || index > max) {
            sender.sendMessage(String.format("%sThe index must be a number between 1 and %d inclusive.", ChatColor.RED, max));
            return;
        }

        String possibleColor = args[2].toUpperCase();
        AlertMessage alert;
        if (colorList().contains(possibleColor)) {
            alert = new AlertMessage(StringUtil.join(args, 3), color(possibleColor));
        } else {
            alert = new AlertMessage(StringUtil.join(args, 2));
        }

        plugin.getAlertHandler().addAlert(alert, index - 1);
        sender.sendMessage(String.format("%sAlert #%d added.", ChatColor.LIGHT_PURPLE, index));

    }

    /**
     * Edit an existing alert.
     */
    private void editCommand(CommandSender sender, String[] args) {

        if(args.length < 3) {
            sender.sendMessage(Component.text("Usage: /alert insert <index> [color] <message>", NamedTextColor.RED));
        }

        int max = plugin.getAlertHandler().getAlerts().size() - 1;
        int index;
        try {
            index = Integer.parseInt(args[1]);
        } catch (NumberFormatException ex) {
            sender.sendMessage(Component.text("A numerical index must be specified.", NamedTextColor.RED));
            return;
        }

        index--;

        if (index < 1 || index > max) {
            sender.sendMessage(Component.text("The index must be a number between 1 and " +
                    max + " inclusive.", NamedTextColor.RED));
            return;
        }

        plugin.getAlertHandler().editAlert(StringUtil.join(args, 2), index);

        sender.sendMessage(Component.text("Alert #" + index + " added.", NamedTextColor.LIGHT_PURPLE));

    }


    /**
     * Remove alert at index
     * @param sender
     */
    private void removeCommand(CommandSender sender, String[] args) {

        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /alert remove <number>");
            return;
        }

        Integer index;
        int numAlerts = plugin.getAlertHandler().getAlerts().size();
        try {
            index = Integer.parseInt(args[1]);
        } catch (NumberFormatException ex) {
            sender.sendMessage(ChatColor.RED + "A numerical index must be specified.");
            return;
        }
        if (numAlerts < 1) {
            sender.sendMessage(ChatColor.RED + "There are no alerts to remove.");
            return;
        }
        if (index < 1 || index > numAlerts + 1) {
            sender.sendMessage(String.format("%sThe index must be a number between 1 and %d inclusive.", ChatColor.RED, numAlerts + 1));
            return;
        }

        AlertMessage alert = plugin.getAlertHandler().removeAlert(index - 1);
        if (alert != null) {
            sender.sendMessage(Component.text("Removed alert: ", NamedTextColor.LIGHT_PURPLE).append(alert.getText()));
        }

    }

    /**
     * Admin command to get or set the interval between broadcast messages, in seconds.
     */
    private void intervalCommand(CommandSender sender, String[] args) {

        if (args.length < 2) {
            int seconds = plugin.getAlertHandler().getInterval();
            sender.sendMessage(String.format("%sAlerts are broadcast every %d seconds.", ChatColor.LIGHT_PURPLE, seconds));
            return;
        }

        int minInterval = 30;
        Integer seconds;
        try {
            seconds = Integer.parseInt(args[1]);
        } catch (NumberFormatException ex) {
            sender.sendMessage(ChatColor.RED + "You must specify the interval in seconds as a number.");
            return;
        }
        if (seconds < minInterval) {
            sender.sendMessage(String.format("%sThe interval must be at least %d seconds.", ChatColor.RED, minInterval));
            return;
        }

        plugin.getAlertHandler().setInterval(seconds);
        sender.sendMessage(String.format("%sThe alert broadcast interval was set to %d seconds.", ChatColor.LIGHT_PURPLE, seconds));

    }


    /**
     * Reload alerts.yml
     */
    private void reloadCommand(CommandSender sender) {
        plugin.getAlertHandler().stop();
        plugin.getAlertHandler().start();
        sender.sendMessage(ChatColor.LIGHT_PURPLE + "Alerts reloaded.");
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if(strings.length < 3) return List.of();
        String subCommand = strings[0];
        if(subCommand.equalsIgnoreCase("edit") && strings.length == 3) {
                int index;
                try {
                    index = Integer.parseInt(strings[1]);
                } catch(Exception e) {
                    return List.of();
                }
                index--;

                List<AlertMessage> alerts = plugin.getAlertHandler().getAlerts();
                if(index >= 0 && index <= (alerts.size() - 1)) {
                    String text = alerts.get(index).getRawText();
                    if(text != null) return List.of(text);
                }
            }

        return List.of();
    }
}
