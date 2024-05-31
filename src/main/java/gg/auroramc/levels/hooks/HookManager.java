package gg.auroramc.levels.hooks;

import gg.auroramc.levels.AuroraLevels;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;

public class HookManager {
        public static void registerHooks(AuroraLevels plugin) {
            for (var hook : Hooks.values()) {
                try {
                    if(Bukkit.getPluginManager().isPluginEnabled(hook.getPlugin())) {
                        var instance = hook.getClazz().getDeclaredConstructor().newInstance();
                        instance.hook(plugin);
                        if(instance instanceof Listener) {
                            Bukkit.getPluginManager().registerEvents((Listener) instance, plugin);
                        }
                    }
                } catch (Exception e) {
                    AuroraLevels.logger().warning("Failed to hook " + hook.getPlugin() + ": " + e.getMessage());
                }
            }
        }
}
