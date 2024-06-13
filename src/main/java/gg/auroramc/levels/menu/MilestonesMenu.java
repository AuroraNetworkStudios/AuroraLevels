package gg.auroramc.levels.menu;

import gg.auroramc.aurora.api.levels.ConcreteMatcher;
import gg.auroramc.aurora.api.levels.LevelMatcher;
import gg.auroramc.aurora.api.menu.AuroraMenu;
import gg.auroramc.aurora.api.menu.ItemBuilder;
import gg.auroramc.aurora.api.message.Placeholder;
import gg.auroramc.aurora.api.message.Text;
import gg.auroramc.aurora.api.util.NamespacedId;
import gg.auroramc.levels.AuroraLevels;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MilestonesMenu {
    @Getter
    private final static NamespacedId menuId = NamespacedId.fromDefault("milestones_menu");
    private final Player player;
    private int page = 0;
    private final AuroraLevels plugin;

    public MilestonesMenu(AuroraLevels plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
    }

    public void open() {
        createMenu().open();
    }

    private AuroraMenu createMenu() {
        var leveler = plugin.getLeveler();
        var menuConfig = plugin.getConfigManager().getMilestoneMenuConfig();
        var lvlMenuConfig = plugin.getConfigManager().getLevelMenuConfig();
        var lvlConfig = plugin.getConfigManager().getLevelConfig();

        var menu = new AuroraMenu(player, menuConfig.getTitle(), 54, false, menuId);

        if(menuConfig.getItems().getFiller().getEnabled()) {
            menu.addFiller(ItemBuilder.of(menuConfig.getItems().getFiller().getItem()).slot(0).build(player).getItemStack());
        } else {
            menu.addFiller(ItemBuilder.filler(Material.AIR));
        }

        long level = leveler.getUserData(player).getLevel();

        for (var cItem : menuConfig.getCustomItems().values()) {
            menu.addItem(ItemBuilder.of(cItem).build(player));
        }

        var customLevels = getPage(page, menuConfig.getDisplayArea().size());

        for (int i = 0; i < menuConfig.getDisplayArea().size(); i++) {
            var slot = menuConfig.getDisplayArea().get(i);
            if (customLevels.size() <= i) {
                break;
            }

            var milestone = customLevels.get(i);
            var rewards = milestone.getValue().computeRewards(milestone.getValue().getConfig().getLevel());
            var milestoneLevel = milestone.getKey();


            if (milestoneLevel == 0) continue;

            var itemConfig = lvlMenuConfig.getItems().getLockedLevel();
            var defaultMaterial = Material.RED_STAINED_GLASS_PANE;

            if (milestoneLevel <= level) {
                itemConfig = lvlMenuConfig.getItems().getCompletedLevel();
                defaultMaterial = Material.LIME_STAINED_GLASS_PANE;
            }

            List<Placeholder<?>> placeholders = leveler.getRewardFormulaPlaceholders(player, milestoneLevel);

            var lore = new ArrayList<String>();

            for (var line : itemConfig.getLore()) {
                if (line.equals("component:rewards")) {
                    var display = lvlMenuConfig.getDisplayComponents().get("rewards");
                    if (!rewards.isEmpty()) {
                        lore.add(display.getTitle());
                    }
                    for (var reward : rewards) {
                        lore.add(display.getLine().replace("{reward}", reward.getDisplay(player, placeholders)));
                    }
                } else {
                    lore.add(line);
                }
            }

            var item = ItemBuilder.of(itemConfig).defaultMaterial(defaultMaterial)
                    .slot(slot).placeholder(placeholders)
                    .loreCompute(() -> lore.stream().map(l -> Text.component(player, l, placeholders)).toList())
                    .build(player);

            menu.addItem(item);
        }

        var pageCount = getTotalPageCount(menuConfig.getDisplayArea().size());

        if (leveler.getLevelMatcher().getCustomMatchers().size() > menuConfig.getDisplayArea().size()) {
            List<Placeholder<?>> placeholders = List.of(Placeholder.of("{current}", page + 1), Placeholder.of("{max}", pageCount + 1));

            menu.addItem(ItemBuilder.of(menuConfig.getItems().getPreviousPage()).defaultSlot(48).placeholder(placeholders).build(player),
                    (e) -> {
                        if (page > 0) {
                            page--;
                            createMenu().open();
                        }
                    });

            menu.addItem(ItemBuilder.of(menuConfig.getItems().getCurrentPage()).defaultSlot(49)
                    .placeholder(placeholders).build(player));

            menu.addItem(ItemBuilder.of(menuConfig.getItems().getNextPage()).defaultSlot(50).placeholder(placeholders).build(player),
                    (e) -> {
                        if (page < pageCount) {
                            page++;
                            createMenu().open();
                        }
                    });
        }


        return menu;
    }

    private List<Map.Entry<Integer, ConcreteMatcher>> getPage(int page, int pageSize) {
        return plugin.getLeveler().getLevelMatcher().getCustomMatchers().entrySet()
                .stream().sorted(Map.Entry.comparingByKey())
                .skip((long) page * pageSize).limit(pageSize).toList();
    }

    private int getTotalPageCount(int pageSize) {
        var levelMatcher = plugin.getLeveler().getLevelMatcher();
        return (int) Math.ceil((double) levelMatcher.getCustomMatchers().size() / pageSize) - 1;
    }
}
