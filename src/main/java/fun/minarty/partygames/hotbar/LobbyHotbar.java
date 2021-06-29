package fun.minarty.partygames.hotbar;

import cc.pollo.gladeus.hotbar.model.Hotbar;
import cc.pollo.gladeus.hotbar.model.HotbarItem;
import cc.pollo.gladeus.hotbar.model.HotbarItemCollection;
import cc.pollo.gladeus.hotbar.model.HotbarItemState;
import cc.pollo.gladeus.item.StackBuilder;
import cc.pollo.texty.Texty;
import fun.minarty.partygames.PartyGames;
import fun.minarty.partygames.model.game.GamePlayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;

/**
 * Hotbar applied when players are in the lobby
 */
public class LobbyHotbar extends Hotbar {

    private final Texty texty;
    private final PartyGames plugin;

    public LobbyHotbar(Player player, Texty texty, PartyGames plugin){
        super(player);
        this.texty = texty;
        this.plugin = plugin;
    }

    @Override
    public HotbarItemCollection build() {
        return HotbarItemCollection.create()
                .set(3, new HotbarItem(true,
                        new HotbarItemState(StackBuilder.localized(texty, Material.LIME_DYE)
                                .displayName(Component.translatable("hotbar.ready.go_afk", NamedTextColor.AQUA)), click -> {

                            GamePlayer gamePlayer = plugin.getPlayerManager().getGamePlayerByPlayer(click.getPlayer());
                            if(gamePlayer == null)
                                return;

                            gamePlayer.setReady(false);

                        }),
                        new HotbarItemState(StackBuilder.localized(texty, Material.GRAY_DYE)
                                .displayName(Component.translatable("hotbar.ready.go_ready", NamedTextColor.AQUA)), click -> {

                            GamePlayer gamePlayer = plugin.getPlayerManager().getGamePlayerByPlayer(click.getPlayer());
                            if(gamePlayer == null)
                                return;

                            gamePlayer.setReady(true);

                        })))
                .set(4, new HotbarItem(false,
                        new HotbarItemState(StackBuilder.localized(texty,  Material.COMPASS)
                                .displayName(Component.translatable("hotbar.spectate", NamedTextColor.AQUA)),
                                click -> Bukkit.dispatchCommand(click.getPlayer(), "spectate")),
                        new HotbarItemState(StackBuilder.localized(texty, Material.NETHER_STAR)
                                .displayName(Component.translatable("hotbar.vote", NamedTextColor.AQUA)),
                                click -> Bukkit.dispatchCommand(click.getPlayer(), "gamevote"))))
                .set(5, new HotbarItem(StackBuilder.localized(texty, Material.HONEYCOMB)
                                .displayName(Component.translatable("hotbar.cosmetics", NamedTextColor.AQUA)),
                        click -> click.getPlayer().sendMessage(Component.translatable("hotbar.cosmetics_ready", NamedTextColor.RED))));
    }

}