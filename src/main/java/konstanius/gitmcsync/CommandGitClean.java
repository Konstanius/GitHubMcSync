package konstanius.gitmcsync;

import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.io.File;

import static konstanius.gitmcsync.GitMcSync.*;

public class CommandGitClean implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
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
