package nu.nerd.nerdmessage.database;

import java.util.Map;

import javax.annotation.Nonnegative;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;

import org.bukkit.configuration.file.FileConfiguration;

import com.avaje.ebean.validation.NotNull;
import com.google.common.collect.Maps;

@Entity
@Table(name ="nerdmailsettings")
public class NerdMailSettings {

    public static final int MAX_USERNAME_LENGTH = 16;

    @Id
    @Column(length = MAX_USERNAME_LENGTH)
    private String user_name;

    @Enumerated(EnumType.STRING)
    private SortType sortType;

    @NotNull
    @Nonnegative
    private int previewLength;

    @NotNull
    @Nonnegative
    private int pagesize;

    @NotNull
    private int maxInboxSize;

    public NerdMailSettings() {
        //default constructor
    }

    public NerdMailSettings(SortType sortType, int previewLength, int pagesize, int maxInboxSize) {
        this.setSortType(sortType);
        this.setPreviewLength(previewLength);
        this.setPagesize(pagesize);
        this.setMaxInboxSize(maxInboxSize);
    }

    public static NerdMailSettings constructDefault(FileConfiguration config) {
        int previewLength = config.getInt("newmailboxdefaults.inbox.previewlength");
        int pagesize = config.getInt("newmailboxdefaults.inbox.pagesize");
        int maxInboxSize = config.getInt("newmailboxdefaults.inbox.maxinboxsize");
        final SortType sortType = SortType.getSortType(config.getString("newmailboxdefaults.inbox.sort"));

        return new NerdMailSettings(sortType, previewLength, pagesize, maxInboxSize);
    }

    public enum SortType {
        NEWEST_FIRST, OLDEST_FIRST, UNREAD_FIRST;

        public static final Map<String, SortType> typeMap = Maps.newHashMap();
        static {
            typeMap.put(NEWEST_FIRST.name().toLowerCase(), NEWEST_FIRST);
            typeMap.put(OLDEST_FIRST.name().toLowerCase(), OLDEST_FIRST);
            typeMap.put(UNREAD_FIRST.name().toLowerCase(), UNREAD_FIRST);
        }

        public static SortType getSortType(String key) {
            SortType sortType = typeMap.get(key);
            if (sortType == null) {
                // invalid option, just silently go with a safe choice.
                sortType = NEWEST_FIRST;
            }
            return sortType;
        }
    }

    public String getUser_name() {
        return user_name;
    }

    public void setUser_name(String user_name) {
        this.user_name = user_name;
    }

    public SortType getSortType() {
        return sortType;
    }

    public void setSortType(SortType sortType) {
        this.sortType = sortType;
    }

    public int getPreviewLength() {
        return previewLength;
    }

    public void setPreviewLength(int previewLength) {
        this.previewLength = previewLength > 0 ? previewLength : Integer.MAX_VALUE;
    }

    public int getPagesize() {
        return pagesize;
    }

    public void setPagesize(int pagesize) {
        this.pagesize = pagesize >= 0 ? pagesize : Integer.MAX_VALUE;
    }

    public int getMaxInboxSize() {
        return maxInboxSize;
    }

    public void setMaxInboxSize(int maxInboxSize) {
        this.maxInboxSize = maxInboxSize >= 0 ? maxInboxSize : Integer.MAX_VALUE;
    }
}
