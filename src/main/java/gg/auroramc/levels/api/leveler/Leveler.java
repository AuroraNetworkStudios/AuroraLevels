package gg.auroramc.levels.api.leveler;

import gg.auroramc.aurora.api.levels.MatcherManager;
import gg.auroramc.aurora.api.message.Placeholder;
import gg.auroramc.levels.api.data.LevelData;
import org.bukkit.entity.Player;

import java.util.List;

public interface Leveler {
    /**
     * Get the level data of a player
     *
     * @param player    player to get the data for
     * @return          level data of the player
     */
    LevelData getUserData(Player player);

    /**
     * Adds the given amount of xp to the player. It will handle level ups, events
     * notifications and rewards and everything else.
     * Only call this method from the main thread or from global region scheduler!.
     *
     * @param player   player to add the xp to
     * @param xp       amount of xp to add
     */
    void addXpToPlayer(Player player, double xp);

    /**
     * Set the player's level to the given level. It will handle level ups, events
     * notifications and rewards and everything else.
     *
     * @param player    player to set the level for
     * @param level     level to set
     */
    void setPlayerLevel(Player player, int level);

    /**
     * Set the player's level to the given level without sending notifications or rewards and events.
     * You shouldn't really use this method unless you know what you're doing.
     * Reward correctors will still be executed on user data load.
     *
     * @param player    player to set the level for
     * @param level     level to set
     */
    void setPlayerLevelRaw(Player player, int level);

    /**
     * Get the xp required for the given level
     *
     * @param level     level to get the xp for
     * @return          xp required for the level
     */
    double getXpForLevel(int level);

    /**
     * Get how much xp the player needs to level up currently
     *
     * @param player        player to get the required xp for
     * @return              xp required for the player to level up
     */
    double getRequiredXpForLevelUp(Player player);

    /**
     * Get the placeholders for the reward formulas
     *
     * @param player    player to get the placeholders for
     * @param level     level to get the placeholders for
     * @return          list of placeholders for the reward formulas
     */
    List<Placeholder<?>> getRewardFormulaPlaceholders(Player player, int level);

    /**
     * Get the level matcher
     *
     * @return  level matcher
     */
    MatcherManager getLevelMatcher();
}
