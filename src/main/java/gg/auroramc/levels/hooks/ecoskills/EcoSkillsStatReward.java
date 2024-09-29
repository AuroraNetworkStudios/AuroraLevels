package gg.auroramc.levels.hooks.ecoskills;

import com.willfp.ecoskills.api.EcoSkillsAPI;
import com.willfp.ecoskills.api.modifiers.ModifierOperation;
import com.willfp.ecoskills.api.modifiers.StatModifier;
import com.willfp.ecoskills.stats.Stat;
import com.willfp.ecoskills.stats.Stats;
import gg.auroramc.aurora.api.message.Placeholder;
import gg.auroramc.aurora.api.reward.NumberReward;
import gg.auroramc.levels.AuroraLevels;
import lombok.Getter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Getter
public class EcoSkillsStatReward extends NumberReward {
    public static final String prefix = "aurora_levels/stat/";

    private boolean valid = true;
    private Stat stat;
    private ModifierOperation operation = ModifierOperation.ADD;

    private UUID createStatModifierUUID() {
        return createStatModifierUUID(stat, operation);
    }

    public static UUID createStatModifierUUID(Stat stat, ModifierOperation operation) {
        return UUID.nameUUIDFromBytes((prefix + stat.getId() + "/" + operation.name()).getBytes());
    }

    @Override
    public void execute(Player player, long level, List<Placeholder<?>> placeholders) {
        if (!valid) return;
        var uuid = createStatModifierUUID();
        var current = EcoSkillsAPI.getStatModifier(player, uuid);

        double value = getValue(placeholders) + (current != null ? current.getModifier() : 0);

        // Since the UUID is always the same this should overwrite the previous value
        EcoSkillsAPI.addStatModifier(player, new StatModifier(uuid, stat, value, operation));
    }

    @Override
    public void init(ConfigurationSection args) {
        super.init(args);
        var statName = args.getString("stat", "");
        stat = Stats.INSTANCE.getByID(statName);
        if (stat == null) {
            valid = false;
            AuroraLevels.logger().warning("Couldn't find EcoSkills stat: " + statName);
        }

        var operationName = args.getString("operation", "add").toUpperCase(Locale.ROOT);

        try {
            operation = ModifierOperation.valueOf(operationName);
        } catch (IllegalArgumentException e) {
            valid = false;
            AuroraLevels.logger().warning("Couldn't find EcoSkills operation: " + operationName);
        }
    }

    @Override
    public String getDisplay(Player player, List<Placeholder<?>> placeholders) {
        var display = super.getDisplay(player, placeholders);
        if (!valid) return display;
        return Placeholder.execute(display, Placeholder.of("{stat}", stat.getName()));
    }
}
