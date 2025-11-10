package nu.nerd.nerdmessage.alerts;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import nu.nerd.nerdmessage.NerdMessage;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static nu.nerd.nerdmessage.ColourUtils.color;


public class AlertHandler {


    private NerdMessage plugin;
    private List<AlertMessage> alerts;
    private File yamlFile;
    private FileConfiguration yaml;
    private BukkitRunnable runnable;
    private int index;


    public AlertHandler(NerdMessage plugin) {
        this.plugin = plugin;
        this.yamlFile = new File(plugin.getDataFolder(), "alerts.yml");
        start();
    }


    /**
     * Start the alert rotation after loading the messages and settings from disk
     */
    public void start() {
        this.yaml = YamlConfiguration.loadConfiguration(yamlFile);
        loadAlerts();
        index = yaml.getInt("index", 0);
        int seconds = yaml.getInt("seconds", 200);
        runnable = new BukkitRunnable() {
            @Override
            public void run() {
                if (index >= alerts.size()) {
                    index = 0;
                }
                if (!alerts.isEmpty() && alerts.get(index) != null) {
                    broadcast(alerts.get(index));
                }
                index++;
            }
        };
        runnable.runTaskTimer(plugin, 20L*seconds, 20L*seconds);
    }


    /**
     * Stop the alert rotation and save state to disk
     */
    public void stop() {
        runnable.cancel();
        try {
            yaml.load(yamlFile);
            yaml.set("index", index);
            yaml.save(yamlFile);
        } catch (IOException|InvalidConfigurationException ex) {
            plugin.getLogger().warning("Could not write alerts.yml");
        }
    }


    /**
     * Load the alerts from the config
     */
    private void loadAlerts() {
        alerts = new ArrayList<>();
        String text, configColor;
        for (Map<?, ?> map : yaml.getMapList("alerts")) {
            text = (String) map.get("text");
            configColor = (String) map.get("color");
            alerts.add(new AlertMessage(text, color(configColor)));
        }
    }


    /**
     * Serialize the alerts into the YAML configuration object
     */
    private void serializeAlerts() {
        List<Map<String, String>> maps = new ArrayList<>();
        for (AlertMessage alert : alerts) {
            Map<String, String> m = new HashMap<>();
            m.put("text", alert.getRawText());
            m.put("color", alert.getColor().toString().toUpperCase());
            maps.add(m);
        }
        yaml.set("alerts", maps);
    }


    /**
     * Format and send the message to all users online
     */
    private void broadcast(AlertMessage msg) {
        for(Player player : Bukkit.getOnlinePlayers()) {
            player.sendMessage(Component.text("[Server] ", msg.getColor()).append(msg.getText()));
        }
    }


    /**
     * Get the active alert messages
     */
    public List<AlertMessage> getAlerts() {
        return alerts;
    }


    /**
     * Add a new alert to the rotation
     * @param alert the alert to add
     * @param index the index to put it at
     * @return true if successful
     */
    public boolean addAlert(AlertMessage alert, int index) {
        try {
            alerts.add(index, alert);
            serializeAlerts();
            yaml.save(yamlFile);
        } catch (IOException ex) {
            return false;
        }
        return true;
    }

    /**
     * Edit an already existing alert
     * @param text   the new text of the alert
     * @param index  the index of the alert being edited
     * @return true if successful
     */
    public boolean editAlert(String text, int index) {
        try {
            AlertMessage alertMessage = alerts.get(index);
            alertMessage.setText(text);
            serializeAlerts();
            yaml.save(yamlFile);
        } catch (IOException ex) {
            return false;
        }
        return true;
    }


    /**
     * Remove an alert from the rotation
     * @param index the index to remove
     * @return AlertMessage if successful or null
     */
    public AlertMessage removeAlert(int index) {
        AlertMessage alert;
        try {
            alert = alerts.remove(index);
            serializeAlerts();
            yaml.save(yamlFile);
        } catch (IOException ex) {
            return null;
        }
        return alert;
    }


    /**
     * Change the interval in seconds that alerts are broadcast
     * @param seconds interval
     */
    public void setInterval(int seconds) {
        try {
            runnable.cancel();
            yaml.load(yamlFile);
            yaml.set("seconds", seconds);
            yaml.save(yamlFile);
            start();
        } catch (IOException|InvalidConfigurationException ex) {
            plugin.getLogger().warning("Could not write alerts.yml");
        }
    }


    /**
     * Get the interval in seconds between broadcasts
     */
    public int getInterval() {
        return yaml.getInt("seconds", 200);
    }


}
