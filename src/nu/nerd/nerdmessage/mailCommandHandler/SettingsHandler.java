package nu.nerd.nerdmessage.mailCommandHandler;

import nu.nerd.nerdmessage.NerdMessage;
import nu.nerd.nerdmessage.database.NerdMailManager;
import nu.nerd.nerdmessage.database.NerdMailSettings;
import nu.nerd.nerdmessage.database.NerdMailSettings.SortType;
import nu.nerd.nerdmessage.database.NerdMailbox;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import com.google.common.base.Optional;

public class SettingsHandler implements MailCommandHandler {

    private static final String MAX_INBOX_SIZE = "max_inbox_size";
    private static final String SORT_TYPE = "sort_type";
    private static final String PAGE_SIZE = "page_size";
    private static final String PREVIEW_LENGTH = "preview_length";
    private final NerdMailManager manager;

    public SettingsHandler(NerdMessage plugin, NerdMailManager manager) {
        this.manager = manager;
    }

    //TODO: Refactor settings in general. Blegh.
    public boolean handle(CommandSender sender, String[] args) {
        if (args.length == 1) {
            printHelp(sender);
            return true;
        }
        
        if (args.length < 3) {
            return false;
        }
        final String key = args[1];
        final String value = args[2];
        final String target;
        
        if (args.length > 3) {
            target = args[3].toLowerCase();
        } else {
            target = sender.getName().toLowerCase();
        }
        
        if (!sender.getName().toLowerCase().equals(target) && !sender.hasPermission("nerdmessage.mail.settings.admin")) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to change other users settings.");
            return true;
        }
        
        Optional<NerdMailbox> mailbox = manager.getMailbox(target);
        
        if (!mailbox.isPresent()) {
            sender.sendMessage(ChatColor.RED + "Could not find mailbox for " + target);
            return true;
        }
        
        final NerdMailSettings settingsForUser = mailbox.get().getSettingsForUser();
        
        //TODO: this is the worst thing ever. 
        if (MAX_INBOX_SIZE.equals(key)) {
            if (sender.hasPermission("nerdmessage.mail.settings.internal")) {
                try {
                    int newVal = Integer.parseInt(value);
                    settingsForUser.setMaxInboxSize(newVal);
                } catch (Exception e) {
                    return false;
                }
            } else {
                sender.sendMessage(ChatColor.RED + "You do not have permission to change " + key);
                return true;
            }
        } else if (SORT_TYPE.equals(key)) {
            if (sender.hasPermission("nerdmessage.mail.settings.display")) {
                SortType sortType = SortType.typeMap.get(value);
                if (sortType != null) {
                    settingsForUser.setSortType(sortType);
                } else {
                    sender.sendMessage(ChatColor.RED + "Invalid sort type.");
                }
            } else {
                sender.sendMessage(ChatColor.RED + "You do not have permission to change " + key);
                return true;
            }
        } else if (PAGE_SIZE.equals(key)) {
            if (sender.hasPermission("nerdmessage.mail.settings.display")) {
                try {
                    int newVal = Integer.parseInt(value);
                    settingsForUser.setPagesize(newVal);
                } catch (Exception e) {
                    return false;
                }
            } else {
                sender.sendMessage(ChatColor.RED + "You do not have permission to change " + key);
                return true;
            }
        } else if (PREVIEW_LENGTH.equals(key)) {
            if (sender.hasPermission("nerdmessage.mail.settings.display")) {
                try {
                    int newVal = Integer.parseInt(value);
                    settingsForUser.setPreviewLength(newVal);
                } catch (Exception e) {
                    return false;
                }
            } else {
                sender.sendMessage(ChatColor.RED + "You do not have permission to change " + key);
                return true;
            }
        }
        
        manager.saveSettings(settingsForUser, target);
        sender.sendMessage(ChatColor.GREEN + "Settings saved!");
        return true;
    }

    private void printHelp(CommandSender sender) {
        StringBuilder stringBuilder = new StringBuilder(200);
        stringBuilder.append(ChatColor.GOLD + "All settings avaliable to you:\n");
        if (sender.hasPermission("nerdmessage.mail.settings.display")) {
            appendDescription(stringBuilder, PAGE_SIZE, "Any number greater than zero. -1 for no limit.", "Sets the number of messages to be displayed per page of the inbox.");
            appendDescription(stringBuilder, PREVIEW_LENGTH, "Any number, negative values indicate no limit.", "Sets the maximum length of the message previews shown in the inbox.");
            appendDescription(stringBuilder, SORT_TYPE, "One of: " + SortType.typeMap.keySet().toString(), "Sets the sort order for the inbox.");
        } 
        if (sender.hasPermission("nerdmessage.mail.settings.display")){
            appendDescription(stringBuilder, MAX_INBOX_SIZE, "Any number, negatives indicate infinite. (well all right, a big number.)", "Sets the maximum number of messages that can be stored in a user's inbox.");
        }
        sender.sendMessage(stringBuilder.toString());
    }

    private void appendDescription(StringBuilder stringBuilder, String key, String value, String description) {
        stringBuilder.append(ChatColor.GOLD).append("Key: ").append(ChatColor.GREEN).append(key);
        stringBuilder.append(ChatColor.GOLD).append(" value: ").append(ChatColor.GREEN).append(value);
        stringBuilder.append(ChatColor.GOLD).append(" Description: ").append(ChatColor.GREEN).append(description).append("\n");
    }

}
