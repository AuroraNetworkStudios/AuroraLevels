package gg.auroramc.levels.hooks.mythic.mechanics;

import gg.auroramc.levels.AuroraLevels;
import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.skills.ITargetedEntitySkill;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.SkillResult;
import io.lumine.mythic.api.skills.ThreadSafetyLevel;
import io.lumine.mythic.api.skills.placeholders.PlaceholderDouble;
import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.bukkit.events.MythicMechanicLoadEvent;
import io.lumine.mythic.core.utils.annotations.MythicMechanic;
import org.bukkit.entity.Player;

@MythicMechanic(
        author = "erik_sz",
        name = "giveAuroraLevelsXP",
        description = "Give a player a certain amount of AuroraLevels XP",
        aliases = {"giveAuroraXP", "giveLevelXP"}
)
public class GiveAuroraLevelsXP implements ITargetedEntitySkill {
    private final AuroraLevels plugin;
    private final PlaceholderDouble xp;

    public GiveAuroraLevelsXP(AuroraLevels plugin, MythicMechanicLoadEvent loader) {
        this.plugin = plugin;
        this.xp = loader.getConfig().getPlaceholderDouble("xp", 0.0);
    }

    @Override
    public SkillResult castAtEntity(SkillMetadata data, AbstractEntity target) {
        if (!target.isPlayer()) return SkillResult.INVALID_TARGET;
        Player player = BukkitAdapter.adapt(target.asPlayer());

        plugin.getLeveler().addXpToPlayer(player, xp.get(data));

        return SkillResult.SUCCESS;
    }

    @Override
    public ThreadSafetyLevel getThreadSafetyLevel() {
        return ThreadSafetyLevel.SYNC_ONLY;
    }
}
