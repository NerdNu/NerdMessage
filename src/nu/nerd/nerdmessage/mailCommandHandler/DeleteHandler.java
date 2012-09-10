package nu.nerd.nerdmessage.mailCommandHandler;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import nu.nerd.nerdmessage.NerdMessage;
import nu.nerd.nerdmessage.database.NerdMail;
import nu.nerd.nerdmessage.database.NerdMailManager;
import nu.nerd.nerdmessage.database.NerdMailbox;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import com.google.common.base.Optional;
import com.google.common.collect.Sets;

public class DeleteHandler extends AbstractInboxIndexHandler {

    public DeleteHandler(NerdMessage plugin, NerdMailManager manager) {
        super(plugin, manager);
    }

    @Override
    protected boolean processIndexes(CommandSender sender, Collection<Integer> parsedIndexes) {
        
        //TODO: if the user receives a new message between viewing the inbox and deleting, user-visible indexes will have changed possibly causing them to delete the wrong message.
        
        final Optional<NerdMailbox> mailbox = getManager().getMailbox(sender.getName());
        final List<NerdMail> mail;
        if (mailbox.isPresent()) {
            mail = mailbox.get().getMail();
        } else {
            mail = Collections.emptyList();
        }
        final Set<NerdMail> toDelete = Sets.newHashSetWithExpectedSize(mail.size());
        for (Integer userVisibleIndex : parsedIndexes) {
            if (userVisibleIndex > 0 && userVisibleIndex <= mail.size()) {
                toDelete.add(mail.get(userVisibleIndex - 1));
            }
        }
        getManager().deleteItems(toDelete, sender.getName());
        sender.sendMessage(ChatColor.RED + "Messages deleted.");
        return true;
    }

}
