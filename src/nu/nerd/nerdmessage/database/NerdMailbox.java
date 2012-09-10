package nu.nerd.nerdmessage.database;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;

public class NerdMailbox {

    private List<NerdMail> mailForUser;
    private final NerdMailSettings settingsForUser;
    private final Set<String> blockedUsers;
    private boolean sorted = false;

    public NerdMailbox(List<NerdMail> mailForUser, NerdMailSettings settingsForUser, Set<String> blockedUsers) {
        this.mailForUser = mailForUser;
        this.settingsForUser = settingsForUser;
        this.blockedUsers = blockedUsers;
        sort();
    }

    public boolean hasNewMail() {
        for (NerdMail mail : mailForUser) {
            if (!mail.isRead())
                return true;
        }
        return false;
    }

    public void addMail(NerdMail nerdMail) {
        sort();
        switch (getSettingsForUser().getSortType()) {
        case OLDEST_FIRST:
            mailForUser.add(nerdMail);
            break;
        case NEWEST_FIRST:
        case UNREAD_FIRST:
        default:
            mailForUser.add(0, nerdMail);
            break;
        }
    }
    
    public void deleteMail(final Set<NerdMail> toDelete) {
        Collection<NerdMail> filtered = Collections2.filter(mailForUser, new Predicate<NerdMail>() {

            public boolean apply(@Nullable NerdMail arg0) {
                return toDelete.contains(arg0);
            }});
        mailForUser.clear();
        mailForUser.addAll(filtered);
    }

    private void sort() {
        if (!sorted ) {
            switch (getSettingsForUser().getSortType()) {
            case NEWEST_FIRST:
                Collections.sort(mailForUser, new Comparator<NerdMail>() {
                    public int compare(NerdMail o1, NerdMail o2) {
                        return o2.getSent().compareTo(o1.getSent());
                    }});
                break;
            case OLDEST_FIRST:
                Collections.sort(mailForUser, new Comparator<NerdMail>() {
                    public int compare(NerdMail o1, NerdMail o2) {
                        return o1.getSent().compareTo(o2.getSent());
                    }});
                break;
            case UNREAD_FIRST:
                Collections.sort(mailForUser, new Comparator<NerdMail>() {
                    public int compare(NerdMail o1, NerdMail o2) {
                        if (o1.isRead() && o2.isRead()) {
                            return o2.getSent().compareTo(o1.getSent());
                        } else if (o1.isRead()) {
                            return 1;
                        } else {
                            return -1;
                        }
                    }});
                break;
            default:
                break;
            }
            sorted = true;
        }
    }

    public boolean isFull() {
        return mailForUser.size() >= getSettingsForUser().getMaxInboxSize();
    }

    public void setSorted(boolean b) {
        this.sorted = b;
    }

    public List<NerdMail> getMail() {
        sort();
        return mailForUser;
    }

    public boolean isBlocked(String user) {
        return getBlockedUsers().contains(user);
    }

    public NerdMailSettings getSettingsForUser() {
        return settingsForUser;
    }

    public Set<String> getBlockedUsers() {
        return blockedUsers;
    }

}
