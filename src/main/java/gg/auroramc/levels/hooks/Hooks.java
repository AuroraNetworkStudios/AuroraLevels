package gg.auroramc.levels.hooks;

import gg.auroramc.levels.hooks.auraskills.AuraSkillsHook;
import gg.auroramc.levels.hooks.luckperms.LuckPermsHook;
import gg.auroramc.levels.hooks.mythic.MythicHook;
import lombok.Getter;

@Getter
public enum Hooks {
    AURA_SKILLS(AuraSkillsHook.class, "AuraSkills"),
    LUCK_PERMS(LuckPermsHook.class, "LuckPerms"),
    MYTHIC_MOBS(MythicHook.class, "MythicMobs");

    private final Class<? extends Hook> clazz;
    private final String plugin;

    Hooks(Class<? extends Hook> clazz, String plugin) {
        this.clazz = clazz;
        this.plugin = plugin;
    }
}
