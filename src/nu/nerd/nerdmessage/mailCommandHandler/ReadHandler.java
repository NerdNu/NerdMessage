package nu.nerd.nerdmessage.mailCommandHandler;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import nu.nerd.nerdmessage.NerdMessage;
import nu.nerd.nerdmessage.database.NerdMail;
import nu.nerd.nerdmessage.database.NerdMailManager;
import nu.nerd.nerdmessage.database.NerdMailbox;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;

public class ReadHandler extends AbstractInboxIndexHandler {

    public ReadHandler(NerdMessage plugin, NerdMailManager manager) {
        super(plugin, manager);
    }

    @Override
    protected boolean processIndexes(CommandSender sender, Collection<Integer> parsedIndexes) {
        if (parsedIndexes.isEmpty()) {
            return false;
        }
        
        final Optional<NerdMailbox> mailbox = getManager().getMailbox(sender.getName());
        final List<NerdMail> mail;
        if (mailbox.isPresent()) {
            mail = mailbox.get().getMail();
        } else {
            mail = Collections.emptyList();
        }
        final List<NerdMail> readMail = Lists.newArrayListWithCapacity(parsedIndexes.size());
        for (Integer userVisibleIndex : parsedIndexes) {
            if (userVisibleIndex <= 0 || userVisibleIndex > mail.size()) {
                sender.sendMessage(ChatColor.RED + "Couldn't find message #" + userVisibleIndex + "!");
                continue;
            }
            final NerdMail nerdMail = mail.get(userVisibleIndex - 1);
            readMail.add(nerdMail);
            
            final StringBuilder builder = new StringBuilder();
            
            builder.append(ChatColor.WHITE).append(userVisibleIndex).append(")[");
            builder.append(ChatColor.GREEN).append(nerdMail.getFrom_user());
            builder.append(ChatColor.WHITE).append("] ");
            builder.append(nerdMail.getMessage());
            //Reset after message in case the user has done some formatting
            builder.append(ChatColor.RESET);
            
            sender.sendMessage(builder.toString());
        }
        
        getManager().markItemsRead(readMail, sender.getName());
        return true;
    }

}
