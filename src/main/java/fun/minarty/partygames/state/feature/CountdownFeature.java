package fun.minarty.partygames.state.feature;

import fun.minarty.partygames.PartyGames;
import fun.minarty.partygames.model.game.PartyGame;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

/**
 * Feature which shows game description and countdown title
 * before the game starts
 */
public class CountdownFeature extends Feature {

    private boolean complete;

    public static final char[] COUNTDOWN_NUMBERS = new char[]{'\u2780','\u2781','\u2782'};

    public CountdownFeature(PartyGame game, PartyGames plugin, int duration) {
        super(game, plugin, duration);
    }

    @Override
    public boolean isReadyToEnd() {
        return complete;
    }

    @Override
    protected void onStart() {
        for (Player bukkitPlayer : game.getBukkitPlayers()) {
            Component component = Component.newline()
                    .append(Component.text(" > ", NamedTextColor.DARK_AQUA))
                    .append(Component.translatable("games." + game.getType().name().toLowerCase() + ".name", NamedTextColor.AQUA))
                    .append(Component.text(" "))
                    .append(Component.text("(", NamedTextColor.GRAY))
                    .append(Component.translatable("game.start.author", NamedTextColor.GRAY,
                            Component.text(game.getConfig().getAuthor(), NamedTextColor.AQUA)))
                    .append(Component.text(")", NamedTextColor.GRAY))
                    .append(Component.newline())
                    .append(Component.text(" > ", NamedTextColor.DARK_AQUA))
                    .append(Component.translatable("games." + game.getType().name().toLowerCase() + ".description",
                            Style.style(NamedTextColor.GRAY, TextDecoration.ITALIC)))
                    .append(Component.newline());

            bukkitPlayer.sendMessage(component);
            /*
            plugin.getTexty().getChat().send(bukkitPlayer, Component.translatable("game.start.description",
                    Component.translatable("games." + game.getType().name().toLowerCase() + ".name"),
                    Component.text(game.getConfig().getAuthor()),
                    Component.translatable("games." + game.getType().name().toLowerCase() + ".description")));*/
        }
    }

    @Override
    public void onUpdate() {
        long i = plugin.getGameManager().getMainState().getRemainingSeconds();
        if(i > 0){
            game.getBukkitPlayers().forEach(player
                    -> player.sendTitle(ChatColor.DARK_AQUA + ""
                        + COUNTDOWN_NUMBERS[(int) (i-1)], " ", 20, 20, 20));
        }

        if(i == 0)
            complete = true;
    }

}