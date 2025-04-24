package gg.auroramc.levels.config.menu;

import gg.auroramc.aurora.api.config.AuroraConfig;
import gg.auroramc.aurora.api.config.premade.ItemConfig;
import gg.auroramc.levels.AuroraLevels;
import lombok.Getter;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@Getter
public class LevelMenuConfig extends AuroraConfig {
    private String title;
    private List<Integer> levelTrack;
    private Integer rows = 6;
    private Boolean usePagination = false;
    private Boolean useItemAmounts = false;
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
        private ItemConfig nextPage;
        private ItemConfig previousPage;
        private ItemConfig currentPage;
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

    @Override
    protected List<Consumer<YamlConfiguration>> getMigrationSteps() {
        return List.of(
                (yaml) -> {
                    yaml.set("config-version", null);

                    int rows = yaml.getInt("rows", 6) - 1;

                    yaml.set("items.next-page.name", "&fNext page");
                    yaml.set("items.next-page.lore", List.of("&8Click to view the next page"));
                    yaml.set("items.next-page.material", "ARROW");
                    yaml.set("items.next-page.slot", rows * 9 + 5);

                    yaml.set("items.previous-page.name", "&fPrevious page");
                    yaml.set("items.previous-page.lore", List.of("&8Click to view the previous page"));
                    yaml.set("items.previous-page.material", "ARROW");
                    yaml.set("items.previous-page.slot", rows * 9 + 3);

                    yaml.set("items.current-page.name", "&fPage {current}&7/&f{max}");
                    yaml.set("items.current-page.material", "PAPER");
                    yaml.set("items.current-page.slot", rows * 9 + 4);

                    yaml.set("use-pagination", false);
                    yaml.set("use-item-amounts", false);

                    yaml.set("config-version", 1);
                }
        );
    }
}
