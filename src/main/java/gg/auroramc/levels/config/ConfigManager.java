package gg.auroramc.levels.config;

import gg.auroramc.levels.AuroraLevels;
import gg.auroramc.levels.config.menu.LevelMenuConfig;
import gg.auroramc.levels.config.menu.MilestoneMenuConfig;
import lombok.Getter;

@Getter
public class ConfigManager {
    private LevelConfig levelConfig;
    private MessageConfig messageConfig;
    private LevelMenuConfig levelMenuConfig;
    private MilestoneMenuConfig milestoneMenuConfig;
    private final AuroraLevels plugin;

    public ConfigManager(AuroraLevels plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        LevelConfig.saveDefault(plugin);
        levelConfig = new LevelConfig(plugin);
        levelConfig.load();

        MessageConfig.saveDefault(plugin, levelConfig.getLanguage());
        messageConfig = new MessageConfig(plugin, levelConfig.getLanguage());
        messageConfig.load();

        LevelMenuConfig.saveDefault(plugin);
        levelMenuConfig = new LevelMenuConfig(plugin);
        levelMenuConfig.load();

        MilestoneMenuConfig.saveDefault(plugin);
        milestoneMenuConfig = new MilestoneMenuConfig(plugin);
        milestoneMenuConfig.load();

        // Prevent showing rewards for level 0
        levelConfig.getCustomLevels().put(0L, new LevelConfig.CustomLevel());
    }
}
