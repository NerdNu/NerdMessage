package nu.nerd.nerdmessage.mailCommandHandler;

import nu.nerd.nerdmessage.database.NerdMailManager;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class ClearHandler implements MailCommandHandler {

    private final NerdMailManager manager;

    public ClearHandler(NerdMailManager manager) {
        this.manager = manager;
    }

    public boolean handle(CommandSender sender, String[] args) {
        manager.deleteMailForUser(sender.getName());
        sender.sendMessage(ChatColor.RED + "Cleared inbox.");
        return true;
    }

}
