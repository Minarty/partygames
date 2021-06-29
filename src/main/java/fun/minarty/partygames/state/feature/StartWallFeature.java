package fun.minarty.partygames.state.feature;

import fun.minarty.partygames.PartyGames;
import fun.minarty.partygames.model.game.PartyGame;
import fun.minarty.partygames.util.Cuboid;
import org.bukkit.Material;

/**
 * Feature which adds a wall which is removed when the game starts
 */
public class StartWallFeature extends Feature {

    private final Cuboid startWall;

    public StartWallFeature(PartyGame game, PartyGames plugin) {
        super(game, plugin, 0);
        this.startWall = game.getConfig().getStartWall();
    }

    @Override
    protected void onStart() {
        startWall.fill(Material.AIR);
    }

}