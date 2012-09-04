package nu.nerd.nerdmessage.mailCommandHandler;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import nu.nerd.nerdmessage.NerdMessage;
import nu.nerd.nerdmessage.database.NerdMailManager;

import org.apache.commons.lang.StringUtils;
import org.bukkit.command.CommandSender;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public abstract class AbstractInboxIndexHandler implements MailCommandHandler {

    private final NerdMessage plugin;
    private final NerdMailManager manager;

    public AbstractInboxIndexHandler(NerdMessage plugin, NerdMailManager manager) {
        this.plugin = plugin;
        this.manager = manager;
    }

    public boolean handle(CommandSender sender, String[] args) {
        if (validArgs(args)) {
            return false;
        }
        
        final Collection<Integer> parsedIndexes = parseIndexes(args);
        return processIndexes(sender, parsedIndexes);
    }

    protected abstract boolean processIndexes(CommandSender sender, Collection<Integer> parsedIndexes);

    private boolean validArgs(String[] args) {
        return args.length < 2;
    }

    private Collection<Integer> parseIndexes(String[] args) {
        final String[] rawIndexList = StringUtils.join(args, "", 1, args.length).split(",");
        
        final List<Integer> indexList = Lists.newArrayListWithExpectedSize(args.length * 2);
        for (String indexExpr : rawIndexList) {
            try {
                indexList.add((Integer.parseInt(indexExpr)));
                continue;
            } catch (NumberFormatException e) {
                //fall through
            }
            try {
                final String[] rawRange = indexExpr.split("-");
                if (rawRange.length != 2) continue;
                final int rangeStart = Integer.parseInt(rawRange[0]);
                final int rangeEnd = Integer.parseInt(rawRange[1]);
                if (rangeStart >= rangeEnd) continue;
                for (int i = rangeStart; i <= rangeEnd; i++) {
                    indexList.add(i);
                }
            } catch (NumberFormatException e) {
                //fall through
            }
        }
        Collections.sort(indexList);
        return Sets.<Integer>newLinkedHashSet(indexList);
    }

    public NerdMessage getPlugin() {
        return plugin;
    }

    public NerdMailManager getManager() {
        return manager;
    }

}
