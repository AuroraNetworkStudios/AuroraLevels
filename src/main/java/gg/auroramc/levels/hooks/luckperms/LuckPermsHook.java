package gg.auroramc.levels.hooks.luckperms;

import gg.auroramc.aurora.api.reward.PermissionReward;
import gg.auroramc.aurora.api.util.NamespacedId;
import gg.auroramc.levels.AuroraLevels;
import gg.auroramc.levels.hooks.Hook;

public class LuckPermsHook implements Hook {
    @Override
    public void hook(AuroraLevels plugin) {
        plugin.getLeveler().getRewardFactory().registerRewardType(NamespacedId.fromDefault("permission"), PermissionReward.class);
        plugin.getLeveler().getRewardAutoCorrector().registerCorrector(NamespacedId.fromDefault("permission"), new PermissionCorrector(plugin));
    }
}
