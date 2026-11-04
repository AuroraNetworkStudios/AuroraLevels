package gg.auroramc.levels.hooks.mythic;

import gg.auroramc.aurora.api.plugin.Hook;
import gg.auroramc.aurora.api.reward.integration.MythicStatReward;
import gg.auroramc.aurora.api.util.NamespacedId;
import gg.auroramc.levels.AuroraLevels;
import gg.auroramc.levels.hooks.mythic.reward.AuroraStatSource;
import gg.auroramc.levels.hooks.mythic.reward.MythicStatCorrector;
import io.lumine.mythic.bukkit.events.MythicConditionLoadEvent;
import io.lumine.mythic.bukkit.events.MythicMechanicLoadEvent;
import io.lumine.mythic.core.skills.stats.StatSource;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class MythicHook implements Hook<AuroraLevels>, Listener {
    // Levels' stats in MythicMobs, kept apart from other plugins'
    public static final StatSource SOURCE = new AuroraStatSource();
    private MythicRegistrar registrar;

    @Override
    public void hook(AuroraLevels plugin) {
        this.registrar = new MythicRegistrar(plugin);

        plugin.getLeveler().getRewardFactory()
                .registerRewardType(NamespacedId.fromDefault("mythic_stat"), () -> new MythicStatReward(SOURCE, AuroraLevels.logger()));

        plugin.getLeveler().getRewardAutoCorrector()
                .registerCorrector(NamespacedId.fromDefault("mythic_stat"), new MythicStatCorrector(plugin));

        AuroraLevels.logger().info("Hooked into MythicMobs for custom mechanics (addAuroraLevel, giveAuroraXP), " +
                "conditions (hasAuroraLevel) and \"mythic_stat\" reward. " +
                "Auto reward corrector for stats is registered.");
    }

    @EventHandler
    public void onMechanicLoad(MythicMechanicLoadEvent event) {
        registrar.registerApplicableMechanic(event);
    }

    @EventHandler
    public void onConditionLoad(MythicConditionLoadEvent event) {
        registrar.registerApplicableCondition(event);
    }
}
