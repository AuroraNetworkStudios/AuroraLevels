package gg.auroramc.levels.reward.corrector;

import gg.auroramc.aurora.api.reward.CommandReward;
import gg.auroramc.aurora.api.reward.RewardCorrector;
import gg.auroramc.levels.AuroraLevels;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class CommandCorrector implements RewardCorrector {

    private final AuroraLevels plugin;

    public CommandCorrector(AuroraLevels plugin) {
        this.plugin = plugin;
    }

    @Override
    public void correctRewards(Player player) {
        CompletableFuture.runAsync(() -> {
            var leveler = plugin.getLeveler();
            var data = leveler.getUserData(player);
            var level = data.getLevel();

            final var rewards = new ArrayList<Map.Entry<Integer, CommandReward>>();

            for (int i = 1; i < level + 1; i++) {
                var matcher = leveler.getLevelMatcher().getBestMatcher(i);
                if (matcher == null) continue;

                for (var reward : matcher.computeRewards(i)) {
                    if (reward instanceof CommandReward commandReward) {
                        if (commandReward.shouldBeCorrected(player, i)) {
                            rewards.add(Map.entry(i, commandReward));
                        }
                    }
                }
            }

            if (rewards.isEmpty()) return;

            Bukkit.getGlobalRegionScheduler().run(plugin, (task) -> {
                for (var entry : rewards) {
                    if (!player.isOnline()) break;
                    var lvl = entry.getKey();
                    entry.getValue().execute(player, lvl, leveler.getRewardFormulaPlaceholders(player, lvl));
                }
                AuroraLevels.logger().debug("Corrected %d command rewards for player %s".formatted(rewards.size(), player.getName()));
            });
        });
    }
}
