package gg.auroramc.levels.hooks.auraskills;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import dev.aurelium.auraskills.api.AuraSkillsApi;
import dev.aurelium.auraskills.api.stat.Stat;
import dev.aurelium.auraskills.api.stat.StatModifier;
import gg.auroramc.aurora.api.reward.RewardCorrector;
import gg.auroramc.levels.AuroraLevels;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class AuraSkillsCorrector implements RewardCorrector {
    private final AuroraLevels plugin;
    private final Set<UUID> toLoad = Sets.newConcurrentHashSet();

    public AuraSkillsCorrector(AuroraLevels plugin) {
        this.plugin = plugin;
    }

    @Override
    public void correctRewards(Player player) {
        if (AuraSkillsApi.get().getUser(player.getUniqueId()).isLoaded()) {
            AuroraLevels.logger().debug("User is already loaded by AuraSkills applying stat correctors immediately.");
            correctRewardsWhenLoaded(player, true);
        } else {
            toLoad.add(player.getUniqueId());
        }
    }

    public void correctRewardsWhenLoaded(Player player, boolean force) {
        if (!force && !toLoad.contains(player.getUniqueId())) return;
        toLoad.remove(player.getUniqueId());

        AuroraLevels.logger().debug("Correcting rewards for player: " + player.getName() + " was forced: " + force);

        var leveler = plugin.getLeveler();
        var data = leveler.getUserData(player);
        var level = data.getLevel();

        Map<Stat, Double> statMap = Maps.newHashMap();

        // Reset all stat modifiers first
        for (var stat : AuraSkillsApi.get().getGlobalRegistry().getStats()) {
            statMap.put(stat, 0.0);
        }

        // Gather new stat modifiers
        for (int i = 1; i < level + 1; i++) {
            var matcher = leveler.getLevelMatcher().getBestMatcher(i);
            if (matcher == null) continue;
            var formulaPlaceholders = leveler.getRewardFormulaPlaceholders(player, i);
            for (var reward : matcher.computeRewards(i)) {
                if (reward instanceof AuraSkillsStatReward statReward) {
                    statMap.merge(statReward.getStat(), statReward.getValue(formulaPlaceholders), Double::sum);
                }
            }
        }

        if (!player.isOnline()) return;

        player.getScheduler().run(plugin, (task) -> {
            // Apply the new stat modifiers
            for (var entry : statMap.entrySet()) {
                var statKey = AuraSkillsStatReward.getAURA_SKILLS_STAT() + entry.getKey().getId().toString();
                var user = AuraSkillsApi.get().getUser(player.getUniqueId());

                var oldModifier = user.getStatModifier(statKey);

                if (oldModifier == null) {
                    if (entry.getValue() > 0) {
                        user.addStatModifier(new StatModifier(statKey, entry.getKey(), entry.getValue()));
                    }
                } else if (entry.getValue() <= 0) {
                    user.removeStatModifier(statKey);
                } else if (entry.getValue() != oldModifier.value()) {
                    user.addStatModifier(new StatModifier(statKey, entry.getKey(), entry.getValue()));
                }
            }
        }, null);
    }
}
