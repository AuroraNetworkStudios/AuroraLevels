package gg.auroramc.levels.hooks.ecoskills;

import com.google.common.collect.Maps;
import com.willfp.ecoskills.api.EcoSkillsAPI;
import com.willfp.ecoskills.api.modifiers.ModifierOperation;
import com.willfp.ecoskills.api.modifiers.StatModifier;
import com.willfp.ecoskills.stats.Stats;
import gg.auroramc.aurora.api.reward.RewardCorrector;
import gg.auroramc.levels.AuroraLevels;
import org.bukkit.entity.Player;

import java.util.Map;

public class EcoSkillsCorrector implements RewardCorrector {
    private final AuroraLevels plugin;

    public EcoSkillsCorrector(AuroraLevels plugin) {
        this.plugin = plugin;
    }

    @Override
    public void correctRewards(Player player) {
        var leveler = plugin.getLeveler();
        var data = leveler.getUserData(player);
        var level = data.getLevel();

        Map<String, Map<ModifierOperation, Double>> statMap = Maps.newHashMap();

        Stats.INSTANCE.values().forEach(stat -> {
            var map = statMap.computeIfAbsent(stat.getId(), (key) -> Maps.newHashMap());
            for (var operation : ModifierOperation.values()) {
                map.put(operation, 0.0);
            }
        });

        // Gather new stat modifiers
        for (int i = 1; i < level + 1; i++) {
            var matcher = leveler.getLevelMatcher().getBestMatcher(i);
            if (matcher == null) continue;
            var formulaPlaceholders = leveler.getRewardFormulaPlaceholders(player, i);
            for (var reward : matcher.computeRewards(i)) {
                if (reward instanceof EcoSkillsStatReward statReward && statReward.isValid()) {
                    statMap.computeIfAbsent(statReward.getStat().getId(), (key) -> Maps.newHashMap())
                            .merge(statReward.getOperation(), statReward.getValue(formulaPlaceholders), Double::sum);
                }
            }
        }

        // Apply the new stat modifiers
        player.getScheduler().run(plugin, (task) -> {
            for (var entry : statMap.entrySet()) {
                var statType = entry.getKey();
                var stat = Stats.INSTANCE.getByID(statType);
                if (stat == null) continue;

                for (var modifierEntry : entry.getValue().entrySet()) {
                    var operation = modifierEntry.getKey();
                    var value = modifierEntry.getValue();
                    var uuid = EcoSkillsStatReward.createStatModifierUUID(stat, operation);
                    if (value <= 0) {
                        EcoSkillsAPI.removeStatModifier(player, uuid);
                    } else {
                        AuroraLevels.logger().debug("Adding stat " + statType + " with value " + value + "/" + operation.name() + " to player " + player.getName());
                        EcoSkillsAPI.addStatModifier(player, new StatModifier(uuid, stat, value, operation));
                    }
                }
            }
        }, null);
    }
}
