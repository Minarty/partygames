package fun.minarty.partygames.scoreboard;

import cc.pollo.gladeus.scoreboard.builder.ScoreboardBuilder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;

import java.util.function.BiFunction;

/**
 * Contains entry builders for various
 */
public final class ScoreboardConstants {

    public static final String TRANSLATABLE_PREFIX = "scoreboard.";

    private ScoreboardConstants(){}

    public static Component TITLE = Component.text("Minarty Games", Style.style(NamedTextColor.AQUA, TextDecoration.BOLD));

    public static ScoreboardBuilder.FormatFunction FORMAT = (key, value) ->
                Component.text("▎ ", NamedTextColor.AQUA)
                        .append(key.color(NamedTextColor.DARK_AQUA))
                        .append(Component.text(":", NamedTextColor.GRAY))
                        .append(Component.text(" ", NamedTextColor.AQUA).append(value));

    public static Component formatTopPlayerEntry(int place, Component name, int points){
        Component component = (name != null ? name : Component.text("-", NamedTextColor.GRAY));

        Component value = name != null ?
                Component.text(" -", NamedTextColor.GOLD)
                        .append(Component.text(" "))
                        .append(Component.text(String.valueOf(points), NamedTextColor.AQUA)) : Component.empty();

        return Component.text((place+1) + " ", NamedTextColor.DARK_AQUA)
                .append(component)
                .append(value);
    }

}