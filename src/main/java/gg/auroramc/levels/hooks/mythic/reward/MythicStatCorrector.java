package gg.auroramc.levels.hooks.mythic.reward;

import com.google.common.collect.Maps;
import gg.auroramc.aurora.api.reward.RewardCorrector;
import gg.auroramc.levels.AuroraLevels;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.core.skills.stats.StatModifierType;
import io.lumine.mythic.core.skills.stats.StatType;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class MythicStatCorrector implements RewardCorrector {
    private final AuroraLevels plugin;

    public MythicStatCorrector(AuroraLevels plugin) {
        this.plugin = plugin;
    }

    @Override
    public void correctRewards(Player player) {
        CompletableFuture.runAsync(() -> {
            var leveler = plugin.getLeveler();
            var data = leveler.getUserData(player);
            var level = data.getLevel();
            var mythic = MythicBukkit.inst();
            var registry = mythic.getPlayerManager().getProfile(player).getStatRegistry();

            Map<StatType, Map<StatModifierType, Double>> statMap = Maps.newHashMap();

            mythic.getStatManager().getStats().values()
                    .forEach(statType -> {
                        if (!statType.isEnabled()) return;
                        registry.removeValue(statType, MythicStatReward.getSource());
                    });

            // Gather new stat modifiers
            for (int i = 1; i < level + 1; i++) {
                var matcher = leveler.getLevelMatcher().getBestMatcher(i);
                if (matcher == null) continue;
                var formulaPlaceholders = leveler.getRewardFormulaPlaceholders(player, i);
                for (var reward : matcher.computeRewards(i)) {
                    if (reward instanceof MythicStatReward statReward && statReward.isValid()) {
                        statMap.computeIfAbsent(statReward.getStatType(), (key) -> Maps.newHashMap())
                                .merge(statReward.getModifierType(), statReward.getValue(formulaPlaceholders), Double::sum);
                    }
                }
            }

            // Apply the new stat modifiers
            for (var entry : statMap.entrySet()) {
                var statType = entry.getKey();
                for (var modifierEntry : entry.getValue().entrySet()) {
                    var modifierType = modifierEntry.getKey();
                    var value = modifierEntry.getValue();
                    AuroraLevels.logger().debug("Adding stat " + statType.getKey() + " with value " + value + " to player " + player.getName());
                    registry.putValue(statType, MythicStatReward.getSource(), modifierType, value);
                }
            }
        });
    }
}
