package fun.minarty.partygames.model.misc;

/**
 * Enum for different spawn modes, this determines where the players
 * will be teleported on as the game starts
 */
public enum SpawnMode {

    /**
     * Teleports to the first location in the list
     */
    FIRST,

    /**
     * Teleports to a random location in the list
     */
    RANDOM,

    /**
     * Fills the players by index to the list
     */
    FILL

}