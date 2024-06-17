package gg.auroramc.levels.hooks.mythic.reward;

import gg.auroramc.aurora.api.message.Placeholder;
import gg.auroramc.aurora.api.reward.NumberReward;
import gg.auroramc.levels.AuroraLevels;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.core.skills.stats.*;
import lombok.Getter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.List;

@Getter
public class MythicStatReward extends NumberReward {
    @Getter
    private static final StatSource source = new AuroraStatSource();

    private StatType statType;
    private StatModifierType modifierType;
    private boolean valid = true;

    @Override
    public void execute(Player player, long level, List<Placeholder<?>> placeholders) {
        if (!valid) return;

        StatRegistry registry = MythicBukkit.inst().getPlayerManager().getProfile(player).getStatRegistry();

        if (registry.getStatData(statType).isPresent()) {

            StatRegistry.StatMap data = registry.getStatData(statType).get();
            double current = 0;

            switch (modifierType) {
                case ADDITIVE -> current = data.getAdditives().getOrDefault(source, 0.0);
                case ADDITIVE_MULTIPLIER -> current = data.getAdditiveMultipliers().getOrDefault(source, 0.0);
                case COMPOUND_MULTIPLIER -> current = data.getCompoundMultipliers().getOrDefault(source, 0.0);
                case SETTER -> current = data.getSetters().getOrDefault(source, 0.0);
            }

            registry.putValue(statType, source, modifierType, current + getValue(placeholders));
        } else {
            registry.putValue(statType, source, modifierType, getValue(placeholders));
        }
    }

    @Override
    public void init(ConfigurationSection args) {
        super.init(args);
        var stat = args.getString("stat");

        if (stat == null) {
            this.valid = false;
            AuroraLevels.logger().warning("Stat is not defined in MythicStatReward");
            return;
        }

        this.statType = MythicBukkit.inst().getStatManager().getStats().get(stat);

        if (statType == null) {
            this.valid = false;
            AuroraLevels.logger().warning("Invalid stat: " + stat + " in MythicStatReward");
            return;
        }

        if (!statType.isEnabled()) {
            this.valid = false;
            AuroraLevels.logger().warning("Stat: " + stat + " is not enabled in MythicMobs. Cannot create MythicStatReward.");
            return;
        }

        var modifier = args.getString("modifier", "additive");

        try {
            this.modifierType = StatModifierType.valueOf(modifier.toUpperCase());
        } catch (IllegalArgumentException e) {
            this.valid = false;
            AuroraLevels.logger().warning("Invalid modifier type: " + modifier + " in MythicStatReward");
        }
    }

    @Override
    public String getDisplay(Player player, List<Placeholder<?>> placeholders) {
        var display = super.getDisplay(player, placeholders);

        if (!valid) {
            return display;
        }

        var value = getValue(placeholders);

        return Placeholder.execute(display,
                Placeholder.of("{stat}", statType.getDisplay()),
                Placeholder.of("{stat_display}", statType.getFormattedValue(modifierType, value))
        );
    }
}
