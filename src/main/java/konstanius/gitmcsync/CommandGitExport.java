package konstanius.gitmcsync;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.jetbrains.annotations.NotNull;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.nio.file.*;
import java.util.Comparator;

import static konstanius.gitmcsync.GitMcSync.*;

public class CommandGitExport implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(!sender.hasPermission("gitsync.export")) {
            sender.sendMessage(getString("no-permission"));
            return true;
        }
        if(args.length != 1) {
            sender.sendMessage(getString("missing-argument"));
            return true;
        }
        String path = plugin.getServer().getWorldContainer().getAbsolutePath().replace("/.", "") + args[0];
        if(!(new File(path)).exists()) {
            sender.sendMessage(getString("invalid-directory"));
            return true;
        }
        if(!Boolean.parseBoolean(getString("authenticate"))) {
            sender.sendMessage(getString("not-authenticated"));
            return true;
        }
        if(busy) {
            sender.sendMessage(getString("busy"));
            return true;
        }
        busy = true;
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                try {
                    FileUtils.deleteDirectory(new File(plugin.getDataFolder().getAbsolutePath().replace("/.", "") + "/RepoTemp"));
//                    Files.walk(Path.of(plugin.getDataFolder().getAbsolutePath().replace("/.", "") + "/RepoTemp"))
//                            .sorted(Comparator.reverseOrder())
//                            .map(Path::toFile)
//                            .forEach(File::delete);
                    Files.createDirectory(Path.of(plugin.getDataFolder().getAbsolutePath().replace("/.", "") + "/RepoTemp"));
                } catch (Exception ignored) {}
                File file = new File(plugin.getDataFolder().getAbsolutePath().replace("/.", "") + "/RepoTemp");
                Git git = Git.cloneRepository()
                        .setURI(getString("repository-url").replace("https://", "https://" + getString("token") + "@"))
                        .setCredentialsProvider(new UsernamePasswordCredentialsProvider(getString("token"), ""))
                        .setDirectory(file)
                        .call();
                Repository repository = git.getRepository();
                String newPath = plugin.getServer().getWorldContainer().getAbsolutePath().replace("/.", "/plugins/GitMcSync/RepoTemp" + args[0]);
                try {
                    Files.createDirectories(Path.of(newPath));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                FileUtils.copyDirectory(new File(path), new File(newPath));
                try {
                    FileUtils.deleteDirectory(new File(newPath + "/plugins/GitMcSync"));
                }
                catch(Exception ignored) {}
                git.add().addFilepattern(".").call();
                RevCommit result = git.commit().setMessage(getString("export-message").replace("%path%", args[0]).replace("%player%", sender.getName())).call();
                git.push()
                        .setCredentialsProvider(new UsernamePasswordCredentialsProvider(getString("token"), ""))
                        .call();
                sender.sendMessage(getString("export-successful"));
                busy = false;
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        });
        return true;
    }
}