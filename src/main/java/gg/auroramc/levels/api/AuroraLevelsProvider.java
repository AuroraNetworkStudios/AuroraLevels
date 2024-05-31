package gg.auroramc.levels.api;

import gg.auroramc.levels.AuroraLevels;
import gg.auroramc.levels.api.leveler.Leveler;

public class AuroraLevelsProvider {
    private static AuroraLevels plugin;

    private AuroraLevelsProvider() {
    }

    public static Leveler getLeveler() {
        return plugin.getLeveler();
    }
}
