package fun.minarty.partygames.model.vote;

import fun.minarty.partygames.api.model.game.GameType;
import fun.minarty.partygames.model.game.GamePlayer;
import lombok.Getter;
import lombok.Setter;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Object for an active vote, contains participants and votes
 */
public class GameVote {

    @Getter
    private final Set<GamePlayer> participants = new HashSet<>();
    private final Map<GameType, Integer> votes = new HashMap<>();

    @Setter
    private GameType forced;

    public void vote(GamePlayer player, GameType type){
        votes.put(type, votes.get(type) + 1);
        participants.add(player);
    }

    public boolean hasVoted(GamePlayer player){
        return participants.contains(player);
    }

    public void addType(GameType type){
        votes.put(type, 0);
    }

    public Set<GameType> getTypes(){
        return votes.keySet();
    }

    public GameType getWinningType(){
        if(forced != null)
            return forced;

        List<GameType> collect = votes.entrySet().stream()
                .sorted((Map.Entry.comparingByValue()))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        Collections.reverse(collect);
        return collect.get(0);
    }

}