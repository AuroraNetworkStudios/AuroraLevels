package gg.auroramc.levels.hooks.worldguard;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.session.MoveType;
import com.sk89q.worldguard.session.Session;
import com.sk89q.worldguard.session.handler.Handler;
import gg.auroramc.aurora.api.AuroraAPI;
import gg.auroramc.aurora.api.message.Chat;
import gg.auroramc.aurora.api.message.Placeholder;
import gg.auroramc.levels.AuroraLevels;
import gg.auroramc.levels.api.data.LevelData;


public class EntryHandler extends Handler {

    private final AuroraLevels plugin;

    public static Factory FACTORY(AuroraLevels plugin) {
        return new Factory(plugin);
    }

    public static class Factory extends Handler.Factory<EntryHandler> {
        private final AuroraLevels plugin;

        public Factory(AuroraLevels plugin) {
            this.plugin = plugin;
        }

        @Override
        public EntryHandler create(Session session) {
            return new EntryHandler(session, plugin);
        }
    }

    protected EntryHandler(Session session, AuroraLevels plugin) {
        super(session);
        this.plugin = plugin;
    }

    @Override
    public boolean testMoveTo(LocalPlayer player, Location from, Location to, ApplicableRegionSet toSet, MoveType moveType) {
        if(getSession().getManager().hasBypass(player, (World) to.getExtent())) return true;

        var user = AuroraAPI.getUser(player.getUniqueId());
        if (!user.isLoaded()) return true;
        var config = plugin.getConfigManager().getMessageConfig();

        var level = user.getData(LevelData.class).getLevel();

        Integer minLevel = toSet.queryValue(player, FlagManager.MIN_LEVEL_FLAG);
        Integer maxLevel = toSet.queryValue(player, FlagManager.MAX_LEVEL_FLAG);

        var bukkitPlayer = BukkitAdapter.adapt(player);

        if (minLevel != null && level <= minLevel) {
            Chat.sendMessage(bukkitPlayer, config.getRegionEnterDenyMinLevel(), Placeholder.of("{level}", level), Placeholder.of("{min-level}", minLevel));
            return false;
        }

        if (maxLevel != null && level >= maxLevel) {
            Chat.sendMessage(bukkitPlayer, config.getRegionEnterDenyMaxLevel(), Placeholder.of("{level}", level), Placeholder.of("{max-level}", maxLevel));
            return false;
        }

        return true;
    }
}
