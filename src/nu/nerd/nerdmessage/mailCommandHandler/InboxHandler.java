package nu.nerd.nerdmessage.mailCommandHandler;

import java.text.BreakIterator;
import java.util.Collections;
import java.util.List;

import nu.nerd.nerdmessage.NerdMessage;
import nu.nerd.nerdmessage.database.NerdMail;
import nu.nerd.nerdmessage.database.NerdMailManager;
import nu.nerd.nerdmessage.database.NerdMailbox;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import com.google.common.base.Optional;

public class InboxHandler implements MailCommandHandler {

    private static final int PAGE_SIZE_FALLBACK = 5;
    private static final int PREVIEW_LENGTH_FALLBACK = 40;
    private final NerdMailManager manager;

    public InboxHandler(NerdMessage plugin, NerdMailManager manager) {
        this.manager = manager;
    }

    public boolean handle(CommandSender sender, String[] args) {
        final String user = sender.getName();
        int page = 1;
        if (args.length > 1) {
            try {
                page = Integer.parseInt(args[1]);
                if (page <= 0) page = 1;
            } catch (NumberFormatException e) {
                //just ignore and go with the sensible default. 
            }
        }
        
        final Optional<NerdMailbox> mailbox = manager.getMailbox(user);
        final List<NerdMail> mail;
        final int previewLength;
        final int pageSize;
        if (mailbox.isPresent()) {
            mail = mailbox.get().getMail();
            previewLength = mailbox.get().getSettingsForUser().getPreviewLength();
            pageSize = mailbox.get().getSettingsForUser().getPagesize();
        } else {
            //TODO: can this ever be the case? Class was written before per-user settings were introduced need to confirm it handles new cases gracefully.
            mail = Collections.emptyList();
            previewLength = PREVIEW_LENGTH_FALLBACK;
            pageSize = PAGE_SIZE_FALLBACK;
        }
        
        //early out for empty mailbox
        if (mail.isEmpty()) {
            sender.sendMessage(ChatColor.YELLOW + "No mail for " + user);
            return true;
        }
        
        final int pages;
        if (pageSize == Integer.MAX_VALUE) {
            pages = 1;
        } else {
            pages = Math.max((mail.size() + pageSize - 1) / pageSize, 1);
        }
        
        if (page > pages) page = pages;
        
        
        final int startIndex = (pageSize * (page - 1));
        final int endIndex = Math.min((startIndex + pageSize) -1, mail.size() - 1);
        
        final StringBuilder messagePreviews = new StringBuilder(75 * mail.size());
        
        NerdMail nerdMail;
        for (int i = startIndex; i <= endIndex; i++) {
            nerdMail = mail.get(i);
            formatMessagePreview(i, nerdMail, messagePreviews, previewLength);
        }
        
        //now print the inbox
        inboxInfo(sender, user, page, pages);
        sender.sendMessage(messagePreviews.toString());
        sender.sendMessage(ChatColor.YELLOW + "Use \"/mail\" to see all available options. \"/mail inbox <page>\" for more pages.");
        return true;
    }

    private void formatMessagePreview(int index, NerdMail nerdMail, StringBuilder builder, int previewSize) {
        final int id = index + 1;
        final String from_user = nerdMail.getFrom_user();
        final String message = nerdMail.getMessage();
        final boolean isRead = nerdMail.isRead();
        
        builder.append(ChatColor.WHITE).append(id).append(")[");
        builder.append(ChatColor.GREEN).append(from_user);
        builder.append(ChatColor.WHITE).append("] ");
        if (!isRead) {
            builder.append(ChatColor.WHITE).append("[");
            builder.append(ChatColor.RED).append("UNREAD");
            builder.append(ChatColor.WHITE).append("]");
        }
        
        if (message.length() > previewSize && previewSize > 0) {
            final BreakIterator bi = BreakIterator.getWordInstance();
            bi.setText(message);
            int truncatePoint = bi.preceding(previewSize);
            builder.append(message.substring(0, truncatePoint));
            //Reset after message in case the user has done some formatting
            builder.append(ChatColor.RESET).append("...");
        } else if (previewSize != 0){
          //Reset after message in case the user has done some formatting
            builder.append(message).append(ChatColor.RESET);
        }
        builder.append("\n");
    }

    private void inboxInfo(CommandSender sender, final String user, int page, final int pages) {
        final StringBuilder builder = new StringBuilder(50);
        builder.append(ChatColor.YELLOW).append("Inbox for ").append(user).append(" : [page ").append(page).append(" of ").append(pages).append("]");

        sender.sendMessage(builder.toString());
    }

}
