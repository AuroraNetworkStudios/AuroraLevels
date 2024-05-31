package gg.auroramc.levels.api.reward;

import gg.auroramc.levels.api.leveler.Leveler;
import org.bukkit.entity.Player;

public interface RewardCorrector {
    void correctRewards(Leveler leveler, Player player);
}
