package gg.auroramc.levels.api.leveler;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import gg.auroramc.aurora.api.util.NamespacedId;
import gg.auroramc.levels.AuroraLevels;
import gg.auroramc.levels.api.reward.LevelReward;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class LevelMatcher {
    private final AuroraLevels plugin;
    private final Map<NamespacedId, Class<? extends LevelReward>> rewardTypes = new ConcurrentHashMap<>();
    @Getter
    private final List<Matcher> matchers = Lists.newCopyOnWriteArrayList();
    @Getter
    private final Map<Long, Matcher> customMatchers = Maps.newConcurrentMap();


    public LevelMatcher(AuroraLevels plugin) {
        this.plugin = plugin;
        // Wait for hooks and 3rd party plugins to load before constructing the matchers
        // This way we can ensure that every reward type is registered
        Bukkit.getGlobalRegionScheduler().runDelayed(plugin, (task) -> {
            reload();
            AuroraLevels.logger().info("All level matchers have been loaded.");
        }, 1L);
    }

    public void reload() {
        var config = plugin.getConfigManager().getLevelConfig();

        matchers.clear();
        customMatchers.clear();

        matchers.addAll(config.getLevelMatchers().values().stream()
                .sorted((a, b) -> Integer.compare(b.getPriority(), a.getPriority()))
                .map((m) -> new Matcher(m.getInterval(), m.getPriority(), createRewards(m.getRewards())))
                .toList());

        for (var cLevel : config.getCustomLevels().entrySet()) {
            customMatchers.put(cLevel.getKey(), new Matcher(0, 0, createRewards(cLevel.getValue().getRewards())));
        }

    }

    private List<LevelReward> createRewards(ConfigurationSection rewards) {
        if(rewards == null) return new ArrayList<>();

        return rewards.getKeys(false).stream().map(rewards::getConfigurationSection)
                .map(this::createReward)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
    }

    private Optional<LevelReward> createReward(ConfigurationSection args) {
        if (args == null) return Optional.empty();
        var type = NamespacedId.fromDefault(args.getString("type", "command").toLowerCase(Locale.ROOT));
        LevelReward reward;

        try {
            var clazz = rewardTypes.get(type);
            if (clazz == null) {
                AuroraLevels.logger().warning("Failed to create reward of type " + type + ": Reward type not found");
                return Optional.empty();
            }
            reward = clazz.getDeclaredConstructor().newInstance();
            reward.init(args);
            return Optional.of(reward);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                 NoSuchMethodException e) {
            AuroraLevels.logger().warning("Failed to create reward of type " + type + ": " + e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Get the best matcher for the given level
     *
     * @param level level to get the matcher for
     * @return the best matcher for the given level
     */
    public Matcher getBestMatcher(long level) {
        if (customMatchers.containsKey(level)) {
            return customMatchers.get(level);
        }

        for (var matcher : matchers) {
            if (matcher.matches(level)) {
                return matcher;
            }
        }

        return null;
    }

    /**
     * Register a reward type.
     * This method needs to be called in the onEnable method.
     * If it is called later, the reward for this type won't be constructed until
     * the {@link LevelMatcher#reload()} method is called.
     *
     * @param id    id of the reward type
     * @param clazz implementation of the reward type
     */
    public void registerRewardType(NamespacedId id, Class<? extends LevelReward> clazz) {
        rewardTypes.put(id, clazz);
    }
}
