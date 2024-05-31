package gg.auroramc.levels.config;

import gg.auroramc.aurora.api.config.AuroraConfig;
import gg.auroramc.levels.AuroraLevels;
import lombok.Getter;
import lombok.SneakyThrows;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Getter
public class MessageConfig extends AuroraConfig {

    private String reloaded = "&aReloaded configuration!";
    private String levelSet = "&aLevel set to %level% for player %player%!";
    private String levelSetTarget = "&aYour level was set to %level%!";
    private String xpAddedFeedback = "&a%amount% XP added to player %player%!";
    private String dataNotLoadedYet = "&cData for this player hasn't loaded yet, try again later!";
    private String dataNotLoadedYetSelf = "&cYour data isn't loaded yet, please try again later!";
    private String playerOnlyCommand = "&cThis command can only be executed by a player!";
    private String noPermission = "&cYou don't have permission to execute this command!";
    private String invalidSyntax = "&cInvalid command syntax!";
    private String mustBeNumber = "&cArgument must be a number!";
    private String playerNotFound = "&cPlayer not found!";
    private String commandError = "&cAn error occurred while executing this command!";

    public MessageConfig(AuroraLevels plugin, String language) {
        super(getFile(plugin, language));
    }

    private static File getFile(AuroraLevels plugin, String language) {
        return new File(plugin.getDataFolder(), "messages_" + language + ".yml");
    }

    public static void saveDefault(AuroraLevels plugin, String language) {
        if (!getFile(plugin, language).exists()) {
            try {
                plugin.saveResource("messages_" + language + ".yml", false);
            } catch (Exception e) {
                AuroraLevels.logger().warning("Internal message file for language: " + language + " not found! Creating a new one from english...");

                var file = getFile(plugin, language);


                try (InputStream in = plugin.getResource("messages_en.yml")) {
                    Files.copy(in, file.toPath());
                } catch (IOException ex) {
                    AuroraLevels.logger().severe("Failed to create message file for language: " + language);
                    ex.printStackTrace();
                }
            }
        }
    }
}
