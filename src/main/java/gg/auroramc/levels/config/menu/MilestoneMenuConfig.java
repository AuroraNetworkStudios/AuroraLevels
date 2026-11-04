package gg.auroramc.levels.config.menu;

import gg.auroramc.aurora.api.config.premade.ItemConfig;
import lombok.Getter;

import java.util.List;
import java.util.Map;

@Getter
public class MilestoneMenuConfig {
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
}
