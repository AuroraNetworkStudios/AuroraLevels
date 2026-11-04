package gg.auroramc.levels.hooks.luckperms;

import gg.auroramc.aurora.api.plugin.Hook;
import gg.auroramc.aurora.api.reward.PermissionReward;
import gg.auroramc.aurora.api.util.NamespacedId;
import gg.auroramc.levels.AuroraLevels;

public class LuckPermsHook implements Hook<AuroraLevels> {
    @Override
    public void hook(AuroraLevels plugin) {
        plugin.getLeveler().getRewardFactory().registerRewardType(NamespacedId.fromDefault("permission"), PermissionReward.class);
        plugin.getLeveler().getRewardAutoCorrector().registerCorrector(NamespacedId.fromDefault("permission"), new PermissionCorrector(plugin));

        AuroraLevels.logger().info("Hooked into LuckPerms for permission rewards with reward type: 'permission'. Auto reward corrector for permissions is registered.");
    }
}
