package gg.auroramc.levels.hooks.mythic.reward;

import io.lumine.mythic.core.skills.stats.StatSource;

public class AuroraStatSource implements StatSource {
    @Override
    public boolean removeOnReload() {
        return false;
    }
}
