package gg.auroramc.levels.hooks.mythic.conditions;

import gg.auroramc.levels.AuroraLevels;
import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.skills.SkillCaster;
import io.lumine.mythic.api.skills.conditions.ICasterCondition;
import io.lumine.mythic.api.skills.conditions.IEntityCondition;
import io.lumine.mythic.api.skills.placeholders.PlaceholderInt;
import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.bukkit.events.MythicConditionLoadEvent;
import io.lumine.mythic.core.utils.annotations.MythicCondition;
import org.bukkit.entity.Player;

@MythicCondition(
        author = "erik_sz",
        name = "hasAuroraLevel",
        description = "Check if the player has a certain AuroraLevels level",
        aliases = {"hasAuroraLvl"}
)
public class HasAuroraLevelCondition implements IEntityCondition, ICasterCondition {
    private final AuroraLevels plugin;
    private final PlaceholderInt level;

    public HasAuroraLevelCondition(AuroraLevels plugin, MythicConditionLoadEvent loader) {
        this.plugin = plugin;
        this.level = loader.getConfig().getPlaceholderInteger(new String[]{"level", "lvl", "l"}, 0);
    }

    @Override
    public boolean check(AbstractEntity entity) {
        return checkCondition(entity, level.get(entity));
    }

    @Override
    public boolean check(SkillCaster caster) {
        return checkCondition(caster.getEntity(), level.get(caster));
    }

    private boolean checkCondition(AbstractEntity entity, int level) {
        if (!entity.isPlayer()) return false;
        Player player = BukkitAdapter.adapt(entity.asPlayer());

        return plugin.getLeveler().getUserData(player).getLevel() >= level;
    }
}
