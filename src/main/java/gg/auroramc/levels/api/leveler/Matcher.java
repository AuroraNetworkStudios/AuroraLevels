package gg.auroramc.levels.api.leveler;

import gg.auroramc.levels.api.reward.LevelReward;

import java.util.List;

public record Matcher(int interval, int priority, List<LevelReward> rewards) {
    public boolean matches(long level) {
        return level % interval == 0;
    }
}
