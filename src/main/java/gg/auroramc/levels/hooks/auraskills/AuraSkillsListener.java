package gg.auroramc.levels.hooks.auraskills;

import dev.aurelium.auraskills.api.event.user.UserLoadEvent;
import gg.auroramc.levels.AuroraLevels;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.concurrent.CompletableFuture;

public class AuraSkillsListener implements Listener {
    private final AuraSkillsHook hook;

    public AuraSkillsListener(AuraSkillsHook hook) {
        this.hook = hook;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onUserDataLoad(UserLoadEvent event) {
        var player = Bukkit.getPlayer(event.getUser().getUuid());
        if (player == null) return;
        AuroraLevels.logger().debug("AuraSkillsListener: onUserDataLoad called.");
        CompletableFuture.runAsync(() -> hook.getCorrector().correctRewardsWhenLoaded(player, false));
    }
}
