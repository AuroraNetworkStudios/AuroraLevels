package gg.auroramc.levels.api.reward;

import gg.auroramc.aurora.api.message.Placeholder;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.List;

public interface LevelReward {
    /**
     * Will be called when the player reaches the appropriate level.
     *
     * @param player                player who reached the level
     * @param formulaPlaceholders   reward formulas
     */
    void execute(Player player, long level, List<Placeholder<?>> formulaPlaceholders);

    /**
     * Initialize the reward with the given configuration.
     *
     * @param args  configuration to initialize the reward with
     */
    void init(ConfigurationSection args);

    /**
     * Get the display of the reward.
     * This should be always read from the args configuration.
     *
     * @param player                player to get the display for
     * @param formulaPlaceholders   reward formulas
     * @return  display of the reward
     */
    String getDisplay(Player player, List<Placeholder<?>> formulaPlaceholders);
}
