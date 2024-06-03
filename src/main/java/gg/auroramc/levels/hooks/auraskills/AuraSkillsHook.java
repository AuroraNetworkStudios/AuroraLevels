package gg.auroramc.levels.hooks.auraskills;

import gg.auroramc.aurora.api.util.NamespacedId;
import gg.auroramc.levels.AuroraLevels;
import gg.auroramc.levels.hooks.Hook;

public class AuraSkillsHook implements Hook {
    @Override
    public void hook(AuroraLevels plugin) {
        plugin.getLeveler().getLevelMatcher().registerRewardType(
                NamespacedId.fromDefault("auraskills_stat"), AuraSkillsStatReward.class);
        plugin.getLeveler().registerRewardCorrector("auraskills_stat", new AuraSkillsCorrector(plugin));
    }
}
