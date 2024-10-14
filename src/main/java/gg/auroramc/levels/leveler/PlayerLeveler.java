package gg.auroramc.levels.leveler;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import gg.auroramc.aurora.api.AuroraAPI;
import gg.auroramc.aurora.api.events.user.AuroraUserLoadedEvent;
import gg.auroramc.aurora.api.expression.NumberExpression;
import gg.auroramc.aurora.api.levels.MatcherManager;
import gg.auroramc.aurora.api.message.*;
import gg.auroramc.aurora.api.reward.*;
import gg.auroramc.aurora.api.util.NamespacedId;
import gg.auroramc.levels.AuroraLevels;
import gg.auroramc.levels.api.data.LevelData;
import gg.auroramc.levels.api.event.PlayerLevelUpEvent;
import gg.auroramc.levels.api.event.PlayerXpGainEvent;
import gg.auroramc.levels.api.leveler.Leveler;
import gg.auroramc.levels.reward.corrector.CommandCorrector;
import lombok.Getter;
import lombok.SneakyThrows;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class PlayerLeveler implements Leveler, Listener {
    private final AuroraLevels plugin;
    private final AtomicReference<NumberExpression> xpFormula = new AtomicReference<>();
    private final Map<Integer, Double> levelXPCache = new ConcurrentHashMap<>();
    private final Map<String, NumberExpression> formulas = new ConcurrentHashMap<>();
    private final Map<String, Map<Integer, Double>> formulaCache = new ConcurrentHashMap<>();
    private final AtomicReference<MatcherManager> levelMatcher = new AtomicReference<>();
    @Getter
    private final RewardFactory rewardFactory = new RewardFactory();
    @Getter
    private final RewardAutoCorrector rewardAutoCorrector = new RewardAutoCorrector();

    private final Cache<Double, Integer> xpToLevelCache = CacheBuilder.newBuilder()
            .expireAfterWrite(10, TimeUnit.MINUTES)
            .build();

    public MatcherManager getLevelMatcher() {
        return levelMatcher.get();
    }

    public PlayerLeveler(AuroraLevels plugin) {
        Bukkit.getPluginManager().registerEvents(this, plugin);
        this.plugin = plugin;

        rewardFactory.registerRewardType(NamespacedId.fromDefault("command"), CommandReward.class);
        rewardFactory.registerRewardType(NamespacedId.fromDefault("money"), MoneyReward.class);
        rewardFactory.registerRewardType(NamespacedId.fromDefault("item"), ItemReward.class);


        this.levelMatcher.set(new MatcherManager(rewardFactory));

        rewardAutoCorrector.registerCorrector(NamespacedId.fromDefault("command"), new CommandCorrector(plugin));

        reload(true);
    }

    public void reload(boolean first) {
        var config = plugin.getConfigManager().getLevelConfig();
        var xpFormula = new NumberExpression(config.getXpFormula().replace("{level}", "level"), "level");
        this.xpFormula.set(xpFormula);

        formulas.clear();

        for (var formula : config.getFormulaPlaceholders().entrySet()) {
            formulas.put(formula.getKey(), new NumberExpression(formula.getValue().replace("{level}", "level"), "level"));
        }

        levelXPCache.clear();
        formulaCache.clear();

        if (!first) {
            levelMatcher.get().reload(config.getLevelMatchers(), config.getCustomLevels());
        }
    }

    public LevelData getUserData(Player player) {
        return AuroraAPI.getUser(player.getUniqueId()).getData(LevelData.class);
    }

    public double addXpToPlayer(Player player, double xp) {
        if (!player.hasPermission("aurora.levels.use")) return 0;
        var event = new PlayerXpGainEvent(player, xp);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) return 0;
        xp = event.getXp();

        var data = getUserData(player);

        double requiredXpToLevelUp = getRequiredXpForLevelUp(player);
        double newXP = data.getCurrentXP() + xp;

        if (plugin.getConfigManager().getLevelConfig().getXpGainActionBar().getEnabled()) {
            ActionBar.send(player, plugin.getConfigManager().getLevelConfig().getXpGainActionBar().getMessage(),
                    Placeholder.of("{amount}", AuroraAPI.formatNumber(xp)));
        }

        if (newXP < requiredXpToLevelUp) {
            data.setCurrentXP(newXP);
            AuroraAPI.getLeaderboards().updateUser(AuroraAPI.getUserManager().getUser(player), "levels");
            return xp;
        }

        while (newXP >= requiredXpToLevelUp) {
            data.setLevel(data.getLevel() + 1);
            data.setCurrentXP(newXP - requiredXpToLevelUp);

            newXP = data.getCurrentXP();
            requiredXpToLevelUp = getRequiredXpForLevelUp(player);

            rewardPlayer(player, data.getLevel());
            Bukkit.getPluginManager().callEvent(new PlayerLevelUpEvent(player, data.getLevel()));
        }

        AuroraAPI.getLeaderboards().updateUser(AuroraAPI.getUserManager().getUser(player), "levels");

        return xp;
    }

    public List<Placeholder<?>> getRewardFormulaPlaceholders(Player player, int level) {
        var config = plugin.getConfigManager().getLevelConfig();
        var formulaPlaceholders = new ArrayList<Placeholder<?>>();

        for (var formula : config.getFormulaPlaceholders().entrySet()) {
            var value = getFormulaValueForLevel(formula.getKey(), level);
            formulaPlaceholders.add(Placeholder.of("{" + formula.getKey() + "}", value));
            formulaPlaceholders.add(Placeholder.of("{" + formula.getKey() + "_int}", ((Double) value).longValue()));
            formulaPlaceholders.add(Placeholder.of("{" + formula.getKey() + "_formatted}", AuroraAPI.formatNumber(value)));
        }

        var totalRequiredXP = getXpForLevel(level);
        var totalXP = getXpForLevel(getUserData(player).getLevel()) + getUserData(player).getCurrentXP();

        formulaPlaceholders.add(Placeholder.of("{required_xp_total}", AuroraAPI.formatNumber(totalRequiredXP)));
        formulaPlaceholders.add(Placeholder.of("{required_xp_total_short}", AuroraAPI.formatNumberShort(totalRequiredXP)));
        formulaPlaceholders.add(Placeholder.of("{current_xp_total}", AuroraAPI.formatNumber(totalXP)));
        formulaPlaceholders.add(Placeholder.of("{current_xp_total_short}", AuroraAPI.formatNumberShort(totalXP)));
        formulaPlaceholders.add(Placeholder.of("{player}", player.getName()));
        formulaPlaceholders.add(Placeholder.of("{level}", level));
        formulaPlaceholders.add(Placeholder.of("{level_int}", level));
        formulaPlaceholders.add(Placeholder.of("{level_formatted}", AuroraAPI.formatNumber(level)));

        return formulaPlaceholders;
    }

    private void rewardPlayer(Player player, int level) {
        var config = plugin.getConfigManager().getLevelConfig();

        List<Placeholder<?>> placeholders = getRewardFormulaPlaceholders(player, level);

        placeholders.add(Placeholder.of("{prev_level}", level - 1));
        placeholders.add(Placeholder.of("{prev_level_int}", level - 1));
        placeholders.add(Placeholder.of("{prev_level_formatted}", AuroraAPI.formatNumber(level - 1)));

        var matcher = levelMatcher.get().getBestMatcher(level);
        var rewards = matcher.computeRewards(level);

        RewardExecutor.execute(rewards, player, level, placeholders);

        if (config.getLevelUpSound().getEnabled()) {
            var sound = config.getLevelUpSound();
            player.playSound(player.getLocation(),
                    Sound.valueOf(sound.getSound().toUpperCase()),
                    sound.getVolume(),
                    sound.getPitch());
        }

        if (config.getLevelUpMessage().getEnabled()) {
            var text = Component.text();

            var messageLines = config.getLevelUpMessage().getMessage();

            int count = 0;
            for (var line : messageLines) {
                count++;
                if (line.equals("component:rewards")) {
                    if (!rewards.isEmpty()) {
                        text.append(Text.component(player, config.getDisplayComponents().get("rewards").getTitle(), placeholders));
                    }
                    for (var reward : rewards) {
                        text.append(Component.newline());
                        var display = config.getDisplayComponents().get("rewards").getLine().replace("{reward}", reward.getDisplay(player, placeholders));
                        text.append(Text.component(player, display, placeholders));
                    }
                } else {
                    text.append(Text.component(player, line, placeholders));
                }

                if (count != messageLines.size()) text.append(Component.newline());
            }

            if (config.getLevelUpMessage().getOpenMenuWhenClicked()) {
                text.clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, "/" + config.getCommandAliases().getLevel().get(0)));
            }

            Chat.sendMessage(player, text.build());
        }

        if (config.getLevelUpTitle().getEnabled()) {
            TitleBar.send(player, config.getLevelUpTitle().getTitle(), config.getLevelUpTitle().getSubtitle(), placeholders);
        }
    }

    public void setPlayerLevel(Player player, int level) {
        var data = getUserData(player);
        data.setCurrentXP(0);
        if (data.getLevel() == level) return;

        if (data.getLevel() > level) {
            data.setLevel(level);
            Bukkit.getPluginManager().callEvent(new PlayerLevelUpEvent(player, level));
            AuroraAPI.getLeaderboards().updateUser(AuroraAPI.getUserManager().getUser(player), "levels");
            return;
        }

        for (int l = data.getLevel() + 1; l <= level; l++) {
            data.setLevel(l);
            rewardPlayer(player, l);
            Bukkit.getPluginManager().callEvent(new PlayerLevelUpEvent(player, level));
        }

        AuroraAPI.getLeaderboards().updateUser(AuroraAPI.getUserManager().getUser(player), "levels");
    }

    public void setPlayerLevelRaw(Player player, int level) {
        var data = getUserData(player);
        data.setCurrentXP(0);
        data.setLevel(level);
        AuroraAPI.getLeaderboards().updateUser(AuroraAPI.getUserManager().getUser(player), "levels");
    }

    public double getXpForLevel(int level) {
        if (level == 0) return 0;
        return levelXPCache.computeIfAbsent(level,
                (l) -> xpFormula.get().evaluate(Placeholder.of("level", l)));
    }

    @SneakyThrows
    public int getLevelFromXP(final double currentXp) {
        return xpToLevelCache.get(currentXp, () -> {
            int level = 0;

            // Iterate until the total XP required for the next level exceeds the current XP
            while (true) {
                double xpForNextLevel = getXpForLevel(level);
                if (xpForNextLevel > currentXp) {
                    break;
                }
                level++;
            }

            return level - 1;
        });
    }

    public double getFormulaValueForLevel(String formula, int level) {
        return formulaCache.computeIfAbsent(formula, (f) -> new ConcurrentHashMap<>())
                .computeIfAbsent(level, (l) -> formulas.get(formula).evaluate(Placeholder.of("level", l)));
    }

    public double getRequiredXpForLevelUp(Player player) {
        var data = getUserData(player);
        double currentLevelXP = getXpForLevel(data.getLevel());
        double nextLevelXP = getXpForLevel(data.getLevel() + 1);
        return nextLevelXP - currentLevelXP;
    }

    public void correctCurrentXP(Player player) {
        var data = getUserData(player);
        if (data.getCurrentXP() >= getRequiredXpForLevelUp(player)) {
            data.setCurrentXP(0);
            AuroraAPI.getLeaderboards().updateUser(AuroraAPI.getUserManager().getUser(player), "levels");
        }
    }

    @EventHandler
    public void onUserLoaded(AuroraUserLoadedEvent event) {
        var player = event.getUser().getPlayer();
        if (player == null) return;
        CompletableFuture.runAsync(() -> {
            correctCurrentXP(player);
            rewardAutoCorrector.correctRewards(player);
        });
    }
}
