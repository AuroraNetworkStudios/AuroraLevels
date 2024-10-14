package gg.auroramc.levels;

import gg.auroramc.aurora.api.AuroraAPI;
import gg.auroramc.aurora.api.AuroraLogger;
import gg.auroramc.levels.api.AuroraLevelsProvider;
import gg.auroramc.levels.command.CommandManager;
import gg.auroramc.levels.config.ConfigManager;
import gg.auroramc.levels.api.data.LevelData;
import gg.auroramc.levels.hooks.HookManager;
import gg.auroramc.levels.leveler.PlayerLeveler;
import gg.auroramc.levels.placeholder.LevelPlaceholderHandler;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.CompletableFuture;

public class AuroraLevels extends JavaPlugin {
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
        configManager = new ConfigManager(this);
        l = AuroraAPI.createLogger("AuroraLevels", () -> configManager.getLevelConfig().getDebug());
        HookManager.loadHooks(this);

        AuroraAPI.getLeaderboards().registerBoard(
                "levels",
                (user) -> leveler.getXpForLevel(user.getData(LevelData.class).getLevel()) + user.getData(LevelData.class).getCurrentXP(),
                (entry) -> AuroraAPI.formatNumber(leveler.getLevelFromXP(entry.getValue())),
                configManager.getLevelConfig().getLeaderboardCacheSize(),
                1
        );
    }

    @Override
    public void onEnable() {
        AuroraAPI.getUserManager().registerUserDataHolder(LevelData.class);

        leveler = new PlayerLeveler(this);
        AuroraAPI.registerPlaceholderHandler(new LevelPlaceholderHandler(this));

        commandManager = new CommandManager(this);
        commandManager.reload();

        try {
            var field = AuroraLevelsProvider.class.getDeclaredField("plugin");
            field.setAccessible(true);
            field.set(null, this);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            l.severe("Failed to initialize api provider!");
            e.printStackTrace();
        }

        HookManager.enableHooks(this);

        Bukkit.getGlobalRegionScheduler().run(this, (task) -> {
            var config = configManager.getLevelConfig();
            leveler.getLevelMatcher().reload(config.getLevelMatchers(), config.getCustomLevels());
        });
    }

    public void reload() {
        configManager.reload();
        commandManager.reload();
        leveler.reload(false);

        Bukkit.getOnlinePlayers().forEach(player ->
                CompletableFuture.runAsync(() -> {
                    leveler.correctCurrentXP(player);
                    leveler.getRewardAutoCorrector().correctRewards(player);
                }));
    }

    @Override
    public void onDisable() {
        commandManager.unregisterCommands();
    }
}