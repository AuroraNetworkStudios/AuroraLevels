package gg.auroramc.levels.placeholder;

import gg.auroramc.aurora.api.placeholder.PlaceholderHandler;
import gg.auroramc.levels.api.leveler.Leveler;
import org.bukkit.entity.Player;

import java.util.List;

public class LevelPlaceholderHandler implements PlaceholderHandler {
    private final Leveler leveler;

    public LevelPlaceholderHandler(Leveler module) {
        this.leveler = module;
    }

    @Override
    public String getIdentifier() {
        return "level";
    }

    @Override
    public String onPlaceholderRequest(Player player, String[] args) {
        if (args.length > 0) {
            if (args[args.length - 1].equals("xp")) {
                return String.valueOf(((Double) leveler.getUserData(player).getCurrentXP()).longValue());
            } else if (args[args.length - 1].equals("xpnext")) {
                return String.valueOf(((Double) (leveler.getXpForLevel(leveler.getUserData(player).getLevel() + 1) - leveler.getXpForLevel(leveler.getUserData(player).getLevel()))).longValue());
            }
        }
        return String.valueOf(leveler.getUserData(player).getLevel());
    }

    @Override
    public List<String> getPatterns() {
        return List.of("", "xp", "xpnext");
    }
}
