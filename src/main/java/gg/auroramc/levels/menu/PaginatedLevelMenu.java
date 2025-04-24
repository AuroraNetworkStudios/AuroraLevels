package gg.auroramc.levels.menu;

import gg.auroramc.aurora.api.AuroraAPI;
import gg.auroramc.aurora.api.config.premade.ItemConfig;
import gg.auroramc.aurora.api.levels.ConcreteMatcher;
import gg.auroramc.aurora.api.levels.IntervalMatcher;
import gg.auroramc.aurora.api.menu.AuroraMenu;
import gg.auroramc.aurora.api.menu.ItemBuilder;
import gg.auroramc.aurora.api.message.Placeholder;
import gg.auroramc.aurora.api.message.Text;
import gg.auroramc.aurora.api.util.NamespacedId;
import gg.auroramc.levels.AuroraLevels;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PaginatedLevelMenu {
    private int page = 0;

    @Getter
    private final static NamespacedId menuId = NamespacedId.fromDefault("paginated_level_menu");

    private final Player player;
    private final AuroraLevels plugin;

    public PaginatedLevelMenu(AuroraLevels plugin, Player player) {
        this.plugin = plugin;
        this.player = player;

        int level = plugin.getLeveler().getUserData(player).getLevel();
        int pageSize = plugin.getConfigManager().getLevelMenuConfig().getLevelTrack().size();
        int totalPageCount = (int) Math.ceil(plugin.getLeveler().getLevelCap() / (double) pageSize) - 1;
        this.page = Math.min(level / pageSize, totalPageCount);
    }

    public void open() {
        createMenu().open();
    }

    private AuroraMenu createMenu() {
        var leveler = plugin.getLeveler();
        var cfg = plugin.getConfigManager();
        var menuConfig = cfg.getLevelMenuConfig();

        int level = leveler.getUserData(player).getLevel();
        int pageSize = menuConfig.getLevelTrack().size();
        int levelCap = plugin.getLeveler().getLevelCap();

        var menu = new AuroraMenu(player, menuConfig.getTitle(), 9 * menuConfig.getRows(), false, menuId);

        if (menuConfig.getItems().getFiller().getEnabled()) {
            menu.addFiller(ItemBuilder.of(menuConfig.getItems().getFiller().getItem()).slot(0).build(player).getItemStack());
        } else {
            menu.addFiller(ItemBuilder.filler(Material.AIR));
        }

        var lbm = AuroraAPI.getLeaderboards();

        var lb = AuroraAPI.getUser(player.getUniqueId()).getLeaderboardEntries().get("levels");
        var lbPositionPlaceholder = Placeholder.of("{lb_position}", lb == null || lb.getPosition() == 0 ? lbm.getEmptyPlaceholder() : AuroraAPI.formatNumber(lb.getPosition()));
        var lbPositionPercentPlaceholder = Placeholder.of("{lb_position_percent}",
                lb == null || lb.getPosition() == 0 ? lbm.getEmptyPlaceholder() : AuroraAPI.formatNumber(
                        Math.min(((double) lb.getPosition() / Math.max(1, AuroraAPI.getLeaderboards().getBoardSize("levels"))) * 100, 100)
                )
        );
        var lbBoardSizePlaceholder = Placeholder.of("{lb_size}", AuroraAPI.formatNumber(Math.max(AuroraAPI.getLeaderboards().getBoardSize("levels"), lb == null || lb.getPosition() == 0 ? Bukkit.getOnlinePlayers().size() : Math.max(lb.getPosition(), Bukkit.getOnlinePlayers().size()))));
        var totalCurrentXP = leveler.getXpForLevel(leveler.getUserData(player).getLevel()) + leveler.getUserData(player).getCurrentXP();

        for (var customItem : menuConfig.getCustomItems().values()) {
            menu.addItem(ItemBuilder.of(customItem)
                    .placeholder(Placeholder.of("{level}", level))
                    .placeholder(lbPositionPlaceholder)
                    .placeholder(lbPositionPercentPlaceholder)
                    .placeholder(lbBoardSizePlaceholder)
                    .placeholder(Placeholder.of("{current}", AuroraAPI.formatNumber(totalCurrentXP)))
                    .placeholder(Placeholder.of("{current_short}", AuroraAPI.formatNumberShort(totalCurrentXP)))
                    .build(player));
        }


        int pageStartLevel = page * pageSize + 1;
        int pageEndLevel = Math.min((page + 1) * pageSize, levelCap);
        int totalPageCount = (int) Math.ceil(levelCap / (double) pageSize) - 1;

        int iteratorLevel = pageStartLevel;

        for (var slot : menuConfig.getLevelTrack()) {
            var matcher = leveler.getLevelMatcher().getBestMatcher(iteratorLevel);
            var rewards = matcher.computeRewards(iteratorLevel);

            Map<String, ItemConfig> overrideItems = Map.of();

            if (matcher instanceof IntervalMatcher intervalMatcher) {
                overrideItems = intervalMatcher.getConfig().getItem();
            } else if (matcher instanceof ConcreteMatcher concreteMatcher) {
                overrideItems = concreteMatcher.getConfig().getItem();
            }

            ItemConfig itemConfig;
            Material defaultMaterial;

            if (iteratorLevel <= level) {
                itemConfig = menuConfig.getItems().getCompletedLevel().merge(overrideItems.get("completed-level"));
                defaultMaterial = Material.LIME_STAINED_GLASS_PANE;
            } else if (iteratorLevel - 1 == level) {
                itemConfig = menuConfig.getItems().getNextLevel().merge(overrideItems.get("next-level"));
                defaultMaterial = Material.YELLOW_STAINED_GLASS_PANE;
            } else {
                itemConfig = menuConfig.getItems().getLockedLevel().merge(overrideItems.get("locked-level"));
                defaultMaterial = Material.RED_STAINED_GLASS_PANE;
            }

            List<Placeholder<?>> placeholders = leveler.getRewardFormulaPlaceholders(player, Math.min(iteratorLevel, leveler.getLevelCap()));
            placeholders.add(lbPositionPlaceholder);
            placeholders.add(lbPositionPercentPlaceholder);
            placeholders.add(lbBoardSizePlaceholder);

            if (iteratorLevel - 1 == level) {
                var currentXP = leveler.getUserData(player).getCurrentXP();
                var requiredXP = leveler.getRequiredXpForLevelUp(player);
                placeholders.add(Placeholder.of("{current}", AuroraAPI.formatNumber(((Double) currentXP).longValue())));
                placeholders.add(Placeholder.of("{current_short}", AuroraAPI.formatNumberShort(currentXP)));
                placeholders.add(Placeholder.of("{required}", AuroraAPI.formatNumber(((Double) requiredXP).longValue())));
                placeholders.add(Placeholder.of("{required_short}", AuroraAPI.formatNumberShort(requiredXP)));

                var bar = menuConfig.getProgressBar();
                var pcs = bar.getLength();
                var completedPercent = currentXP / requiredXP;
                var completedPcs = ((Double) Math.floor(pcs * completedPercent)).intValue();
                var remainingPcs = pcs - completedPcs;
                placeholders.add(Placeholder.of("{progressbar}", bar.getFilledCharacter().repeat(completedPcs) + bar.getUnfilledCharacter().repeat(remainingPcs) + "&r"));
            }

            var lore = new ArrayList<String>();

            for (var line : itemConfig.getLore()) {
                if (line.equals("component:rewards")) {
                    var display = menuConfig.getDisplayComponents().get("rewards");
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

            var item = ItemBuilder.of(itemConfig).defaultMaterial(defaultMaterial).slot(slot)
                    .placeholder(placeholders)
                    .loreCompute(() -> lore.stream().map(l -> Text.component(player, l, placeholders)).toList())
                    .amount(menuConfig.getUseItemAmounts() && leveler.getLevelCap() <= 64 ? iteratorLevel : 1)
                    .build(player);

            menu.addItem(item);

            iteratorLevel++;
            if (iteratorLevel == pageEndLevel + 1) break;
        }

        if (totalPageCount <= 0) return menu;

        List<Placeholder<?>> placeholders = List.of(Placeholder.of("{current}", page + 1), Placeholder.of("{max}", totalPageCount + 1));

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
                    if (page < totalPageCount) {
                        page++;
                        createMenu().open();
                    }
                });

        return menu;
    }
}
