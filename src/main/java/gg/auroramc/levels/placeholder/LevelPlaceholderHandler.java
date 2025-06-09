package gg.auroramc.levels.placeholder;

import gg.auroramc.aurora.api.AuroraAPI;
import gg.auroramc.aurora.api.message.Text;
import gg.auroramc.aurora.api.placeholder.PlaceholderHandler;
import gg.auroramc.levels.AuroraLevels;
import gg.auroramc.levels.util.RomanNumber;
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
            switch (args[0]) {
                case "xp" -> {
                    var xp = leveler.getUserData(player).getCurrentXP();
                    return getFormattedXP(args, xp);
                }
                case "xpnext" -> {
                    var xp = leveler.getRequiredXpForLevelUp(player);
                    return getFormattedXP(args, xp);
                }
                case "progressbar" -> {
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
                case "icon" -> {
                    String level = String.valueOf(leveler.getUserData(player).getLevel());
                    StringBuilder placeholder = new StringBuilder(plugin.getConfigManager().getLevelConfig().getIconGenerator().getOrDefault(level, ""));

                    if (placeholder.isEmpty()) {
                        for (String c : level.split("")) {
                            String charIcon = plugin.getConfigManager().getLevelConfig().getIconGenerator().get(c);
                            if (charIcon == null) {
                                return null;
                            } else {
                                placeholder.append(charIcon);
                            }
                        }
                    }

                    return serializer.serialize(Text.component(player, placeholder.toString()));
                }
                case "roman" -> {
                    return RomanNumber.toRoman(leveler.getUserData(player).getLevel());
                }
            }
        }
        return String.valueOf(leveler.getUserData(player).getLevel());
    }

    private String getFormattedXP(String[] args, double xp) {
        if (args.length > 1 && args[1].equals("formatted")) {
            return AuroraAPI.formatNumber(xp);
        }
        if (args.length > 1 && args[1].equals("short")) {
            return AuroraAPI.formatNumberShort(xp);
        }
        return String.valueOf(xp);
    }

    @Override
    public List<String> getPatterns() {
        return List.of("", "xp", "xp_formatted", "xp_short", "xpnext", "xpnext_formatted", "xpnext_short", "progressbar", "icon", "roman");
    }
}
