package gg.auroramc.levels.api.leveler;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import gg.auroramc.levels.AuroraLevels;
import gg.auroramc.levels.leveler.Matcher;
import gg.auroramc.levels.leveler.PlayerLeveler;
import lombok.Getter;

import java.util.List;
import java.util.Map;

@Getter
public class LevelMatcher {
    private final List<Matcher> matchers = Lists.newCopyOnWriteArrayList();
    private final Map<Long, Matcher> customMatchers = Maps.newConcurrentMap();
    private final Leveler leveler;

    public LevelMatcher(Leveler leveler) {
        this.leveler = leveler;
        reload();
    }

    public void reload() {
        var plugin = AuroraLevels.getInstance();
        var config = plugin.getConfigManager().getLevelConfig();

        matchers.clear();
        customMatchers.clear();

        matchers.addAll(config.getLevelMatchers().values().stream()
                .sorted((a, b) -> Integer.compare(b.getPriority(), a.getPriority()))
                .map((m) -> new Matcher(m.getInterval(), m.getPriority(), m.getRewards(), leveler))
                .toList());

        for(var cLevel : config.getCustomLevels().entrySet()) {
            customMatchers.put(cLevel.getKey(), new Matcher(0, 0, cLevel.getValue().getRewards(), leveler));
        }

    }

    /**
     * Get the best matcher for the given level
     *
     * @param level     level to get the matcher for
     * @return          the best matcher for the given level
     */
    public Matcher getBestMatcher(long level) {
        if(customMatchers.containsKey(level)) {
            return customMatchers.get(level);
        }

        for (var matcher : matchers) {
            if (matcher.matches(level)) {
                return matcher;
            }
        }

        return null;
    }
}
