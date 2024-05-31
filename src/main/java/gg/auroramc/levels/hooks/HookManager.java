package gg.auroramc.levels.hooks;

import gg.auroramc.levels.AuroraLevels;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;

public class HookManager {
        public static void registerHooks() {
            for (var hook : Hooks.values()) {
                try {
                    if(Bukkit.getPluginManager().isPluginEnabled(hook.getPlugin())) {
                        var instance = hook.getClazz().getDeclaredConstructor().newInstance();
                        instance.hook(AuroraLevels.getInstance());
                        if(instance instanceof Listener) {
                            Bukkit.getPluginManager().registerEvents((Listener) instance, AuroraLevels.getInstance());
                        }
                    }
                } catch (Exception e) {
                    AuroraLevels.logger().warning("Failed to hook " + hook.getPlugin() + ": " + e.getMessage());
                }
            }
        }
}
