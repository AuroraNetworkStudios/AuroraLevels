package gg.auroramc.levels.leveler;

import gg.auroramc.aurora.api.AuroraAPI;
import gg.auroramc.aurora.api.events.user.AuroraUserLoadedEvent;
import gg.auroramc.aurora.api.expression.NumberExpression;
import gg.auroramc.aurora.api.message.ActionBar;
import gg.auroramc.aurora.api.message.Placeholder;
import gg.auroramc.aurora.api.message.Text;
import gg.auroramc.levels.AuroraLevels;
import gg.auroramc.levels.api.data.LevelData;
import gg.auroramc.levels.api.leveler.LevelMatcher;
import gg.auroramc.levels.api.leveler.Leveler;
import gg.auroramc.levels.api.reward.LevelReward;
import gg.auroramc.levels.api.event.PlayerLevelUpEvent;
import gg.auroramc.levels.api.event.PlayerXpGainEvent;
import gg.auroramc.levels.api.reward.RewardCorrector;
import gg.auroramc.levels.reward.CommandReward;
import gg.auroramc.levels.reward.MoneyReward;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

public class PlayerLeveler implements Leveler, Listener {
    private final AuroraLevels plugin;
    private final AtomicReference<NumberExpression> xpFormula = new AtomicReference<>();
    private final Map<Long, Double> levelXPCache = new ConcurrentHashMap<>();
    private final Map<String, NumberExpression> formulas = new ConcurrentHashMap<>();
    private final Map<String, Map<Long, Double>> formulaCache = new ConcurrentHashMap<>();
    private final AtomicReference<LevelMatcher> levelMatcher = new AtomicReference<>();
    private final Map<String, RewardCorrector> rewardCorrectors = new ConcurrentHashMap<>();
    private final Map<String, Class<? extends LevelReward>> rewardTypes = new ConcurrentHashMap<>(
            Map.of("money", MoneyReward.class, "command", CommandReward.class));

    public LevelMatcher getLevelMatcher() {
        return levelMatcher.get();
    }

    public PlayerLeveler(AuroraLevels plugin) {
        Bukkit.getPluginManager().registerEvents(this, plugin);
        this.plugin = plugin;
        this.levelMatcher.set(new LevelMatcher(this));

        reload(true);
    }

    public void registerRewardCorrector(String name, RewardCorrector corrector) {
        rewardCorrectors.put(name, corrector);
    }

    @Override
    public void registerRewardType(String rewardType, Class<? extends LevelReward> clazz) {
        rewardTypes.put(rewardType, clazz);
    }

    public void reload() {
        reload(false);
    }

    public void reload(boolean first) {
        var xpFormula = new NumberExpression(plugin.getConfigManager().getLevelConfig().getXpFormula().replace("{level}", "level"), "level");
        this.xpFormula.set(xpFormula);

        formulas.clear();

        for (var formula : plugin.getConfigManager().getLevelConfig().getFormulaPlaceholders().entrySet()) {
            formulas.put(formula.getKey(), new NumberExpression(formula.getValue(), "level"));
        }

        levelXPCache.clear();
        if (!first) {
            levelMatcher.get().reload();
        }
    }

    public LevelData getUserData(Player player) {
        return AuroraAPI.getUser(player.getUniqueId()).getData(LevelData.class);
    }

    public void addXpToPlayer(Player player, double xp) {
        var data = getUserData(player);

        double requiredXpToLevelUp = getRequiredXpForLevelUp(player);
        double newXP = data.getCurrentXP() + xp;

        if (plugin.getConfigManager().getLevelConfig().getXpGainActionBar().getEnabled()) {
            ActionBar.send(player, plugin.getConfigManager().getLevelConfig().getXpGainActionBar().getMessage(),
                    Placeholder.of("{amount}", AuroraAPI.formatNumber(xp)));
        }

        if (newXP < requiredXpToLevelUp) {
            data.setCurrentXP(newXP);
            Bukkit.getGlobalRegionScheduler().run(AuroraLevels.getInstance(),
                    (task) -> Bukkit.getPluginManager().callEvent(new PlayerXpGainEvent(player, xp)));
            return;
        }

        while (newXP >= requiredXpToLevelUp) {
            data.setLevel(data.getLevel() + 1);
            data.setCurrentXP(newXP - requiredXpToLevelUp);

            newXP = data.getCurrentXP();
            requiredXpToLevelUp = getRequiredXpForLevelUp(player);

            rewardPlayer(player, data.getLevel());
            Bukkit.getGlobalRegionScheduler().run(AuroraLevels.getInstance(),
                    (task) -> Bukkit.getPluginManager().callEvent(new PlayerLevelUpEvent(player, data.getLevel())));
        }

        Bukkit.getGlobalRegionScheduler().run(AuroraLevels.getInstance(),
                (task) -> Bukkit.getPluginManager().callEvent(new PlayerXpGainEvent(player, xp)));
    }

