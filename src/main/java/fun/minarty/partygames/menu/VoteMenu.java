package fun.minarty.partygames.menu;

import cc.pollo.gladeus.item.StackBuilder;
import cc.pollo.gladeus.menu.model.Menu;
import cc.pollo.gladeus.menu.model.MenuItem;
import fun.minarty.partygames.PartyGames;
import fun.minarty.partygames.api.model.game.GameType;
import fun.minarty.partygames.model.game.GamePlayer;
import fun.minarty.partygames.model.vote.GameVote;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static net.kyori.adventure.text.Component.translatable;

/**
 * Menu that is shown to players when they wish to vote for a game,
 * shows the 3 available types to vote for
 */
public class VoteMenu extends Menu {

    private final PartyGames plugin;

    public VoteMenu(PartyGames plugin, Player player) {
        super(Component.translatable("menus.vote.title"), 1, player, 20, false);
        this.plugin = plugin;
    }

    @Override
    public void onTick() {
        Player player = getPlayer();
        GamePlayer gamePlayer = plugin.getPlayerManager()
                .getGamePlayerByPlayer(player);

        if(!plugin.getVoteManager().hasVote()
                || gamePlayer == null
                || !gamePlayer.isReady())

            player.closeInventory();
    }

    @Override
    public void buildMenu() {
        if (!plugin.getVoteManager().hasVote())
            return;

        List<GameType> types = new ArrayList<>(plugin.getVoteManager().getVote().getTypes());
        filledRows(Material.GRAY_STAINED_GLASS_PANE, 1);

        for (int i = 0; i < types.size(); i++) {
            GameType type = types.get(i);

            String displayMaterial = plugin.getConfig().getString("display." + type.name().toLowerCase(Locale.ROOT));
            if(displayMaterial == null)
                displayMaterial = Material.GRASS_BLOCK.name();

            Material display = Material.getMaterial(displayMaterial);
            if(display == null)
                continue;

            set(i + 3, new MenuItem(StackBuilder.localized(plugin.getTexty(), display)
                    .displayName(translatable("games." + type.name().toLowerCase() + ".name", NamedTextColor.DARK_AQUA))
                    .lore(translatable("games." + type.name().toLowerCase() + ".description",
                            Style.style(NamedTextColor.GRAY, TextDecoration.ITALIC))),

                    click -> {

                        Player player = click.getPlayer();

                        if (!plugin.getVoteManager().hasVote())
                            return;

                        GameVote vote = plugin.getVoteManager().getVote();
                        GamePlayer gamePlayer = plugin.getPlayerManager()
                                .getGamePlayerByPlayer(player);

                        if (gamePlayer == null)
                            return;

                        if (vote.hasVoted(gamePlayer)) {
                            player.sendMessage(translatable("vote.already", NamedTextColor.RED));
                            return;
                        }

                        vote.vote(gamePlayer, type);
                        plugin.getTexty().getChat().sendWithDefaultPrefix(player, translatable("vote.voted", NamedTextColor.GRAY,
                                Component.translatable("games." + type.name().toLowerCase() + ".name", NamedTextColor.DARK_AQUA)));

                        player.closeInventory();
                    }));
        }
    }

}