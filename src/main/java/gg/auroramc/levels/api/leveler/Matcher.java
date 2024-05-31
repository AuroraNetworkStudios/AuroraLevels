package gg.auroramc.levels.api.leveler;

import gg.auroramc.levels.api.reward.LevelReward;
import lombok.Getter;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.List;

@Getter
public class Matcher {
    private final int interval;
    private final int priority;
    private final List<LevelReward> rewards;

    public Matcher(int interval, int priority, ConfigurationSection rawRewards, Leveler leveler) {
        this.interval = interval;
        this.priority = priority;
        this.rewards = new ArrayList<>();

        if (rawRewards == null) return;

        for (var key : rawRewards.getKeys(false)) {
            var parsed = leveler.createReward(rawRewards.getConfigurationSection(key));
            parsed.ifPresent(rewards::add);
        }
    }

    public boolean matches(long level) {
        return level % interval == 0;
    }
}
