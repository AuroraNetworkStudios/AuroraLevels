package gg.auroramc.levels.hooks.mmolib;

import gg.auroramc.aurora.api.util.NamespacedId;
import gg.auroramc.levels.AuroraLevels;
import gg.auroramc.levels.hooks.Hook;

public class MMOLibHook implements Hook {
    @Override
    public void hook(AuroraLevels plugin) {
        plugin.getLeveler().getRewardFactory()
                .registerRewardType(NamespacedId.fromDefault("mmo_stat"), MMOStatReward.class);

        plugin.getLeveler().getRewardAutoCorrector()
                .registerCorrector(NamespacedId.fromDefault("mmo_stat"), new MMOStatCorrector(plugin));
    }
}
