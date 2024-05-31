package gg.auroramc.levels.config.menu;

import gg.auroramc.aurora.api.config.AuroraConfig;
import gg.auroramc.aurora.api.config.premade.ItemConfig;
import gg.auroramc.levels.AuroraLevels;
import lombok.Getter;

import java.io.File;
import java.util.List;
import java.util.Map;

@Getter
public class MilestoneMenuConfig extends AuroraConfig {
    private String title;
    private List<Integer> displayArea;
    private Items items;
    private Map<String, ItemConfig> customItems;

    @Getter
    public static final class Items {
        private FillerItem filler;
        private ItemConfig previousPage;
        private ItemConfig currentPage;
        private ItemConfig nextPage;
    }

    @Getter
    public static final class FillerItem {
        private Boolean enabled;
        private ItemConfig item;
    }

    public MilestoneMenuConfig() {
        super(getFile());
    }

    private static File getFile() {
        return new File(AuroraLevels.getInstance().getDataFolder(), "menus/milestones.yml");
    }

    public static void saveDefault() {
        if (!getFile().exists()) {
            AuroraLevels.getInstance().saveResource("menus/milestones.yml", false);
        }
    }
}
