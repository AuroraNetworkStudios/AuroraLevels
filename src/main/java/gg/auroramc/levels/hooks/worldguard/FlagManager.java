package gg.auroramc.levels.hooks.worldguard;

import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.IntegerFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagConflictException;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import gg.auroramc.levels.AuroraLevels;

public class FlagManager {
    public static final IntegerFlag MIN_LEVEL_FLAG = new IntegerFlag("aurora-levels-min-level-entry");
    public static final IntegerFlag MAX_LEVEL_FLAG = new IntegerFlag("aurora-levels-max-level-entry");

    public static void registerFlags() {
        var registry = WorldGuard.getInstance().getFlagRegistry();
        registerFlag(registry, MIN_LEVEL_FLAG);
        registerFlag(registry, MAX_LEVEL_FLAG);
    }

    private static void registerFlag(FlagRegistry registry, IntegerFlag flag) {
        try {
            registry.register(flag);
            AuroraLevels.logger().debug("Registered WorldGuard flag " + flag.getName());
        } catch (FlagConflictException e) {
            AuroraLevels.logger().warning("Could not register WorldGuard flag " + flag.getName());
        }
    }
}
