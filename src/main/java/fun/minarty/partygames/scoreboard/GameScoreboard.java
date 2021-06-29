package fun.minarty.partygames.scoreboard;

import cc.pollo.gladeus.scoreboard.Scoreboard;
import cc.pollo.gladeus.scoreboard.ScoreboardEntry;
import cc.pollo.gladeus.scoreboard.builder.ScoreboardBuilder;
import fun.minarty.partygames.api.model.game.PlayMode;
import fun.minarty.partygames.model.game.GamePlayer;
import fun.minarty.partygames.model.game.PartyGame;
import fun.minarty.partygames.util.TextUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import static fun.minarty.partygames.scoreboard.ScoreboardConstants.formatTopPlayerEntry;

/**
 * Scoreboard for when the player is in a game
 */
public class GameScoreboard extends Scoreboard {

    private final PartyGame game;

    public GameScoreboard(@NotNull PartyGame game) {
        super(ScoreboardConstants.TITLE);
        this.game = game;
    }

    @Override
    public ScoreboardBuilder build() {

        // TODO think about ScoreboardEntryBuilder mutability
        ScoreboardBuilder general = ScoreboardBuilder.builder(ScoreboardConstants.FORMAT, ScoreboardConstants.TRANSLATABLE_PREFIX)
                .empty()
                .formattedTranslatable("game", Component.translatable("games." + game.getType().name().toLowerCase() + ".name"))
                .formattedTranslatable("time", Component.text(TextUtil.formatGameDuration(game.getConfig().getDuration())));

        PlayMode mode = game.getConfig().getMode();

        if (mode == PlayMode.PRESENCE) {
            general = general
                    .empty()
                    .formattedTranslatable("players", Component.text(String.valueOf(game.getActivePlayers().size())));
        }

        if (mode == PlayMode.POINTS) {
            general = general.empty().id("topTitle",
                    new ScoreboardEntry(Component.translatable("scoreboard.top",
                            Style.style(NamedTextColor.AQUA, TextDecoration.BOLD))));

            int entriesToAdd = Math.min(game.getActivePlayers().size(), 5);
            for (int i = 0; i < entriesToAdd; i++) {
                GamePlayer gamePlayer = game.getActivePlayers().get(i);
                if (gamePlayer == null)
                    continue;

                Player p = gamePlayer.getBukkitPlayer();
                if (p == null)
                    continue;

                general = general.id("topEntry" + i, new ScoreboardEntry(formatTopPlayerEntry(i, p.displayName(), 0)));
            }
        }

        general = general.empty();
        return general;
    }

}