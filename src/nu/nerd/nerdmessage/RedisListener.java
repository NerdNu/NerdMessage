package nu.nerd.nerdmessage;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import redis.clients.jedis.JedisPubSub;

import java.util.UUID;

public class RedisListener extends JedisPubSub {


    private NerdMessage plugin;


    public RedisListener(NerdMessage plugin) {
        this.plugin = plugin;
    }


    @Override
    public void onPMessage(final String pattern, final String channel, final String message) {
        if (channel.equalsIgnoreCase("nerdmessage.mbg")) {
            for(Player player : plugin.getPlayersWithPerm("nerdmessage.mb")) {
                player.sendMessage(message);
            }
        }
        else if (channel.equalsIgnoreCase("nerdmessage.abg")) {
            for(Player player : plugin.getPlayersWithPerm("nerdmessage.ab")) {
                player.sendMessage(message);
            }
        }
        else if (channel.equalsIgnoreCase("nerdmessage.globalbroadcast")) {
            for(Player player : Bukkit.getOnlinePlayers()) {
                player.sendMessage(message);
            }
        }
        else if (channel.equalsIgnoreCase("nerdmessage.mail.new")) {
            new BukkitRunnable() {
                public void run() {
                    plugin.getMailHandler().notifyNewMessages(UUID.fromString(message), false);
                }
            }.runTaskAsynchronously(plugin);
        }
    }


}
