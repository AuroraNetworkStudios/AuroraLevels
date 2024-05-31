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


    public LevelMenuConfig() {
        super(getFile());
    }

    private static File getFile() {
        return new File(AuroraLevels.getInstance().getDataFolder(), "menus/levels.yml");
    }

    public static void saveDefault() {
        if (!getFile().exists()) {
            AuroraLevels.getInstance().saveResource("menus/levels.yml", false);
        }
    }
}
