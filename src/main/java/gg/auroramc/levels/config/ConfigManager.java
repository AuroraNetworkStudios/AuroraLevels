package gg.auroramc.levels.config;

import gg.auroramc.levels.config.menu.LevelMenuConfig;
import gg.auroramc.levels.config.menu.MilestoneMenuConfig;
import lombok.Getter;

@Getter
public class ConfigManager {
    private LevelConfig levelConfig;
    private MessageConfig messageConfig;
    private LevelMenuConfig levelMenuConfig;
    private MilestoneMenuConfig milestoneMenuConfig;

    public ConfigManager() {
        reload();
    }

    public void reload() {
        LevelConfig.saveDefault();
        levelConfig = new LevelConfig();
        levelConfig.load();

        MessageConfig.saveDefault(levelConfig.getLanguage());
        messageConfig = new MessageConfig(levelConfig.getLanguage());
        messageConfig.load();

        LevelMenuConfig.saveDefault();
        levelMenuConfig = new LevelMenuConfig();
        levelMenuConfig.load();

        MilestoneMenuConfig.saveDefault();
        milestoneMenuConfig = new MilestoneMenuConfig();
        milestoneMenuConfig.load();

        // Prevent showing rewards for level 0
        levelConfig.getCustomLevels().put(0L, new LevelConfig.CustomLevel());
    }
}
