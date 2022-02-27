package konstanius.gitmcsync;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

import static konstanius.gitmcsync.GitMcSync.*;

public class CommandGitClean implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(!sender.hasPermission("gitsync.clean")) {
            sender.sendMessage(getString("no-permission"));
            return true;
        }
        if(busy) {
            sender.sendMessage(getString("busy"));
            return true;
        }
        busy = true;
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            Path old = Path.of(plugin.getDataFolder().getAbsolutePath() + "/RepoOld");
            try {
                Files.walk(old)
                        .sorted(Comparator.reverseOrder())
                        .map(Path::toFile)
                        .forEach(File::delete);
            } catch (Exception ignored) {}
            Path clone = Path.of(plugin.getDataFolder().getAbsolutePath() + "/RepoClone");
            try {
                Files.walk(clone)
                        .sorted(Comparator.reverseOrder())
                        .map(Path::toFile)
                        .forEach(File::delete);
            } catch (Exception ignored) {}
            Path temp = Path.of(plugin.getDataFolder().getAbsolutePath() + "/RepoTemp");
            try {
                Files.walk(temp)
                        .sorted(Comparator.reverseOrder())
                        .map(Path::toFile)
                        .forEach(File::delete);
            } catch (Exception ignored) {}
            try {
                Files.delete(Path.of(plugin.getDataFolder() + "/RepoOld"));
                Files.delete(Path.of(plugin.getDataFolder() + "/RepoClone"));
                Files.delete(Path.of(plugin.getDataFolder() + "/RepoTemp"));
            } catch(Exception ignored) {}
            sender.sendMessage(getString("clean-successful"));
            busy = false;
        });
        return true;
    }
}
