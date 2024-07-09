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
                        if (permissionReward.getPermissions() == null || permissionReward.getPermissions().isEmpty()) {
                            continue;
                        }

                        var nodes = permissionReward.buildNodes(player, formulaPlaceholders);

                        LuckPermsProvider.get().getUserManager().modifyUser(player.getUniqueId(), (user) -> {
                            for (var node : nodes) {
                                var hasPermission = user.data().contains(node, NodeEqualityPredicate.EXACT);

                                if (hasPermission.equals(Tristate.UNDEFINED)) {
                                    AuroraLevels.logger().debug("Permission " + node.getKey() + " is undefined for player " + player.getName());
                                    user.data().add(node);
                                }
                            }
                        });
                    }
                }


            }
        });
    }
}
