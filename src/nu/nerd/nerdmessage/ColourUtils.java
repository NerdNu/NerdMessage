package nu.nerd.nerdmessage;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A class that holds helper methods related to the PaperMC text system.
 */
public class ColourUtils {

    private static final MiniMessage mm = MiniMessage.miniMessage();

    /**
     * Builds a list of valid colour entries.
     * @return the list of valid colour entries
     */
    public static List<String> colorList() {
        List<String> colorList = new ArrayList<>();
        Set<String> blacklist = new HashSet<>();
        blacklist.add("MAGIC");
        blacklist.add("BOLD");
        blacklist.add("STRIKETHROUGH");
        blacklist.add("UNDERLINE");
        blacklist.add("ITALIC");
        blacklist.add("RESET");
        for(String color : NamedTextColor.NAMES.keys()) {
            colorList.add(color.toUpperCase());
        }
        colorList.removeAll(blacklist);
        return colorList;
    }

    /**
     * Takes a colour's name/hex code and tries to match it with a TextColor representation of it.
     * @param color the name of the colour
     * @return the TextColor that represents the input
     */
    public static TextColor color(String color) {
        return color.charAt(0) == '#' ? TextColor.fromHexString(color) : NamedTextColor.NAMES.value(color.toLowerCase());
    }

    /**
     * Convert a String into a Component and parse its MiniMessage tags, if any exist.
     * @param text the text to parse
     * @return a formatted Component
     */
    public static Component formatMiniMessage(String text) {
        return mm.deserialize(text);
    }

}
