package konstanius.gitmcsync;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import static konstanius.gitmcsync.GitMcSync.*;
import static konstanius.gitmcsync.GitMcSync.log;
import static org.bukkit.Bukkit.getServer;

public class CommandGitMute implements CommandExecutor {
    private final Plugin plugin;

    public CommandGitMute(GitMcSync gitMcSync) {
        plugin = gitMcSync;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        try {
            if(!verifyLicense()) {
                log("Plugin license is invalid. Please contact Konstanius#3698 / eukonstanius@gmail.com to purchase a license.");
                getServer().getPluginManager().disablePlugin(plugin);
            }
        } catch (IOException e) {
            log("Plugin license is invalid. Please contact Konstanius#3698 / eukonstanius@gmail.com to purchase a license.");
            getServer().getPluginManager().disablePlugin(plugin);
        }

        if (!sender.hasPermission("gitsync.mute")) {
            sender.sendMessage(getString("no-permission"));
            return true;
        }
        if (!muteList.contains((Player) sender)) {
            sender.sendMessage(getString("muted").replace("%status%", "muted"));
            muteList.add((Player) sender);
        } else {
            sender.sendMessage(getString("muted").replace("%status%", "unmuted"));
            muteList.remove((Player) sender);
        }
        return true;
    }
}
