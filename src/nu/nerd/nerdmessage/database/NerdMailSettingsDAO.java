package nu.nerd.nerdmessage.database;

import java.util.concurrent.ExecutionException;

import nu.nerd.nerdmessage.NerdMessage;

import com.google.common.base.Optional;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;

public class NerdMailSettingsDAO {
    
    private static final int SETTINGS_CACHE_SIZE = 20;
    private final  NerdMessage plugin;
    //Nerdmail manager sometimes needs to make repeat requests, this small cache is hidden in hear to keep that (already untidy) method cleaner.
    private final Cache<String, Optional<NerdMailSettings>> settingsCache;
    
    public NerdMailSettingsDAO(final NerdMessage plugin) {
        this.plugin = plugin;
        settingsCache = CacheBuilder.newBuilder().maximumSize(SETTINGS_CACHE_SIZE).build(new CacheLoader<String, Optional<NerdMailSettings>>() {
            @Override
            public Optional<NerdMailSettings> load(String user) throws Exception {
                final NerdMailSettings queryResult = plugin.getDatabase().find(NerdMailSettings.class).where().eq("user_name", user).findUnique();
                return Optional.fromNullable(queryResult);
            }
        });
    }
    
    public Optional<NerdMailSettings> getSettingsForUser(String user) {
        try {
            return settingsCache.get(user);
        } catch (ExecutionException e) {
            return Optional.fromNullable(null);
        }
    }
    
    public void storeSettings(NerdMailSettings settings) {
        plugin.getDatabase().save(settings);
        settingsCache.invalidate(settings.getUser_name());
    }
    
    public void updateSettings(NerdMailSettings settings) {
        plugin.getDatabase().update(settings);
        settingsCache.invalidate(settings.getUser_name());
    }
    
    public void deleteSettings(NerdMailSettings settings) {
        plugin.getDatabase().delete(settings);
        settingsCache.invalidate(settings.getUser_name());
    }
}
