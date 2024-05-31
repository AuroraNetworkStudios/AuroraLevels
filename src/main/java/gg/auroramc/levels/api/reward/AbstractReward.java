package gg.auroramc.levels.api.reward;

import gg.auroramc.aurora.api.message.Placeholder;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.List;

public abstract class AbstractReward implements LevelReward {
    protected String display;

    @Override
    public void init(ConfigurationSection args) {
        display = args.getString("display", "");
    }

    @Override
    public String getDisplay(Player player, List<Placeholder<?>> formulaPlaceholders) {
        return display;
    }
}
