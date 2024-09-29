package gg.auroramc.levels.hooks.ecoskills;

import gg.auroramc.aurora.api.util.NamespacedId;
import gg.auroramc.levels.AuroraLevels;
import gg.auroramc.levels.hooks.Hook;

public class EcoSkillsHook implements Hook {
    @Override
    public void hook(AuroraLevels plugin) {
        plugin.getLeveler().getRewardFactory()
                .registerRewardType(NamespacedId.fromDefault("ecoskills_stat"), EcoSkillsStatReward.class);
        plugin.getLeveler().getRewardAutoCorrector()
                .registerCorrector(NamespacedId.fromDefault("ecoskills_stat"), new EcoSkillsCorrector(plugin));

        AuroraLevels.logger().info("Hooked into AuraSkills for stat rewards with reward type: 'ecoskills_stat'. Auto reward corrector for stats is registered.");
    }
}
