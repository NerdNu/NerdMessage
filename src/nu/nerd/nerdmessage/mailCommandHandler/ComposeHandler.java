package nu.nerd.nerdmessage.mailCommandHandler;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import nu.nerd.nerdmessage.NerdMessage;
import nu.nerd.nerdmessage.database.NerdMailManager;

import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.conversations.Conversable;
import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.ConversationFactory;
import org.bukkit.conversations.MessagePrompt;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;
import org.bukkit.entity.Player;

import com.google.common.collect.Lists;

//TODO: This class duplicates daft amounts of logic from sendHandler, should be combined.

public class ComposeHandler implements MailCommandHandler {

    private static final Pattern USERNAME_PATTERN = Pattern.compile("[a-zA-Z0-9_]{0,16}?");

    private final ConversationFactory conversationFactory;
    private final NerdMessage plugin;

    private final NerdMailManager manager;

    public ComposeHandler(NerdMessage plugin, NerdMailManager manager) {
        this.plugin = plugin;
        this.manager = manager;
        this.conversationFactory = new ConversationFactory(plugin).withModality(true).withFirstPrompt(new InstructionPrompt()).withEscapeSequence("/cancel");
    }

    public boolean handle(CommandSender sender, String[] args) {
        if (sender instanceof Conversable) {

            if (!sender.hasPermission("nerdmessage.mail.send")) {
                sender.sendMessage(ChatColor.RED + "You do not have the permissions required to send mail.");
                return true;
            }

            if (args.length < 2) {
                return false;
            }

            final List<String> usernames = Lists.newArrayListWithCapacity(args.length);
            final String[] newArgs = Arrays.copyOfRange(args, 1, args.length);
            for (String username : newArgs) {
                if (isValidName(username) && manager.isUserRegistered(username)) {
                    usernames.add(username);
                } else {
                    sender.sendMessage(ChatColor.RED + username + " is not a known username!");
                    // dont try and recover, they can just issue the command
                    // again.
                    return false;
                }
            }
            
            final Conversation composeConversation = conversationFactory.buildConversation((Conversable)sender);
            final ConversationContext context = composeConversation.getContext();
            context.setSessionData("recipients", usernames);
            composeConversation.begin();
        }
        return false;
    }
    
    //TODO: The three methods below are copied from the SendHandler... lazy, should refactor.
    
    private void notifyFullInbox(String recipientName) {
        final Player recipient = plugin.getServer().getPlayer(recipientName);
        if (recipient != null) {
            recipient.sendMessage(ChatColor.YELLOW + "Someone is trying to send you mail, but your inbox is full! Use \"/mail inbox\" to view and \"/mail delete\" to clear some space.");
        }
    }

    private void notifyRecipient(String recipientName, CommandSender sender) {
        final Player recipient = plugin.getServer().getPlayer(recipientName);
        
        if (recipient != null) {
            recipient.sendMessage(ChatColor.GREEN + "New mail from " + sender.getName() + "! Use \"/mail inbox\" to view.");
        }
    }
    
    private boolean isValidName(String recipientName) {
        return USERNAME_PATTERN.matcher(recipientName).matches();
    }

    
    private class InstructionPrompt extends MessagePrompt {

        public String getPromptText(ConversationContext context) {
            StringBuilder builder = new StringBuilder(250);
            builder.append(ChatColor.GREEN);
            builder.append("Composing message for: ");
            
            //Here be dragons, too lazy to check :p
            @SuppressWarnings("unchecked")
            final List<String> recipients = (List<String>) context.getSessionData("recipients");
            int i = 0;
            for (String name : recipients) {
                builder.append(name);
                if (++i != recipients.size()) {
                    builder.append(", ");
                }
            }
            builder.append(".\n");
            builder.append(ChatColor.RESET);
            builder.append("All chat messages you send will be appended to your new mail message with a space following. Send \"/n\" in a chat message to insert a newline in your mail message.");
            builder.append("Send \"/cancel\" to quit without sending your mail message, or /send to send your mail message.");
            
            return builder.toString();
        }

        @Override
        protected Prompt getNextPrompt(ConversationContext context) {
            return new ComposePrompt();
        }
    }
    
    private class ComposePrompt extends StringPrompt {

        public String getPromptText(ConversationContext context) {
            final Object messageObject = context.getSessionData("message");
            final StringBuilder message;
            if (messageObject != null) {
                message = (StringBuilder)messageObject;
                
                return ChatColor.YELLOW + "Your message so far:\n" + message.toString() + ChatColor.GREEN;
            } else {
                message = new StringBuilder(200);
                context.setSessionData("message", message);
                return "";
            }
            
        }

        public Prompt acceptInput(ConversationContext context, String input) {
            //Being naughty and assuming getPrompttext will always be called first.
            final StringBuilder message = (StringBuilder)context.getSessionData("message");
            
            final Prompt exitPrompt = captureExitCommand(input, context, message);
            if (exitPrompt != null) {
                return exitPrompt;
            }
            
            if("/n".equals(input.trim())) {
                message.append("\n");
            } else {
                message.append(input).append(" ");
            }
            
            return new ComposePrompt();
        }

        /**
         * Checks to see if the input is one of the exit commands, and returns prompts to perform the relevant actions, otherwise null. 
         * @param input
         * @param context 
         * @param message 
         * @return Boolean indicating if the conversation should now end.
         */
        @SuppressWarnings("unchecked")
        private Prompt captureExitCommand(String input, ConversationContext context, StringBuilder message) {
            input = StringUtils.strip(input);
            
            if ("/send".equals(input)) {
                //more dragons!
                return sendMessage(context.getForWhom(), ((StringBuilder)context.getSessionData("message")), (List<String>) context.getSessionData("recipients"));
            } 
            if ("/cancel".equals(input)) {
                return Prompt.END_OF_CONVERSATION;
            }
            
            return null;
        }
        
        public Prompt sendMessage(Conversable forWhom, StringBuilder message, List<String> recipients) {
            //take care
            final CommandSender sender = (CommandSender)forWhom;
            if (manager.isThrottled(sender)) {
                manager.setLastActionTime(sender);
                return new EndPrompt(ChatColor.RED + "Throttled! Wait ten seconds and try again.");
            }
            
            manager.setLastActionTime(sender);
            
            final StringBuilder builder = new StringBuilder(100);
            for (String recipientName : recipients) {
                if (!manager.isInboxFull(recipientName) || sender.hasPermission("nerdmessage.mail.ignorefull")) {
                    boolean sent = manager.send(message.toString(), recipientName, sender);
                    if (sent) {
                        notifyRecipient(recipientName, sender);
                    }
                    builder.append(ChatColor.GREEN).append("Message sent to: ").append(recipientName).append("\n");
                } else {
                    //TODO:Blocked users shouldn't be able to trigger this message
                    notifyFullInbox(recipientName);
                    builder.append(ChatColor.RED).append("Users inbox is full: ").append(recipientName).append("\n");
                }
            }
            return new EndPrompt(builder.toString());
        }
    }
    
    private final class EndPrompt extends MessagePrompt {
        
        private String message;

        public EndPrompt(String message) {
            this.message = message;
            
        }
        
        public String getPromptText(ConversationContext arg0) {
            return message;
        }

        @Override
        protected Prompt getNextPrompt(ConversationContext arg0) {
            return Prompt.END_OF_CONVERSATION;
        }
    }
}
