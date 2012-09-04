package nu.nerd.nerdmessage.mailCommandHandler;

import org.bukkit.command.CommandSender;

public interface MailCommandHandler {
    boolean handle(CommandSender sender, String[] args);
}
