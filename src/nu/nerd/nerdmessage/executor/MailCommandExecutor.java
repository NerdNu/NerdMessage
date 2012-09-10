package nu.nerd.nerdmessage.executor;

import java.util.Map;

import nu.nerd.nerdmessage.NerdMessage;
import nu.nerd.nerdmessage.database.NerdMailManager;
import nu.nerd.nerdmessage.mailCommandHandler.BlockHandler;
import nu.nerd.nerdmessage.mailCommandHandler.ClearHandler;
import nu.nerd.nerdmessage.mailCommandHandler.ComposeHandler;
import nu.nerd.nerdmessage.mailCommandHandler.DeleteHandler;
import nu.nerd.nerdmessage.mailCommandHandler.InboxHandler;
import nu.nerd.nerdmessage.mailCommandHandler.MailCommandHandler;
import nu.nerd.nerdmessage.mailCommandHandler.ReadHandler;
import nu.nerd.nerdmessage.mailCommandHandler.SendHandler;
import nu.nerd.nerdmessage.mailCommandHandler.SettingsHandler;
import nu.nerd.nerdmessage.mailCommandHandler.ViewSettingsHandler;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import com.google.common.collect.Maps;

public class MailCommandExecutor implements CommandExecutor {
    
    private final Map<String, MailCommandHandler> handlers;

    public MailCommandExecutor(NerdMessage plugin, NerdMailManager manager) {
        handlers = Maps.newHashMap();
        handlers.put("send", new SendHandler(plugin, manager));
        handlers.put("inbox", new InboxHandler(plugin, manager));
        handlers.put("read", new ReadHandler(plugin, manager));
        handlers.put("compose", new ComposeHandler(plugin, manager));
        handlers.put("delete", new DeleteHandler(plugin, manager));
        handlers.put("clear", new ClearHandler(manager));
        handlers.put("block", new BlockHandler(plugin, manager));
        handlers.put("settings", new SettingsHandler(plugin, manager));
        handlers.put("view-settings", new ViewSettingsHandler(plugin, manager));
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 1) {
            //show the usage
            return false;
        }
        
        final MailCommandHandler handler = handlers.get(args[0].toLowerCase());
        
        if (handler == null) {
            sender.sendMessage(ChatColor.RED + "Unknown option: /" + label + " " + args[0]);
        } else {
            return handler.handle(sender, args);
        }
        
        return true;
    }

}
