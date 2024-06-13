package gg.auroramc.levels.hooks.luckperms;

import gg.auroramc.aurora.api.reward.PermissionReward;
import gg.auroramc.aurora.api.reward.RewardCorrector;
import gg.auroramc.levels.AuroraLevels;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.node.NodeEqualityPredicate;
import net.luckperms.api.util.Tristate;
import org.bukkit.entity.Player;

import java.util.concurrent.CompletableFuture;

public class PermissionCorrector implements RewardCorrector {
    private AuroraLevels plugin;

    public PermissionCorrector(AuroraLevels plugin) {
        this.plugin = plugin;
    }

    @Override
    public void correctRewards(Player player) {
        CompletableFuture.runAsync(() -> {
            var leveler = plugin.getLeveler();
            var data = leveler.getUserData(player);
            var level = data.getLevel();

            for (int i = 1; i < level + 1; i++) {
                var matcher = leveler.getLevelMatcher().getBestMatcher(i);
                if (matcher == null) continue;
                var formulaPlaceholders = leveler.getRewardFormulaPlaceholders(player, i);
                for (var reward : matcher.computeRewards(i)) {
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
