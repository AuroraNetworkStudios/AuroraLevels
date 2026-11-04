package gg.auroramc.levels.config;

import gg.auroramc.aurora.api.config.ConfigFile;
import gg.auroramc.levels.AuroraLevels;
import gg.auroramc.levels.config.menu.LevelMenuConfig;
import gg.auroramc.levels.config.menu.MilestoneMenuConfig;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ConfigManager {
    private final AuroraLevels plugin;
    private final ConfigFile<LevelConfig> levelConfig;
    private final ConfigFile<LevelMenuConfig> levelMenuConfig;
    private final ConfigFile<MilestoneMenuConfig> milestoneMenuConfig;
    // By language: only the configured language is used, a file stays loaded so a reload can keep it if it's broken
    private final Map<String, ConfigFile<MessageConfig>> messageConfigs = new ConcurrentHashMap<>();
    private volatile String language;

    public ConfigManager(AuroraLevels plugin) {
        this.plugin = plugin;
        levelConfig = ConfigFile.builder(plugin, "config.yml", LevelConfig.class).migrations(LevelConfig.migrations()).build();
        levelMenuConfig = ConfigFile.builder(plugin, "menus/levels.yml", LevelMenuConfig.class).migrations(LevelMenuConfig.migrations()).build();
        milestoneMenuConfig = ConfigFile.builder(plugin, "menus/milestones.yml", MilestoneMenuConfig.class).build();
        levelConfig.get();
        levelMenuConfig.get();
        milestoneMenuConfig.get();
        loadMessages(false);
    }

    public void reload() {
        levelConfig.reload();
        levelMenuConfig.reload();
        milestoneMenuConfig.reload();
        loadMessages(true);
    }

    private void loadMessages(boolean reload) {
        var language = getLevelConfig().getLanguage();
        var existing = messageConfigs.get(language);
        if (existing != null) {
            if (reload) existing.reload();
        } else {
            var name = "messages_" + language + ".yml";
            if (!hasResource(name) && !new File(plugin.getDataFolder(), name).exists()) {
                // The plugin's own logger: this runs before AuroraLevels.logger() exists
                plugin.getLogger().warning("Internal message file for language: " + language + " not found! Creating a new one from english...");
            }
            messageConfigs.put(language, ConfigFile.builder(plugin, name, MessageConfig.class)
                    .bundled(hasResource(name) ? name : "messages_en.yml")
                    .migrations(MessageConfig.migrations())
                    .build());
            messageConfigs.get(language).get();
        }
        this.language = language;
    }

    private boolean hasResource(String name) {
        try (var in = plugin.getResource(name)) {
            return in != null;
        } catch (IOException e) {
            return false;
        }
    }

    public LevelConfig getLevelConfig() {
        return levelConfig.get();
    }

    public MessageConfig getMessageConfig() {
        return messageConfigs.get(language).get();
    }

    public LevelMenuConfig getLevelMenuConfig() {
        return levelMenuConfig.get();
    }

    public MilestoneMenuConfig getMilestoneMenuConfig() {
        return milestoneMenuConfig.get();
    }
}
