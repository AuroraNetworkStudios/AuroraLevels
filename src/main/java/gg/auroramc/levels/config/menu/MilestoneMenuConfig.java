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
    private Integer rows = 6;
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

    public MilestoneMenuConfig(AuroraLevels plugin) {
        super(getFile(plugin));
    }

    private static File getFile(AuroraLevels plugin) {
        return new File(plugin.getDataFolder(), "menus/milestones.yml");
    }

    public static void saveDefault(AuroraLevels plugin) {
        if (!getFile(plugin).exists()) {
            plugin.saveResource("menus/milestones.yml", false);
        }
    }
}
