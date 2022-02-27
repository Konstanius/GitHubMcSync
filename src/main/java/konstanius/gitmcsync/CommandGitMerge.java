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
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import static konstanius.gitmcsync.ActionMerge.mergeReloads;
import static konstanius.gitmcsync.ActionMerge.mergeFiles;
import static konstanius.gitmcsync.GitMcSync.*;

public class CommandGitMerge implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(!sender.hasPermission("gitsync.merge")) {
            sender.sendMessage(getString("permission-denied"));
            return true;
        }
        if(args.length < 1) {
            sender.sendMessage(getString("missing-argument"));
            return true;
        }
        if(current.equals(old)) {
            sender.sendMessage(getString("outdated-commit"));
            return true;
        }
        if(!args[0].equals(current)) {
            sender.sendMessage(getString("invalid-commit"));
            return true;
        }
        if(!ready) {
            sender.sendMessage(getString("outdated-commit"));
            return true;
        }
        if(busy) {
            sender.sendMessage(getString("busy"));
            return true;
        }
        busy = true;
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            fetchFiles(plugin);
            if(!current.equals(getString("repository-url"))) {
                old = current;
            }
            if(args.length > 1 && ((Arrays.asList(args).contains("-r")) || Arrays.asList(args).contains("-s"))) {
                Path src = Path.of(plugin.getDataFolder().getAbsolutePath() + "/RepoClone");
                mergeFiles(src);
                ready = false;
                sender.sendMessage(getString("successful-commit"));
                busy = false;
            }
            else {
                mergeReloads(plugin, sender);
            }
            if(args.length > 1 && Arrays.asList(args).contains("-r")) {
                Bukkit.getScheduler().runTask(plugin, () -> plugin.getServer().dispatchCommand(sender, "restart"));
            }
            if(sender instanceof Player) {
                try {
                    ((Player) sender).playSound(((Player) sender).getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 2f);
                    Thread.sleep(500);
                    ((Player) sender).playSound(((Player) sender).getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 2f);
                    Thread.sleep(500);
                    ((Player) sender).playSound(((Player) sender).getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 2f);
                } catch (Exception ignored) {}
            }
        });
        return true;
    }

    public static void fetchFiles(Plugin plugin) {

        Path path1 = Path.of(plugin.getDataFolder().getAbsolutePath() + "/RepoOld");
        try {
            Files.walk(path1)
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        } catch (Exception ignored) {}

        try {
            Files.createDirectory(Path.of(plugin.getDataFolder() + "/RepoOld"));
            Files.createDirectory(Path.of(plugin.getDataFolder() + "/RepoClone"));
        } catch(Exception ignored) {}

        List<Path> pathsOld = new ArrayList<>();

        try {
            Files.walkFileTree(Path.of(plugin.getDataFolder().getAbsolutePath() + "/RepoClone"), new FileVisitor<Path>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    pathsOld.add(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) {
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            Files.copy(Path.of(plugin.getDataFolder().getAbsolutePath() + "/RepoClone/"), Path.of(plugin.getDataFolder().getAbsolutePath() + "/RepoOld/"), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Path path = Path.of(plugin.getDataFolder().getAbsolutePath() + "/RepoClone");
        try {
            Files.walk(path)
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        } catch (Exception ignored) {}

        try {
            CloneCommand cloneCommand = Git.cloneRepository();
            cloneCommand.setURI(getString("repository-url").replace("https://", "https://" + getString("token") + "@"));
            if(Boolean.parseBoolean(getString("authenticate"))) {
                cloneCommand.setCredentialsProvider(new UsernamePasswordCredentialsProvider(getString("token"), ""));
            }
            cloneCommand.setDirectory(new File(plugin.getDataFolder().getAbsolutePath() + "/RepoClone"));
            cloneCommand.call();
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            Path pathGit = Path.of(plugin.getDataFolder().getAbsolutePath() + "/RepoClone/plugins/GitMcSync");
            try {
                Files.walk(pathGit)
                        .sorted(Comparator.reverseOrder())
                        .map(Path::toFile)
                        .forEach(File::delete);
            } catch (Exception ignored) {}
            FileUtils.deleteDirectory(new File(plugin.getDataFolder().getAbsolutePath() + "/RepoClone/plugins/GitMcSync"));
        } catch(Exception ignored) {}
        try {
            FileUtils.deleteDirectory(new File(plugin.getDataFolder().getAbsolutePath() + "/RepoClone/.git"));
            (new File(plugin.getDataFolder().getAbsolutePath() + "/RepoClone/.git")).delete();
            (new File(plugin.getDataFolder().getAbsolutePath() + "/RepoClone/.gitignore")).delete();
            (new File(plugin.getDataFolder().getAbsolutePath() + "/RepoClone/README.md")).delete();
        } catch(Exception ignored) {}

        List<Path> pathsNew = new ArrayList<>();
        try {
            Files.walkFileTree(Path.of(plugin.getDataFolder().getAbsolutePath() + "/RepoClone"), new FileVisitor<Path>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    pathsNew.add(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) {
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

        for(Path p: pathsOld) {
            if(!pathsNew.contains(p)) {
                (new File(String.valueOf(p).replace("plugins/GitMcSync/RepoClone/", ""))).delete();
                log("File: " + p.toString().replace(plugin.getDataFolder().getAbsolutePath() + "/RepoClone", "") + " has been deleted");
            }
        }
        for(Path p: pathsNew) {
            if(!pathsOld.contains(p)) {
                log("File: " + p.toString().replace(plugin.getDataFolder().getAbsolutePath() + "/RepoClone", "") + " has been created");
            }
        }
    }
}