    public List<Placeholder<?>> getRewardFormulaPlaceholders(Player player, long level) {
        var config = plugin.getConfigManager().getLevelConfig();
        var formulaPlaceholders = new ArrayList<Placeholder<?>>();

        for (var formula : config.getFormulaPlaceholders().entrySet()) {
            var value = getFormulaValueForLevel(formula.getKey(), level);
            formulaPlaceholders.add(Placeholder.of("{" + formula.getKey() + "}", value));
            formulaPlaceholders.add(Placeholder.of("{" + formula.getKey() + "_int}", ((Double) value).longValue()));
            formulaPlaceholders.add(Placeholder.of("{" + formula.getKey() + "_formatted}", AuroraAPI.formatNumber(value)));
        }

        formulaPlaceholders.add(Placeholder.of("{level}", level));
        formulaPlaceholders.add(Placeholder.of("{level_int}", level));
        formulaPlaceholders.add(Placeholder.of("{level_formatted}", AuroraAPI.formatNumber(level)));

        return formulaPlaceholders;
    }

    private void rewardPlayer(Player player, long level) {
        var config = plugin.getConfigManager().getLevelConfig();


        List<Placeholder<?>> placeholders = getRewardFormulaPlaceholders(player, level);

        placeholders.add(Placeholder.of("{prev_level}", level - 1));
        placeholders.add(Placeholder.of("{prev_level_int}", level - 1));
        placeholders.add(Placeholder.of("{prev_level_formatted}", AuroraAPI.formatNumber(level - 1)));

        var matcher = levelMatcher.get().getBestMatcher(level);

        for (var reward : matcher.getRewards()) {
            reward.execute(player, level, placeholders);
        }

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

            for (var line : messageLines) {
                if (line.equals("component:rewards")) {
                    if (!matcher.getRewards().isEmpty()) {
                        text.append(Text.component(player, config.getDisplayComponents().get("rewards").getTitle(), placeholders));
                    }
                    for (var reward : matcher.getRewards()) {
                        text.append(Component.newline());
                        var display = config.getDisplayComponents().get("rewards").getLine().replace("{reward}", reward.getDisplay(player, placeholders));
                        text.append(Text.component(player, display, placeholders));
                    }
                } else {
                    text.append(Text.component(player, line, placeholders));
                }

                if (!line.equals(messageLines.getLast())) text.append(Component.newline());
            }

            player.sendMessage(text);
        }

        if (config.getLevelUpTitle().getEnabled()) {
            var title = Text.component(player, config.getLevelUpTitle().getTitle(), placeholders);
            var subtitle = Text.component(player, config.getLevelUpTitle().getSubtitle(), placeholders);
            player.showTitle(Title.title(title, subtitle));
        }
    }

    public void setPlayerLevel(Player player, long level) {
        var data = getUserData(player);
        data.setCurrentXP(0);
        if (data.getLevel() == level) return;

        if (data.getLevel() > level) {
            data.setLevel(level);
            Bukkit.getGlobalRegionScheduler().run(AuroraLevels.getInstance(),
                    (task) -> Bukkit.getPluginManager().callEvent(new PlayerLevelUpEvent(player, level)));
            return;
        }

        for (long l = data.getLevel() + 1; l <= level; l++) {
            data.setLevel(l);
            rewardPlayer(player, l);
            Bukkit.getGlobalRegionScheduler().run(AuroraLevels.getInstance(),
                    (task) -> Bukkit.getPluginManager().callEvent(new PlayerLevelUpEvent(player, level)));
        }
    }

    public void setPlayerLevelRaw(Player player, long level) {
        var data = getUserData(player);
        data.setCurrentXP(0);
        data.setLevel(level);
    }

    public double getXpForLevel(long level) {
        return levelXPCache.computeIfAbsent(level,
                (l) -> xpFormula.get().evaluate(Placeholder.of("level", l)));
    }

    public double getFormulaValueForLevel(String formula, long level) {
        return formulaCache.computeIfAbsent(formula, (f) -> new ConcurrentHashMap<>())
                .computeIfAbsent(level, (l) -> formulas.get(formula).evaluate(Placeholder.of("level", l)));
    }

    public double getRequiredXpForLevelUp(Player player) {
        var data = getUserData(player);
        double currentLevelXP = getXpForLevel(data.getLevel());
        double nextLevelXP = getXpForLevel(data.getLevel() + 1);
        return nextLevelXP - currentLevelXP;
    }

    public void correctRewards(Player player) {
        for (var corrector : rewardCorrectors.values()) {
            corrector.correctRewards(this, player);
        }
    }

    @Override
    public Optional<LevelReward> createReward(ConfigurationSection args) {
        if (args == null) return Optional.empty();
        var type = args.getString("type", "command").toLowerCase(Locale.ROOT);
        LevelReward reward;

        try {
            var clazz = rewardTypes.get(type);
            if (clazz == null) return Optional.empty();
            reward = clazz.getDeclaredConstructor().newInstance();
            reward.init(args);
            return Optional.of(reward);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                 NoSuchMethodException e) {
            AuroraLevels.logger().warning("Failed to create reward of type " + type + ": " + e.getMessage());
            return Optional.empty();
        }
    }

    @EventHandler
    public void onUserLoaded(AuroraUserLoadedEvent event) {
        var player = event.getUser().getPlayer();
        if (player == null) return;
        correctRewards(player);
    }
}
