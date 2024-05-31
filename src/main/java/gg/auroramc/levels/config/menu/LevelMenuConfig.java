package gg.auroramc.levels.config.menu;

import gg.auroramc.aurora.api.config.AuroraConfig;
import gg.auroramc.aurora.api.config.premade.ItemConfig;
import gg.auroramc.levels.AuroraLevels;
import lombok.Getter;

import java.io.File;
import java.util.List;
import java.util.Map;

@Getter
public class LevelMenuConfig extends AuroraConfig {
    private String title;
    private List<Integer> levelTrack;
    private Map<String, DisplayComponent> displayComponents;
    private Items items;
    private Map<String, ItemConfig> customItems;
    private ProgressBar progressBar;

    @Getter
    public static final class DisplayComponent {
        private String title;
        private String line;
    }

    @Getter
    public static final class Items {
        private FillerItem filler;
        private ItemConfig completedLevel;
        private ItemConfig nextLevel;
        private ItemConfig lockedLevel;
    }

    @Getter
    public static final class FillerItem {
        private Boolean enabled;
        private ItemConfig item;
    }

    @Getter
    public static final class ProgressBar {
        private Integer length = 20;
        private String filledCharacter;
        private String unfilledCharacter;
    }


    public LevelMenuConfig(AuroraLevels plugin) {
        super(getFile(plugin));
    }

    private static File getFile(AuroraLevels plugin) {
        return new File(plugin.getDataFolder(), "menus/levels.yml");
    }

    public static void saveDefault(AuroraLevels plugin) {
        if (!getFile(plugin).exists()) {
            plugin.saveResource("menus/levels.yml", false);
        }
    }
}
