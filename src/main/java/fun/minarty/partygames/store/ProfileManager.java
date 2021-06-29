package fun.minarty.partygames.store;

import cc.pollo.store.mongo.MongoStore;
import fun.minarty.api.user.User;
import fun.minarty.grand.model.user.DefaultUser;
import fun.minarty.partygames.PartyGames;
import fun.minarty.partygames.api.model.game.PlayMode;
import fun.minarty.partygames.api.model.profile.GameStatistic;
import fun.minarty.partygames.api.model.profile.Profile;
import fun.minarty.partygames.manager.LevelManager;
import fun.minarty.partygames.manager.StatisticManager;
import fun.minarty.partygames.model.game.GamePlayer;
import fun.minarty.partygames.model.game.PartyGame;
import fun.minarty.partygames.model.game.ScoreTableEntry;
import fun.minarty.partygames.model.profile.DefaultProfile;

import java.util.UUID;

/**
 * Manages party profiles, such as loading and saving and such
 */
public class ProfileManager {

    private final PartyGames plugin;
    private final MongoStore<UUID, Profile, DefaultProfile> playerProfileStore;

    public ProfileManager(PartyGames plugin,
                          MongoStore<UUID, Profile, DefaultProfile> playerProfileStore) {

        this.plugin = plugin;
        this.playerProfileStore = playerProfileStore;
    }

    public Profile getProfileByUniqueId(UUID uniqueId) {
        return playerProfileStore.get(uniqueId);
    }

    public void update(Profile profile) {
        playerProfileStore.update((DefaultProfile) profile);
    }

    public void applyScore(PartyGame game, ScoreTableEntry score) {
        GamePlayer player = score.getPlayer();
        int xpReward = score.getReward();

        UUID uniqueId = player.getUniqueId();

        StatisticManager statisticManager = plugin.getStatisticManager();

        // Game statistic - wins
        if (score.isFirstPlace())
            statisticManager.incrementStatistic(uniqueId, GameStatistic.WINS, 1);

        // Game statistic - points high score
        if(game.getConfig().getMode() == PlayMode.POINTS){
            long previous = statisticManager.getLongStatistic(uniqueId, GameStatistic.POINTS_HIGH_SCORE);
            long current  = score.getPoints();

            if(current > previous){
                statisticManager.setStatistic(uniqueId, GameStatistic.POINTS_HIGH_SCORE, current);
            }
        }

        Profile profile = getProfileByUniqueId(uniqueId);

        // Apply XP and show level/xp info messages to the player
        if (profile != null) {
            LevelManager levelManager = plugin.getLevelManager();
            int before = levelManager.getLevelForXp(profile.getXp());
            profile.setXp(profile.getXp() + xpReward);
            int after = levelManager.getLevelForXp(profile.getXp());

            levelManager.showLevelProgress(player.getBukkitPlayer(), profile.getXp(), before, after);

            update(profile);
        }

        // Update weekly XP
        plugin.getCommon()
                .getRedisManager()
                .getLeaderboardManager()
                .addWeeklyXp(uniqueId, xpReward);

        User user = plugin.getGrand().getApi().getUserByUniqueId(uniqueId);
        if (user != null) {
            user.setBalance(user.getBalance() + score.getCoins()); // TODO hmm, maybe use Vault economy in Grand?
            plugin.getGrand().getApi().updateUser((DefaultUser) user);
        }
    }

}