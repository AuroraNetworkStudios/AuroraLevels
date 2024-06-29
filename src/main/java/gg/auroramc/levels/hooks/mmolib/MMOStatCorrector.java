package gg.auroramc.levels.hooks.mmolib;

import com.google.common.collect.Maps;
import gg.auroramc.aurora.api.reward.RewardCorrector;
import gg.auroramc.aurora.api.util.NamespacedId;
import gg.auroramc.levels.AuroraLevels;
import io.lumine.mythic.lib.api.player.EquipmentSlot;
import io.lumine.mythic.lib.api.player.MMOPlayerData;
import io.lumine.mythic.lib.api.stat.StatMap;
import io.lumine.mythic.lib.api.stat.modifier.StatModifier;
import io.lumine.mythic.lib.player.modifier.ModifierSource;
import io.lumine.mythic.lib.player.modifier.ModifierType;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class MMOStatCorrector implements RewardCorrector {
    private final AuroraLevels plugin;

    public MMOStatCorrector(AuroraLevels plugin) {
        this.plugin = plugin;
    }

    @Override
    public void correctRewards(Player player) {
        CompletableFuture.runAsync(() -> {
            var leveler = plugin.getLeveler();
            var data = leveler.getUserData(player);
            var level = data.getLevel();

            MMOPlayerData playerData = MMOPlayerData.get(player);
            StatMap stats = playerData.getStatMap();

            Map<String, MMOStat> statMap = Maps.newHashMap();

            // Gather new stat modifiers
            for (int i = 1; i < level + 1; i++) {
                var matcher = leveler.getLevelMatcher().getBestMatcher(i);
                if (matcher == null) continue;
                var formulaPlaceholders = leveler.getRewardFormulaPlaceholders(player, i);
                for (var reward : matcher.computeRewards(i)) {
                    if (reward instanceof MMOStatReward statReward && statReward.isValid()) {
                        var key = NamespacedId.of(MMOStatReward.getMMO_STAT(), statReward.getStat()).toString();
                        var current = statReward.getCurrentModifier(key, stats);
                        UUID uuid = current != null ? current.getUniqueId() : UUID.randomUUID();
                        statMap.merge(statReward.getStat(),
                                new MMOStat(statReward.getModifierType(), statReward.getValue(formulaPlaceholders), key, uuid),
                                (a, b) -> new MMOStat(statReward.getModifierType(), a.value() + b.value(), a.key(), a.uuid()));
                    }
                }
            }

            Bukkit.getGlobalRegionScheduler().runDelayed(plugin, (task) -> {
                for (var entry : statMap.entrySet()) {
                    var statType = entry.getKey();
                    var s = entry.getValue();

                    new StatModifier(s.uuid, s.key, statType, s.value, s.modifierType, EquipmentSlot.OTHER, ModifierSource.OTHER)
                            .register(playerData);
                }
            }, 3);
        });
    }

    public record MMOStat(ModifierType modifierType, double value, String key, UUID uuid) {
    }
}
