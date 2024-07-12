package gg.auroramc.levels.config;

import gg.auroramc.aurora.api.config.AuroraConfig;
import gg.auroramc.aurora.api.config.premade.ConcreteMatcherConfig;
import gg.auroramc.aurora.api.config.premade.IntervalMatcherConfig;
import gg.auroramc.levels.AuroraLevels;
import lombok.Getter;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

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
    private Map<String, IntervalMatcherConfig> levelMatchers;
    private Map<String, ConcreteMatcherConfig> customLevels;
    private CommandAliasConfig commandAliases;
    private Map<String, String> iconGenerator;
    private Integer leaderboardCacheSize = 10;

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

    public LevelConfig(AuroraLevels plugin) {
        super(getFile(plugin));
    }

    public static File getFile(AuroraLevels plugin) {
        return new File(plugin.getDataFolder(), "config.yml");
    }

    public static void saveDefault(AuroraLevels plugin) {
        if (!getFile(plugin).exists()) {
            plugin.saveResource("config.yml", false);
        }
    }

    @Override
    protected List<Consumer<YamlConfiguration>> getMigrationSteps() {
        return List.of(
                (yaml) -> {
                    yaml.set("leaderboard-cache-size", 10);
                    yaml.setComments("leaderboard-cache-size", List.of("This only affects placeholder generation, like %aurora_lb_levels_name_10% and %aurora_lb_levels_fvalue_10%"));
                    yaml.set("config-version", null);
                    yaml.set("config-version", 1);
                },
                (yaml) -> {
                    yaml.set("icon-generator", Map.of(
                            "0", "%any_placeholder_here%",
                            "1", "%oraxen_number1%",
                            "2", "any character, text here",
                            "50", "same"
                    ));
                    yaml.set("config-version", null);
                    yaml.set("config-version", 1);
                }
        );
    }
}
