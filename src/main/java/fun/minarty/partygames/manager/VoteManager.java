package fun.minarty.partygames.manager;

import fun.minarty.partygames.api.model.game.GameType;
import fun.minarty.partygames.store.StoreProvider;
import fun.minarty.partygames.model.vote.GameVote;
import fun.minarty.partygames.util.RandomUtil;
import lombok.Getter;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Manager which handles game voting
 */
public class VoteManager {

    private final Map<GameType, Double> typeProbabilities = new HashMap<>();
    @Getter
    private GameVote vote;

    private final StoreProvider storeProvider;
    private final Random random = new Random();

    public VoteManager(StoreProvider storeProvider){
        this.storeProvider = storeProvider;
        Arrays.stream(GameType.values())
                .forEach(type -> typeProbabilities.put(type, 10.0));
    }

    /**
     * Starts a new vote
     */
    public void startVote(){
        vote = new GameVote();

        // Add 3 random types
        for (int i = 0; i < 3; i++) {
            addRandomType();
        }

        Arrays.stream(GameType.values()).filter(type -> !vote.getTypes().contains(type))
                .forEach(type -> typeProbabilities.put(type, typeProbabilities.get(type) + 10));
    }

    /**
     * Ends the active vote
     * @return GameType with the most votes
     */
    public GameType endVote(){
        GameType winningType = vote.getWinningType();
        typeProbabilities.put(winningType, 5.0);

        vote = null;
        return winningType;
    }

    /**
     * Adds a random type by weight to the active voting types
     */
    private void addRandomType(){
        Map<GameType, Double> probabilities = new HashMap<>();

        // Only add types which are not already in the active vote
        typeProbabilities.entrySet().stream()
                .filter(entry -> !vote.getTypes().contains(entry.getKey()))
                .forEach(entry -> probabilities.put(entry.getKey(), entry.getValue()));

        vote.addType(RandomUtil.getWeightedRandom(probabilities, random));
    }

    /**
     * Returns value based on if a vote is active
     * @return whether or not a vote is active
     */
    public boolean hasVote(){
        return vote != null;
    }

}