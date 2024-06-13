package gg.auroramc.levels.reward.corrector;

import gg.auroramc.aurora.api.reward.CommandReward;
import gg.auroramc.aurora.api.reward.RewardCorrector;
import gg.auroramc.levels.AuroraLevels;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
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

            final var rewards = new HashMap<Integer, CommandReward>();

            for (int i = 1; i < level + 1; i++) {
                var matcher = leveler.getLevelMatcher().getBestMatcher(i);
                if (matcher == null) continue;

                for (var reward : matcher.computeRewards(i)) {
                    if (reward instanceof CommandReward commandReward) {
                        if (commandReward.shouldBeCorrected(player, i)) {
                            rewards.put(i, commandReward);
                        }
                    }
                }
            }

            if (rewards.isEmpty()) return;

            Bukkit.getGlobalRegionScheduler().run(plugin, (task) -> {
                rewards.forEach((lvl, reward) -> {
                    if (!player.isOnline()) return;
                    reward.execute(player, lvl, leveler.getRewardFormulaPlaceholders(player, lvl));
                });
                AuroraLevels.logger().debug("Corrected %d command rewards for player %s".formatted(rewards.size(), player.getName()));
            });
        });
    }
}
