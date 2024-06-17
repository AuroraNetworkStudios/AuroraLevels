package gg.auroramc.levels.hooks.mythic;

import gg.auroramc.aurora.api.util.NamespacedId;
import gg.auroramc.levels.AuroraLevels;
import gg.auroramc.levels.hooks.Hook;
import gg.auroramc.levels.hooks.mythic.reward.MythicStatCorrector;
import gg.auroramc.levels.hooks.mythic.reward.MythicStatReward;
import io.lumine.mythic.bukkit.events.MythicConditionLoadEvent;
import io.lumine.mythic.bukkit.events.MythicMechanicLoadEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class MythicHook implements Hook, Listener {
    private MythicRegistrar registrar;

    @Override
    public void hook(AuroraLevels plugin) {
        this.registrar = new MythicRegistrar(plugin);

        plugin.getLeveler().getRewardFactory()
                .registerRewardType(NamespacedId.fromDefault("mythic_stat"), MythicStatReward.class);

        plugin.getLeveler().getRewardAutoCorrector()
                .registerCorrector(NamespacedId.fromDefault("mythic_stat"), new MythicStatCorrector(plugin));
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
