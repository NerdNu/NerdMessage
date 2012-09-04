package nu.nerd.nerdmessage.mailCommandHandler;

import nu.nerd.nerdmessage.NerdMessage;
import nu.nerd.nerdmessage.database.NerdMailManager;
import nu.nerd.nerdmessage.database.NerdMailSettings;
import nu.nerd.nerdmessage.database.NerdMailbox;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import com.google.common.base.Optional;

public class ViewSettingsHandler implements MailCommandHandler {

    private NerdMailManager manager;

    public ViewSettingsHandler(NerdMessage plugin, NerdMailManager manager) {
        this.manager = manager;
    }

    public boolean handle(CommandSender sender, String[] args) {
        
        if (!sender.hasPermission("nerdmessage.mail.settings.view")) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to use this feature.");
            return true;
        }
        
        final String target;
        
        if (args.length > 1) {
            target = args[1].toLowerCase();
        } else {
            target = sender.getName().toLowerCase();
        }
        
        if (!target.equals(sender.getName().toLowerCase()) && !sender.hasPermission("nerdmessage.mail.settings.admin.view")) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to view setting for other users.");
            return true;
        }
        
        final Optional<NerdMailbox> mailbox = manager.getMailbox(target);
        
        if (mailbox.isPresent()) {
            final NerdMailSettings settingsForUser = mailbox.get().getSettingsForUser();
            final StringBuilder builder = new StringBuilder(100);
            
            /*TODO: again making it a pain in the ass to add new settings. in order to do so need to:
                *Change the description text in plugin.yml.
                *Add to the settings object, make sure it persists ok.
                *Change the view settings handler
                *change the settings handler
                *Change any handler that needs to be aware of the new setting.
            Ideally it should just be bullet two and four. */
            builder.append(ChatColor.GREEN);
            builder.append("Settings for user: ").append(target).append("\n");
            builder.append("Max inbox size: ").append(settingsForUser.getMaxInboxSize()).append("\n");
            builder.append("Inbox page size: ").append(settingsForUser.getPagesize()).append("\n");
            builder.append("Inbox preview length: ").append(settingsForUser.getPreviewLength()).append("\n");
            builder.append("Inbox sort order: ").append(settingsForUser.getSortType().name()).append("\n");
            
            sender.sendMessage(builder.toString());
            return true;
            
        } else {
            sender.sendMessage(ChatColor.RED + "Could not find user: " + target);
            return true;
        }
    }

}
