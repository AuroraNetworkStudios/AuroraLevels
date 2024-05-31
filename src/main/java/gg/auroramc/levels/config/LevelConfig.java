package gg.auroramc.levels.config;

import gg.auroramc.aurora.api.config.AuroraConfig;
import gg.auroramc.levels.AuroraLevels;
import lombok.Getter;
import org.bukkit.configuration.ConfigurationSection;

import java.io.File;
import java.util.List;
import java.util.Map;

@Getter
public class LevelConfig extends AuroraConfig {
    private Boolean debug = false;
    private String language = "en";
    private String xpFormula;
    private Map<String, String> formulaPlaceholders;
    private Map<String, DisplayComponent> displayComponents;
    private LevelUpMessage levelUpMessage;
    private LevelUpTitle levelUpTitle;
    private XpGainActionBar xpGainActionBar;
    private LevelUpSound levelUpSound;
    private Map<String, LevelMatcherConfig> levelMatchers;
    private Map<Long, CustomLevel> customLevels;
    private CommandAliasConfig commandAliases;

    @Getter
    public static final class CommandAliasConfig {
        private List<String> level = List.of("level");
        private List<String> setraw = List.of("setraw");
        private List<String> set = List.of("set");
        private List<String> addxp = List.of("addxp");
        private List<String> milestones = List.of("milestones");
    }

    @Getter
    public static final class LevelUpMessage {
        private Boolean enabled;
        private List<String> message;
    }

    @Getter
    public static final class DisplayComponent {
        private String title;
        private String line;
    }

    @Getter
    public static final class LevelUpTitle {
        private Boolean enabled;
        private String title;
        private String subtitle;
    }

    @Getter
    public static final class XpGainActionBar {
        private Boolean enabled;
        private String message;
    }

    @Getter
    public static final class LevelUpSound {
        private Boolean enabled;
        private String sound;
        private Float volume;
        private Float pitch;
    }

    @Getter
    public static final class LevelMatcherConfig {
        private Integer interval;
        private Integer priority;
        private ConfigurationSection rewards;
    }

    @Getter
    public static final class CustomLevel {
        private ConfigurationSection rewards;
    }

    public LevelConfig() {
        super(getFile());
    }

    public static File getFile() {
        return new File(AuroraLevels.getInstance().getDataFolder(), "config.yml");
    }

    public static void saveDefault() {
        if (!getFile().exists()) {
            AuroraLevels.getInstance().saveResource("config.yml", false);
        }
    }
}
