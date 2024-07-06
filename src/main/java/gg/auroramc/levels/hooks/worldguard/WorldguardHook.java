package gg.auroramc.levels.hooks.worldguard;

import com.sk89q.worldguard.WorldGuard;
import gg.auroramc.levels.AuroraLevels;
import gg.auroramc.levels.hooks.Hook;

public class WorldguardHook implements Hook {
    @Override
    public void hook(AuroraLevels plugin) {
        EntryHandler.Factory handlerFactory = EntryHandler.FACTORY(plugin);
        WorldGuard.getInstance().getPlatform().getSessionManager().registerHandler(handlerFactory, null);
    }

    @Override
    public void hookAtStartUp(AuroraLevels plugin) {
        FlagManager.registerFlags();
    }
}
