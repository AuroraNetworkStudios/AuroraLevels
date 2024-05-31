package gg.auroramc.levels.hooks.luckperms;

import gg.auroramc.levels.AuroraLevels;
import gg.auroramc.levels.hooks.Hook;

public class LuckPermsHook implements Hook {
    @Override
    public void hook(AuroraLevels plugin) {
        plugin.getLeveler().registerRewardType("permission", PermissionReward.class);
        plugin.getLeveler().registerRewardCorrector("permission", new PermissionCorrector());
    }
}
