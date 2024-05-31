package gg.auroramc.levels.hooks.luckperms;

import gg.auroramc.levels.AuroraLevels;
import gg.auroramc.levels.api.leveler.Leveler;
import gg.auroramc.levels.api.reward.RewardCorrector;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.node.NodeEqualityPredicate;
import net.luckperms.api.util.Tristate;
import org.bukkit.entity.Player;

import java.util.concurrent.CompletableFuture;

public class PermissionCorrector implements RewardCorrector {

    @Override
    public void correctRewards(Leveler leveler, Player player) {
        CompletableFuture.runAsync(() -> {
            var data = leveler.getUserData(player);
            var level = data.getLevel();

            for (long i = 1; i < level + 1; i++) {
                var matcher = leveler.getLevelMatcher().getBestMatcher(i);
                if (matcher == null) continue;
                var formulaPlaceholders = leveler.getRewardFormulaPlaceholders(player, i);
                for (var reward : matcher.getRewards()) {
                    if (reward instanceof PermissionReward permissionReward) {
                        if (permissionReward.getPermission() == null) continue;
                        var node = permissionReward.buildNode(player, formulaPlaceholders);
                        var hasPermission = LuckPermsProvider.get().getUserManager().getUser(player.getUniqueId())
                                .data().contains(node, NodeEqualityPredicate.EXACT);

                        if (hasPermission.equals(Tristate.UNDEFINED)) {
                            AuroraLevels.logger().debug("Permission " + node.getKey() + " is undefined for player " + player.getName());
                            permissionReward.execute(player, i, formulaPlaceholders);
                        }
                    }
                }
            }
        });
    }
}
