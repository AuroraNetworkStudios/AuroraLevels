package gg.auroramc.levels.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import gg.auroramc.aurora.api.AuroraAPI;
import gg.auroramc.aurora.api.message.Chat;
import gg.auroramc.aurora.api.message.Placeholder;
import gg.auroramc.levels.AuroraLevels;
import gg.auroramc.levels.menu.LevelMenu;
import gg.auroramc.levels.menu.MilestonesMenu;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandAlias("%levelAlias")
public class LevelCommand extends BaseCommand {
    private final AuroraLevels plugin;

    public LevelCommand(AuroraLevels plugin) {
        this.plugin = plugin;
    }

    @Default
    @Description("Opens the level menu")
    @CommandPermission("aurora.levels.use")
    public void onMenu(Player player) {
        if (!AuroraAPI.getUser(player.getUniqueId()).isLoaded()) {
            Chat.sendMessage(player, plugin.getConfigManager().getMessageConfig().getDataNotLoadedYetSelf());
            return;
        }
        new LevelMenu(plugin, player).open();
    }

    @Subcommand("%milestonesAlias")
    @Description("Opens the milestones menu")
    @CommandPermission("aurora.levels.milestones.use")
    public void onMilestonesMenu(Player player) {
        if (!AuroraAPI.getUser(player.getUniqueId()).isLoaded()) {
            Chat.sendMessage(player, plugin.getConfigManager().getMessageConfig().getDataNotLoadedYetSelf());
            return;
        }
        new MilestonesMenu(plugin, player).open();
    }

    @Subcommand("%setAlias")
    @Description("Sets the level of a player")
    @CommandCompletion("@range:1-1000 @players true|false")
    @CommandPermission("aurora.levels.admin.set")
    public void onSetLevel(CommandSender sender, int level, @Flags("other") Player player, @Default("false") boolean silent) {
        if (!AuroraAPI.getUser(player.getUniqueId()).isLoaded()) {
            Chat.sendMessage(sender, plugin.getConfigManager().getMessageConfig().getDataNotLoadedYet());
            return;
        }
        plugin.getLeveler().setPlayerLevel(player, level);

        if (!silent) {
            Chat.sendMessage(sender, plugin.getConfigManager().getMessageConfig().getLevelSet(),
                    Placeholder.of("{level}", level),
                    Placeholder.of("{player}", player.getName()));

            Chat.sendMessage(player, plugin.getConfigManager().getMessageConfig().getLevelSetTarget(),
                    Placeholder.of("{level}", level));
        }
    }

    @Subcommand("%setrawAlias")
    @Description("Sets the experience of a player without changing total experience structure")
    @CommandCompletion("@range:0-1000 @players true|false")
    @CommandPermission("aurora.levels.admin.setraw")
    public void onSetRaw(CommandSender sender, int level, @Flags("other") Player player, @Default("false") boolean silent) {
        if (!AuroraAPI.getUser(player.getUniqueId()).isLoaded()) {
            Chat.sendMessage(sender, plugin.getConfigManager().getMessageConfig().getDataNotLoadedYet());
            return;
        }
        plugin.getLeveler().setPlayerLevelRaw(player, level);

        if (!silent) {
            Chat.sendMessage(sender, plugin.getConfigManager().getMessageConfig().getLevelSet(),
                    Placeholder.of("{level}", level),
                    Placeholder.of("{player}", player.getName()));

            Chat.sendMessage(player, plugin.getConfigManager().getMessageConfig().getLevelSetTarget(),
                    Placeholder.of("{level}", level));
        }
    }

    @Subcommand("%addxpAlias")
    @Description("Adds experience to a player")
    @CommandCompletion("@range:1-1000 @players true|false")
    @CommandPermission("aurora.levels.admin.addxp")
    public void onAddXp(CommandSender sender, double xp, @Flags("other") Player player, @Default("false") boolean silent) {
        if (!AuroraAPI.getUser(player.getUniqueId()).isLoaded()) {
            Chat.sendMessage(sender, plugin.getConfigManager().getMessageConfig().getDataNotLoadedYet());
            return;
        }

        double added = plugin.getLeveler().addXpToPlayer(player, xp);

        if (!silent) {
            Chat.sendMessage(sender,
                    plugin.getConfigManager().getMessageConfig().getXpAddedFeedback(),
                    Placeholder.of("{amount}", added),
                    Placeholder.of("{player}", player.getName()));
        }
    }

    @Subcommand("reload")
    @Description("Reloads level configurations")
    @CommandPermission("aurora.levels.admin.reload")
    public void onReload(CommandSender sender) {
        plugin.reload();
        Chat.sendMessage(sender, plugin.getConfigManager().getMessageConfig().getReloaded());
    }
}