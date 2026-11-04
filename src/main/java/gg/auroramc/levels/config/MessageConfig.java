package gg.auroramc.levels.config;

import lombok.Getter;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.List;
import java.util.function.Consumer;

@Getter
public class MessageConfig {
    private String reloaded = "&aReloaded configuration!";
    private String levelSet = "&aLevel set to {level} for player {player}!";
    private String info = "&a{player} level is {level} and the current XP is {current_xp}!";
    private String levelSetTarget = "&aYour level was set to {level}!";
    private String xpAddedFeedback = "&a{amount} XP added to player {player}!";
    private String dataNotLoadedYet = "&cData for this player hasn't loaded yet, try again later!";
    private String dataNotLoadedYetSelf = "&cYour data isn't loaded yet, please try again later!";
    private String playerOnlyCommand = "&cThis command can only be executed by a player!";
    private String noPermission = "&cYou don't have permission to execute this command!";
    private String invalidSyntax = "&cInvalid command syntax!";
    private String mustBeNumber = "&cArgument must be a number!";
    private String playerNotFound = "&cPlayer not found!";
    private String commandError = "&cAn error occurred while executing this command!";
    private String regionEnterDenyMinLevel = "&cYou must be at least level {min-level} to enter this region!";
    private String regionEnterDenyMaxLevel = "&cYou must be at most level {max-level} to enter this region!";

    // Steps by config-version, only ever appended, each sets config-version itself
    public static List<Consumer<YamlConfiguration>> migrations() {
        return List.of(
                (yaml) -> {
                    yaml.set("region-enter-deny-min-level", "&cYou must be at least level {min-level} to enter this region!");
                    yaml.set("region-enter-deny-max-level", "&cYou must be at most level {max-level} to enter this region!");
                    yaml.set("config-version", null);
                    yaml.set("config-version", 1);
                },
                (yaml) -> {
                    yaml.set("config-version", null);
                    yaml.set("info", "&a{player} level is {level} and the current XP is {current_xp}!");
                    yaml.set("config-version", 2);
                }
        );
    }
}
