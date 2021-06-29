package fun.minarty.partygames.manager;

import fun.minarty.partygames.PartyGames;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.Set;

import static net.kyori.adventure.text.Component.*;

/**
 * Manages XP and leveling up
 */
public class LevelManager {

    private final PartyGames plugin;
    private int[] levels;

    public LevelManager(PartyGames plugin) {
        this.plugin = plugin;
        loadLevels();
    }

    /**
     * Loads available levels from config and maps them
     * by index to array with XP as value
     */
    private void loadLevels() {
        ConfigurationSection levelSection = plugin.getConfig().getConfigurationSection("levels");
        if (levelSection == null)
            return;

        Set<String> keys = levelSection.getKeys(false);
        levels = new int[keys.size() + 1];

        keys.forEach(key -> {
            int level = Integer.parseInt(key);
            this.levels[level] = levelSection.getInt(key);
        });
    }

    /**
     * Gets the maximum level that the XP meets requirements for
     *
     * @param xp xp to check with
     * @return maximum level
     */
    public int getLevelForXp(int xp) {
        int level = 0;

        for (int i = 0; i < levels.length; i++) {
            if (xp >= levels[i]) {
                level = i;
            } else {
                break;
            }
        }

        return level;
    }

    /**
     * Applies an XP reward to a player and sends information <br>
     * about their progress to the next level.
     *
     * @param player Bukkit player to send message to
     * @param before level before applying to player
     * @param after  level after applying to player
     */
    public void showLevelProgress(Player player, int xp, int before, int after) {
        if (player == null)
            return;

        player.sendMessage(text("   ----------------------------------", NamedTextColor.AQUA));

        Component margin = text("      ");

        Component status;
        if (after > before) {
            status = margin
                    .append(text("||", Style.style(NamedTextColor.AQUA, TextDecoration.OBFUSCATED)))
                    .append(space())
                    .append(translatable("level.status.level_up", NamedTextColor.GRAY, text(after, NamedTextColor.AQUA)));

            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
        } else {
            status = margin
                    .append(text(xp, NamedTextColor.AQUA)
                            .append(text(" / ", NamedTextColor.GRAY))
                            .append(text(levels[after + 1]))
                            .append(space())
                            .append(Component.text("XP")))
                    .append(space())
                    .append(translatable("level.status.xp", NamedTextColor.GRAY,
                            text(after + 1, NamedTextColor.AQUA)));
        }

        player.sendMessage(
                text(" ")
                        .append(Component.newline())
                        .append(status)
                        .append(space())
                        .append(Component.newline()));
    }

}