package fun.minarty.partygames.state.feature;

import fun.minarty.partygames.PartyGames;
import fun.minarty.partygames.model.game.GamePlayer;
import fun.minarty.partygames.model.game.PartyGame;
import fun.minarty.partygames.state.StateListen;
import fun.minarty.partygames.util.Cuboid;
import fun.minarty.partygames.util.Cuboid;
import net.kyori.adventure.text.Component;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerMoveEvent;

public class CheckpointFeature extends Feature {

    public CheckpointFeature(PartyGame game, PartyGames plugin) {
        super(game, plugin);
    }

    @StateListen
    public void onPlayerMove(PlayerMoveEvent event){
        Player player = event.getPlayer();
        Location location = player.getLocation();

        GamePlayer gamePlayer = plugin.getPlayerManager().getGamePlayerByPlayer(player);
        if(gamePlayer == null)
            return;

        Cuboid previous = (Cuboid) gamePlayer.getData("checkpoint");
        int size = game.getConfig().getCheckpoints().size();

        for (int i = 0; i < size; i++) {
            Cuboid checkpoint = game.getConfig().getCheckpoints().get(i);
            if (checkpoint.containsLocation(location) && (previous == null || !previous.equals(checkpoint))) {
                gamePlayer.setData("checkpoint", checkpoint);
                player.sendActionBar(Component.text(ChatColor.AQUA + "Checkpoint " + (i+1)+"/" + size));
            }
        }
    }

}