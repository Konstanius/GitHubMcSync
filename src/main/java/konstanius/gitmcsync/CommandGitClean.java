package konstanius.gitmcsync;

import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;

import static konstanius.gitmcsync.GitMcSync.*;
import static org.bukkit.Bukkit.getServer;

public class CommandGitClean implements CommandExecutor {
    private final Plugin plugin;

    public CommandGitClean(GitMcSync gitMcSync) {
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

        if (!sender.hasPermission("gitsync.clean")) {
            sender.sendMessage(getString("no-permission"));
            return true;
        }
        if (busy) {
            sender.sendMessage(getString("busy"));
            return true;
        }
        busy = true;
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                FileUtils.deleteDirectory(new File(plugin.getDataFolder().getAbsolutePath() + "/RepoOld"));
            } catch (Exception ignored) {
            }
            try {
                FileUtils.deleteDirectory(new File(plugin.getDataFolder().getAbsolutePath() + "/RepoCone"));
            } catch (Exception ignored) {
            }
            try {
                FileUtils.deleteDirectory(new File(plugin.getDataFolder().getAbsolutePath() + "/RepoTemp"));
            } catch (Exception ignored) {
            }
            sender.sendMessage(getString("clean-successful"));
            busy = false;
        });
        return true;
    }
}
