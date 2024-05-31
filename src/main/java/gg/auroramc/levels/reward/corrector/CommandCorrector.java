package gg.auroramc.levels.reward.corrector;

import gg.auroramc.levels.AuroraLevels;
import gg.auroramc.levels.api.leveler.Leveler;
import gg.auroramc.levels.api.reward.RewardCorrector;
import gg.auroramc.levels.reward.CommandReward;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.concurrent.CompletableFuture;

public class CommandCorrector implements RewardCorrector {

    @Override
    public void correctRewards(Leveler leveler, Player player) {
        CompletableFuture.runAsync(() -> {
            var data = leveler.getUserData(player);
            var level = data.getLevel();

            final var rewards = new HashMap<Long, CommandReward>();

            for (long i = 1; i < level + 1; i++) {
                var matcher = leveler.getLevelMatcher().getBestMatcher(i);
                if (matcher == null) continue;

                for (var reward : matcher.getRewards()) {
                    if (reward instanceof CommandReward commandReward) {
                        if (commandReward.shouldBeCorrected(player, i)) {
                            rewards.put(i, commandReward);
                        }
                    }
                }
            }

            if (rewards.isEmpty()) return;

            Bukkit.getGlobalRegionScheduler().run(AuroraLevels.getInstance(), (task) -> {
                rewards.forEach((lvl, reward) -> {
                    if (!player.isOnline()) return;
                    reward.execute(player, lvl, leveler.getRewardFormulaPlaceholders(player, lvl));
                });
                AuroraLevels.logger().debug("Corrected %d command rewards for player %s".formatted(rewards.size(), player.getName()));
            });
        });
    }
}
