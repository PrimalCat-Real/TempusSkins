package primalcat.tempusskins;

import net.skinsrestorer.api.SkinsRestorer;
import net.skinsrestorer.api.SkinsRestorerProvider;
import net.skinsrestorer.api.VersionProvider;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Logger;

public class TempusSkins extends JavaPlugin {
    // Skin values for the custom skin example

    private static SkinsRestorer skinsRestorerAPI;
    private final Logger logger = getLogger();

    public static SkinsRestorer getAPI() {
        return skinsRestorerAPI;
    }
    public static TempusSkins plugin;

    @Override
    public void onEnable() {
        logger.info(ChatColor.AQUA + "Hooking into SkinsRestorer API");

        if (!VersionProvider.isCompatibleWith("15")) {
            logger.info("This plugin was made for SkinsRestorer v15, but " + VersionProvider.getVersionInfo() + " is installed. There may be errors!");
        }

        skinsRestorerAPI = SkinsRestorerProvider.get();
        plugin = this;
        this.saveDefaultConfig();

        Util.connect();

        Bukkit.getPluginManager().registerEvents(new EventListener(), this);
        PluginCommand command = this.getCommand("tskins");
        if (command != null) {
            command.setExecutor(new Command());
        }

        logger.info(ChatColor.AQUA + "Registering command");

    }
}
