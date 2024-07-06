package gg.auroramc.levels.placeholder;

import gg.auroramc.aurora.api.message.Text;
import gg.auroramc.aurora.api.placeholder.PlaceholderHandler;
import gg.auroramc.levels.AuroraLevels;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.Player;

import java.util.List;

public class LevelPlaceholderHandler implements PlaceholderHandler {
    private final AuroraLevels plugin;
    private final LegacyComponentSerializer serializer = LegacyComponentSerializer.builder().useUnusualXRepeatedCharacterHexFormat().build();

    public LevelPlaceholderHandler(AuroraLevels plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getIdentifier() {
        return "level";
    }

    @Override
    public String onPlaceholderRequest(Player player, String[] args) {
        var leveler = plugin.getLeveler();

        if (args.length > 0) {
            if (args[args.length - 1].equals("xp")) {
                return String.valueOf(((Double) leveler.getUserData(player).getCurrentXP()).longValue());
            } else if (args[args.length - 1].equals("xpnext")) {
                return String.valueOf(((Double) (leveler.getXpForLevel(leveler.getUserData(player).getLevel() + 1) - leveler.getXpForLevel(leveler.getUserData(player).getLevel()))).longValue());
            } else if (args[args.length - 1].equals("progressbar")) {
                var currentXP = leveler.getUserData(player).getCurrentXP();
                var requiredXP = leveler.getRequiredXpForLevelUp(player);
                var menuConfig = plugin.getConfigManager().getLevelMenuConfig();
                var bar = menuConfig.getProgressBar();
                var pcs = bar.getLength();
                var completedPercent = currentXP / requiredXP;
                var completedPcs = ((Double) Math.floor(pcs * completedPercent)).intValue();
                var remainingPcs = pcs - completedPcs;
                var rawBar = bar.getFilledCharacter().repeat(completedPcs) + bar.getUnfilledCharacter().repeat(remainingPcs) + "&r";
                return serializer.serialize(Text.component(rawBar));
            }
        }
        return String.valueOf(leveler.getUserData(player).getLevel());
    }

    @Override
    public List<String> getPatterns() {
        return List.of("", "xp", "xpnext", "progressbar");
    }
}
