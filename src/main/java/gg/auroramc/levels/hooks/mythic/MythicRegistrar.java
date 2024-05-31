package gg.auroramc.levels.hooks.mythic;

import gg.auroramc.levels.AuroraLevels;
import gg.auroramc.levels.hooks.mythic.conditions.HasAuroraLevelCondition;
import gg.auroramc.levels.hooks.mythic.mechanics.AddAuroraLevel;
import gg.auroramc.levels.hooks.mythic.mechanics.GiveAuroraLevelsXP;
import io.lumine.mythic.api.skills.ISkillMechanic;
import io.lumine.mythic.api.skills.conditions.ISkillCondition;
import io.lumine.mythic.bukkit.events.MythicConditionLoadEvent;
import io.lumine.mythic.bukkit.events.MythicMechanicLoadEvent;
import io.lumine.mythic.core.utils.annotations.MythicCondition;
import io.lumine.mythic.core.utils.annotations.MythicMechanic;
import lombok.SneakyThrows;

public class MythicRegistrar {
    private final AuroraLevels plugin;

    public MythicRegistrar(AuroraLevels plugin) {
        this.plugin = plugin;
    }

    public void registerApplicableCondition(MythicConditionLoadEvent event) {
        registerCondition(event, HasAuroraLevelCondition.class);
    }

    public void registerApplicableMechanic(MythicMechanicLoadEvent event) {
        registerMechanic(event, GiveAuroraLevelsXP.class);
        registerMechanic(event, AddAuroraLevel.class);
    }

    @SneakyThrows
    private void registerCondition(MythicConditionLoadEvent event, Class<? extends ISkillCondition> conditionClass) {
        var annotation = conditionClass.getAnnotation(MythicCondition.class);

        if (event.getConditionName().equalsIgnoreCase(annotation.name())) {
            event.register(conditionClass.getConstructor(AuroraLevels.class, MythicConditionLoadEvent.class)
                    .newInstance(plugin, event));
            return;
        }

        for (var alias : annotation.aliases()) {
            if (event.getConditionName().equalsIgnoreCase(alias)) {
                event.register(conditionClass.getConstructor(AuroraLevels.class, MythicConditionLoadEvent.class)
                        .newInstance(plugin, event));
                return;
            }
        }
    }

    @SneakyThrows
    private void registerMechanic(MythicMechanicLoadEvent event, Class<? extends ISkillMechanic> mechanicClass) {
        var annotation = mechanicClass.getAnnotation(MythicMechanic.class);

        if (event.getMechanicName().equalsIgnoreCase(annotation.name())) {
            event.register(mechanicClass.getConstructor(AuroraLevels.class, MythicMechanicLoadEvent.class)
                    .newInstance(plugin, event));
            return;
        }

        for (var alias : annotation.aliases()) {
            if (event.getMechanicName().equalsIgnoreCase(alias)) {
                event.register(mechanicClass.getConstructor(AuroraLevels.class, MythicMechanicLoadEvent.class)
                        .newInstance(plugin, event));
                return;
            }
        }
    }
}
