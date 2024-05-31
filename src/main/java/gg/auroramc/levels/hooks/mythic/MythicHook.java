package gg.auroramc.levels.hooks.mythic;

import gg.auroramc.levels.AuroraLevels;
import gg.auroramc.levels.hooks.Hook;
import io.lumine.mythic.bukkit.events.MythicConditionLoadEvent;
import io.lumine.mythic.bukkit.events.MythicMechanicLoadEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class MythicHook implements Hook, Listener {
    private MythicRegistrar registrar;

    @Override
    public void hook(AuroraLevels plugin) {
        this.registrar = new MythicRegistrar(plugin);
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
