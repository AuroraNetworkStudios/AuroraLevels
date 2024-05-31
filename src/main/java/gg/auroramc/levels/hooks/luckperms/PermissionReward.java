package gg.auroramc.levels.hooks.luckperms;

import gg.auroramc.aurora.api.message.Placeholder;
import gg.auroramc.aurora.api.message.Text;
import gg.auroramc.levels.AuroraLevels;
import gg.auroramc.levels.api.reward.AbstractReward;
import lombok.Getter;
import net.luckperms.api.context.MutableContextSet;
import net.luckperms.api.node.Node;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import net.luckperms.api.LuckPermsProvider;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PermissionReward extends AbstractReward {
    @Getter
    private String permission;
    private boolean value;
    private final Map<String, String> contexts = new HashMap<>();

    @Override
    public void execute(Player player, long level, List<Placeholder<?>> formulaPlaceholders) {
        if (permission == null) return;
        var node = buildNode(player, formulaPlaceholders);
        AuroraLevels.logger().debug("Adding permission " + node.getKey() + " to player " + player.getName());
        LuckPermsProvider.get().getUserManager().modifyUser(player.getUniqueId(), user -> user.data().add(node));
    }

    @Override
    public void init(ConfigurationSection args) {
        super.init(args);
        permission = args.getString("permission", null);
        value = args.getBoolean("value", true);
        display = args.getString("display", "");

        if (args.isConfigurationSection("contexts")) {
            ConfigurationSection contextSection = args.getConfigurationSection("contexts");
            for (String key : contextSection.getKeys(false)) {
                contexts.put(key, contextSection.getString(key));
            }
        }

        if (permission == null) {
            AuroraLevels.logger().warning("PermissionReward doesn't have the permission key");
        }
    }

    public Node buildNode(Player player, List<Placeholder<?>> formulaPlaceholders) {
        var builder = Node.builder(Text.fillPlaceholders(player, permission, formulaPlaceholders)).value(value);

        if (!contexts.isEmpty()) {
            var contextSet = MutableContextSet.create();

            for (var entry : contexts.entrySet())
                contextSet.add(entry.getKey(), Text.fillPlaceholders(player, entry.getValue(), formulaPlaceholders));

            builder.withContext(contextSet);
        }

        return builder.build();
    }
}
