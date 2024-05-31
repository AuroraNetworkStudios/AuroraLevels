package gg.auroramc.levels.hooks.mythic.mechanics;

import gg.auroramc.levels.AuroraLevels;
import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.skills.ITargetedEntitySkill;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.SkillResult;
import io.lumine.mythic.api.skills.placeholders.PlaceholderInt;
import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.bukkit.events.MythicMechanicLoadEvent;
import io.lumine.mythic.core.utils.annotations.MythicMechanic;
import org.bukkit.entity.Player;

@MythicMechanic(
        author = "erik_sz",
        name = "addAuroraLevel",
        description = "Give a player a certain amount of AuroraLevels XP",
        aliases = {"addAuroraLvl"}
)
public class AddAuroraLevel implements ITargetedEntitySkill {
    private final AuroraLevels plugin;
    private final PlaceholderInt level;

    public AddAuroraLevel(AuroraLevels plugin, MythicMechanicLoadEvent loader) {
        this.plugin = plugin;
        this.level = loader.getConfig().getPlaceholderInteger(new String[]{"level", "lvl", "l"}, 0);
    }

    @Override
    public SkillResult castAtEntity(SkillMetadata skillMetadata, AbstractEntity target) {
        if (!target.isPlayer()) return SkillResult.INVALID_TARGET;
        Player player = BukkitAdapter.adapt(target.asPlayer());

        var data = plugin.getLeveler().getUserData(player);

        var currentLevel = data.getLevel();
        var newLevel = currentLevel + level.get(skillMetadata);

        plugin.getLeveler().setPlayerLevel(player, newLevel);

        return SkillResult.SUCCESS;
    }
}
