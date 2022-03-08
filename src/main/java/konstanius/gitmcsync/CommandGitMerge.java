package konstanius.gitmcsync;

import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import static konstanius.gitmcsync.ActionMerge.mergeFiles;
import static konstanius.gitmcsync.ActionMerge.mergeReloads;
import static konstanius.gitmcsync.GitMcSync.*;
import static org.bukkit.Bukkit.getServer;

public class CommandGitMerge implements CommandExecutor {
    private final Plugin plugin;

    public CommandGitMerge(GitMcSync gitMcSync) {
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

        if (!sender.hasPermission("gitsync.merge")) {
            sender.sendMessage(getString("permission-denied"));
            return true;
        }
        if (args.length < 1) {
            sender.sendMessage(getString("missing-argument"));
            return true;
        }
        if (current.equals(old)) {
            sender.sendMessage(getString("outdated-commit"));
            return true;
        }
        if (!args[0].equals(current)) {
            sender.sendMessage(getString("invalid-commit"));
            return true;
        }
        if (!ready) {
            sender.sendMessage(getString("outdated-commit"));
            return true;
        }
        if (busy) {
            sender.sendMessage(getString("busy"));
            return true;
        }
        busy = true;
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            fetchFiles(plugin);
            if (!current.equals(getString("repository-url"))) {
                old = current;
            }
            if (args.length > 1 && ((Arrays.asList(args).contains("-r")) || Arrays.asList(args).contains("-s"))) {
                Path src = Path.of(plugin.getDataFolder().getAbsolutePath() + "/RepoClone");
                mergeFiles(src);
                ready = false;
                sender.sendMessage(getString("successful-commit"));
                busy = false;
            } else {
                mergeReloads(plugin, sender);
            }
            if (args.length > 1 && Arrays.asList(args).contains("-r")) {
                Bukkit.getScheduler().runTask(plugin, () -> plugin.getServer().dispatchCommand(sender, "restart"));
            }
            if (sender instanceof Player) {
                try {
                    ((Player) sender).playSound(((Player) sender).getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 2f);
                    Thread.sleep(500);
                    ((Player) sender).playSound(((Player) sender).getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 2f);
                    Thread.sleep(500);
                    ((Player) sender).playSound(((Player) sender).getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 2f);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        return true;
    }

    public static void fetchFiles(Plugin plugin) {
        try {
            FileUtils.deleteDirectory(new File(plugin.getDataFolder().getAbsolutePath() + "/RepoOld"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            FileUtils.copyDirectory(new File(plugin.getDataFolder().getAbsolutePath() + "/RepoClone"), new File(plugin.getDataFolder().getAbsolutePath() + "/RepoOld"), true);
        } catch (Exception ignored) {
        }

        try {
            FileUtils.deleteDirectory(new File(plugin.getDataFolder().getAbsolutePath() + "/RepoClone"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            Files.createDirectory(Path.of(plugin.getDataFolder() + "/RepoClone"));
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            CloneCommand cloneCommand = Git.cloneRepository();
            cloneCommand.setURI(getString("repository-url").replace("https://", "https://" + getString("token") + "@"));
            if (Boolean.parseBoolean(getString("authenticate"))) {
                cloneCommand.setCredentialsProvider(new UsernamePasswordCredentialsProvider(getString("token"), ""));
            }
            cloneCommand.setDirectory(new File(plugin.getDataFolder().getAbsolutePath() + "/RepoClone"));
            cloneCommand.call();
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            FileUtils.deleteDirectory(new File(plugin.getDataFolder().getAbsolutePath() + "/RepoClone/plugins/GitMcSync"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            FileUtils.deleteDirectory(new File(plugin.getDataFolder().getAbsolutePath() + "/RepoClone/.git"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            (new File(plugin.getDataFolder().getAbsolutePath() + "/RepoClone/.git")).delete();
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            (new File(plugin.getDataFolder().getAbsolutePath() + "/RepoClone/.gitignore")).delete();
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            (new File(plugin.getDataFolder().getAbsolutePath() + "/RepoClone/README.md")).delete();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
