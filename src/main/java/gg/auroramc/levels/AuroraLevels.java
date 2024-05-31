package gg.auroramc.levels;

import gg.auroramc.aurora.api.AuroraAPI;
import gg.auroramc.aurora.api.AuroraLogger;
import gg.auroramc.levels.api.AuroraLevelsProvider;
import gg.auroramc.levels.api.leveler.Leveler;
import gg.auroramc.levels.command.CommandManager;
import gg.auroramc.levels.config.ConfigManager;
import gg.auroramc.levels.api.data.LevelData;
import gg.auroramc.levels.hooks.HookManager;
import gg.auroramc.levels.leveler.PlayerLeveler;
import gg.auroramc.levels.placeholder.LevelPlaceholderHandler;
import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;

public class AuroraLevels extends JavaPlugin {
    @Getter
    private static AuroraLevels instance;
    @Getter
    private PlayerLeveler leveler;
    @Getter
    private ConfigManager configManager;

    private CommandManager commandManager;

    private static AuroraLogger l;

    public static AuroraLogger logger() {
        return l;
    }

    @Override
    public void onLoad() {
        instance = this;
    }

    @Override
    public void onEnable() {
        configManager = new ConfigManager();

        l = AuroraAPI.createLogger("AuroraLevels", () -> configManager.getLevelConfig().getDebug());
        AuroraAPI.getUserManager().registerUserDataHolder(LevelData.class);

        leveler = new PlayerLeveler(this);
        AuroraAPI.registerPlaceholderHandler(new LevelPlaceholderHandler(leveler));

        commandManager = new CommandManager();
        commandManager.reload();

        try {
            var field = AuroraLevelsProvider.class.getDeclaredField("plugin");
            field.setAccessible(true);
            field.set(null, this);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            l.severe("Failed to initialize api provider!");
            e.printStackTrace();
        }

        HookManager.registerHooks();
    }

    public void reload() {
        configManager.reload();
        commandManager.reload();
        leveler.reload();
    }

    @Override
    public void onDisable() {
        commandManager.unregisterCommands();
    }
}