package nu.nerd.nerdmessage;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public class NerdMessage extends JavaPlugin {

    List<NMUser> users = new ArrayList<NMUser>();
    WorldGuardPlugin worldGuard = null;
    
    @Override
    public void onEnable() {
        worldGuard = getWorldGuard();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String name, String[] args) {
        if (args.length == 0)
            return false;
        if (command.getName().equalsIgnoreCase("msg") || command.getName().equalsIgnoreCase("cmsg")) {
            NMUser user = null;
            //Player receiver = null;
            CommandSender receiver = null;
            String message;
            if ("r".equalsIgnoreCase(name) || "reply".equalsIgnoreCase(name)) {
                message = Join(args, 0);
                user = getUser(sender.getName());
                if (user == null || user.getReplyTo() == null) {
                    sender.sendMessage(ChatColor.RED + "No user to reply to.");
                    return true;
                }
            } else {
                message = Join(args, 1);
            }

            if (user == null) {
                if (args[0].equalsIgnoreCase("console")) {
                    receiver = getServer().getConsoleSender();
                }
                else if (args[0].contains("r:")) {
                    if (worldGuard == null) {
                        System.out.println("!! Could not get WorldGuard from Bukkit !!");
                        return false;
                    }
                    
                    ProtectedRegion region = null;
                    String regionName = args[0].replace("r:", "");

                    if (sender instanceof Player) {
                        World w = ((Player)sender).getWorld();
                        RegionManager rm = worldGuard.getRegionManager(w);
                        if (rm.hasRegion(regionName)) {
                            region = rm.getRegionExact(regionName);
                        }
                    }
                    else if (sender instanceof ConsoleCommandSender) {
                        System.out.println("Silly Console, you cannot /msg a region");
                        return true;
                    }

                    if (region == null) {
                        sender.sendMessage(ChatColor.RED + "Region could not be found");
                        if (user != null) {
                            user.setReplyTo(null);
                        }
                        return true;
                    }

                    if (!region.isMember(sender.getName())) {
                        sender.sendMessage(ChatColor.RED + "You cannot message a region that you do not belong to");
                        return true;
                    }
                    
                    Set<String> players = region.getOwners().getPlayers();
                    players.addAll(region.getMembers().getPlayers());
                            
                    if (players.size() < 1) {
                        sender.sendMessage(ChatColor.RED + "No players in region");
                        return true;
                    }
                    
                    sender.sendMessage("[" + ChatColor.RED + "Me" + ChatColor.WHITE + " -> " + ChatColor.GOLD + regionName + ChatColor.WHITE + "] " + message);

                    for (String player : players) {
                        NMUser u = getOrCreateUser(player);
                        u.setReplyTo("r:" + regionName);
                        
                        if (sender.getName().equals(u.getName()))
                            continue;
                        
                        receiver = getPlayer(player);

                        if (name.equalsIgnoreCase("cmsg")) {
                            receiver.sendMessage("[" + ChatColor.RED + sender.getName() + ChatColor.WHITE + " -> " + ChatColor.GOLD + regionName + ChatColor.WHITE + "] " + ChatColor.GREEN + message);
                        }
                        else {
                            System.out.println(u.getName() + ":/msg " + regionName + " " + message);
                            receiver.sendMessage("[" + ChatColor.RED + sender.getName() + ChatColor.WHITE + " -> " + ChatColor.GOLD + regionName + ChatColor.WHITE + "] " + message);
                        }

                        if (receiver != getServer().getConsoleSender()) {
                            System.out.println("[" + sender.getName() + " -> " + regionName + "] " + message);
                        }
                    }

                    return true;
                }
                else {
                    receiver = getPlayer(args[0]);
                }
                user = getOrCreateUser(sender.getName());
            } else {
                if (user.getReplyTo().equalsIgnoreCase("console")) {
                    receiver = getServer().getConsoleSender();
                } 
                else if (user.getReplyTo().contains("r:")) {
                    if (worldGuard == null) {
                        System.out.println("!! Could not get WorldGuard from Bukkit !!");
                        return false;
                    }
                    
                    ProtectedRegion region = null;
                    String regionName = user.getReplyTo().replace("r:", "");

                    if (sender instanceof Player) {
                        World w = ((Player)sender).getWorld();
                        RegionManager rm = worldGuard.getRegionManager(w);
                        if (rm.hasRegion(regionName)) {
                            region = rm.getRegionExact(regionName);
                        }
                    }
                    else if (sender instanceof ConsoleCommandSender) {
                        System.out.println("Silly Console, you cannot /msg a region");
                        return true;
                    }

                    if (region == null) {
                        sender.sendMessage(ChatColor.RED + "Region could not be found");
                        if (user != null) {
                            user.setReplyTo(null);
                        }
                        return true;
                    }

                    if (!region.isMember(sender.getName())) {
                        sender.sendMessage(ChatColor.RED + "You cannot message a region that you do not belong to");
                        return true;
                    }
                    
                    Set<String> players = region.getOwners().getPlayers();
                    players.addAll(region.getMembers().getPlayers());
                            
                    if (players.size() < 1) {
                        sender.sendMessage(ChatColor.RED + "No players in region");
                        return true;
                    }
                    
                    sender.sendMessage("[" + ChatColor.RED + "Me" + ChatColor.WHITE + " -> " + ChatColor.GOLD + regionName + ChatColor.WHITE + "] " + message);

                    for (String player : players) {
                        NMUser u = getOrCreateUser(player);
                        u.setReplyTo("r:" + regionName);
                        
                        if (sender.getName().equals(u.getName()))
                            continue;
                        
                        receiver = getPlayer(player);

                        if (name.equalsIgnoreCase("cmsg")) {
                            receiver.sendMessage("[" + ChatColor.RED + sender.getName() + ChatColor.WHITE + " -> " + ChatColor.GOLD + regionName + ChatColor.WHITE + "] " + ChatColor.GREEN + message);
                        }
                        else {
                            System.out.println(u.getName() + ":/msg " + regionName + " " + message);
                            receiver.sendMessage("[" + ChatColor.RED + sender.getName() + ChatColor.WHITE + " -> " + ChatColor.GOLD + regionName + ChatColor.WHITE + "] " + message);
                        }

                        if (receiver != getServer().getConsoleSender()) {
                            System.out.println("[" + sender.getName() + " -> " + regionName + "] " + message);
                        }
                    }

                    return true;
                }
                else {
                    receiver = getPlayer(user.getReplyTo());
                }
            }
            
            if (receiver == null) {
                sender.sendMessage(ChatColor.RED + "User is not online.");
                if (user != null) {
                    user.setReplyTo(null);
                }
                return true;
            }

            NMUser r = getOrCreateUser(receiver.getName());

            r.setReplyTo(user.getName());
            user.setReplyTo(receiver.getName());

            if (name.equalsIgnoreCase("cmsg")) {
                sender.sendMessage("[" + ChatColor.RED + "Me" + ChatColor.WHITE + " -> " + ChatColor.GOLD + receiver.getName() + ChatColor.WHITE + "] " + message);
                receiver.sendMessage("[" + ChatColor.RED + sender.getName() + ChatColor.WHITE + " -> " + ChatColor.GOLD + "Me" + ChatColor.WHITE + "] " + ChatColor.GREEN + message);
            }
            else {
                
                sender.sendMessage("[" + ChatColor.RED + "Me" + ChatColor.WHITE + " -> " + ChatColor.GOLD + receiver.getName() + ChatColor.WHITE + "] " + message);
                receiver.sendMessage("[" + ChatColor.RED + sender.getName() + ChatColor.WHITE + " -> " + ChatColor.GOLD + "Me" + ChatColor.WHITE + "] " + message);
            }
            
            if (receiver != getServer().getConsoleSender()) {
                System.out.println("[" + sender.getName() + " -> " + receiver.getName() + "] " + message);
            }
            return true;
        }
        else if (command.getName().equalsIgnoreCase("me")) {
            if (sender instanceof Player) {
                getServer().broadcastMessage("* " + ChatColor.stripColor(sender.getName()) + " " + Join(args, 0));
            }
        }
        
        return false;
    }
    
    public Player getPlayer(final String name) {
        Player[] players = getServer().getOnlinePlayers();

        Player found = null;
        String lowerName = name.toLowerCase();
        int delta = Integer.MAX_VALUE;
        for (Player player : players) {
            if (ChatColor.stripColor(player.getName()).toLowerCase().startsWith(lowerName)) {
                int curDelta = player.getName().length() - lowerName.length();
                if (curDelta < delta) {
                    found = player;
                    delta = curDelta;
                }
                if (curDelta == 0) break;
            }
        }
        return found;
    }

    public String Join(String[] args, int start) {
        String s = "";
        for (int i = start; i < args.length; i++) {
            if (s.length() > 0) {
                s += " ";
            }

            s += args[i];
        }
        return s;
    }

    public NMUser addUser(String username) {
        username = ChatColor.stripColor(username);
        NMUser u = new NMUser(this, username);
        users.add(u);
        return u;
    }

    public NMUser getOrCreateUser(String username) {
        username = ChatColor.stripColor(username);
        NMUser u = getUser(username);
        if (u == null) {
            u = addUser(username);
        }

        return u;
    }

    public NMUser getUser(String username) {
        username = ChatColor.stripColor(username);
        for (NMUser u : users) {
            if (username.equalsIgnoreCase(u.getName())) {
                return u;
            }
        }

        return null;
    }
    
    public void removeUser(String username) {
        NMUser u = getUser(username);
        if (u != null) {
            users.remove(u);
        }
    }
    
    private WorldGuardPlugin getWorldGuard() {
        Plugin plugin = getServer().getPluginManager().getPlugin("WorldGuard");
 
        if (plugin == null || !(plugin instanceof WorldGuardPlugin)) {
            return null; // Maybe you want throw an exception instead
        }

        return (WorldGuardPlugin) plugin;
    }
}
