package nu.nerd.nerdmessage.alerts;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.ChatColor;

import static nu.nerd.nerdmessage.ColourUtils.formatMiniMessage;


public class AlertMessage {


    private String text;
    private TextColor color;


    public AlertMessage(String text) {
        this.text = text;
        this.color = NamedTextColor.LIGHT_PURPLE;
    }


    public AlertMessage(String text, TextColor color) {
        this.text = text;
        this.color = color;
    }


    public Component getText() {
        return formatMiniMessage(text);
    }


    public String getRawText() {
        return text;
    }


    public TextColor getColor() {
        return color;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setColor(TextColor color) {
        this.color = color;
    }
}
