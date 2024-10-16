package gg.auroramc.levels.hooks.auraskills;

import gg.auroramc.aurora.api.util.NamespacedId;
import gg.auroramc.levels.AuroraLevels;
import gg.auroramc.levels.hooks.Hook;
import lombok.Getter;
import org.bukkit.Bukkit;

@Getter
public class AuraSkillsHook implements Hook {
    private AuraSkillsCorrector corrector;

    @Override
    public void hook(AuroraLevels plugin) {
        this.corrector = new AuraSkillsCorrector(plugin);

        Bukkit.getPluginManager().registerEvents(new AuraSkillsListener(this), plugin);

        plugin.getLeveler().getRewardFactory()
                .registerRewardType(NamespacedId.fromDefault("auraskills_stat"), AuraSkillsStatReward.class);
        plugin.getLeveler().getRewardFactory()
                .registerRewardType(NamespacedId.fromDefault("auraskills_xp"), AuraSkillsXpReward.class);
        plugin.getLeveler().getRewardAutoCorrector()
                .registerCorrector(NamespacedId.fromDefault("auraskills_stat"), this.corrector);

        AuroraLevels.logger().info("Hooked into AuraSkills for stat rewards with reward type: 'auraskills_stat' and 'auraskills_xp'." +
                " Auto reward corrector for stats is registered.");
    }
}
