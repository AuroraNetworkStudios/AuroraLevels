package gg.auroramc.levels.hooks.mmolib;

import gg.auroramc.aurora.api.message.Placeholder;
import gg.auroramc.aurora.api.reward.NumberReward;
import gg.auroramc.aurora.api.util.NamespacedId;
import gg.auroramc.levels.AuroraLevels;
import io.lumine.mythic.lib.api.player.MMOPlayerData;
import io.lumine.mythic.lib.api.stat.modifier.StatModifier;
import io.lumine.mythic.lib.player.modifier.ModifierType;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import io.lumine.mythic.lib.api.stat.StatMap;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@Getter
public class MMOStatReward extends NumberReward {
    @Getter
    private static final String MMO_STAT = "aurora_levels";
    private String stat;
    private ModifierType modifierType;
    private boolean valid = true;

    @Override
    public void execute(Player player, long level, List<Placeholder<?>> placeholders) {
        if (!valid) return;

        MMOPlayerData playerData = MMOPlayerData.get(player);
        StatMap statMap = playerData.getStatMap();
        String key = NamespacedId.of(MMO_STAT, stat).toString();
        StatModifier currentModifier = getCurrentModifier(key, statMap);

        double value = getValue(placeholders);

        if (currentModifier != null) {
            value += currentModifier.getValue();
            currentModifier.unregister(playerData);
        }

        StatModifier modifier = new StatModifier(key, stat, value, modifierType);

        modifier.register(playerData);

        Bukkit.getGlobalRegionScheduler().runDelayed(Bukkit.getPluginManager().getPlugin("AuroraLevels"), (task) -> {
            statMap.getInstance(stat).update();
        }, 3);
    }

    @Override
    public void init(ConfigurationSection args) {
        super.init(args);

        this.stat = args.getString("stat");

        if (this.stat == null) {
            this.valid = false;
            AuroraLevels.logger().warning("Stat is not defined in MMOStatReward");
        }

        this.modifierType = ModifierType.valueOf(args.getString("modifier", "FLAT"));
    }

    @Nullable
    public StatModifier getCurrentModifier(String key, StatMap statMap) {
        for (StatModifier stat : statMap.getInstance(stat).getModifiers()) {
            if (stat.getKey().equals(key)) {
                return stat;
            }
        }
        return null;
    }
}
