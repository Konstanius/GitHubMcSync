package konstanius.gitmcsync;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

import static konstanius.gitmcsync.ActionMerge.mergeFiles;
import static konstanius.gitmcsync.GitMcSync.*;

public class CommandGitUpgrade implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(!sender.hasPermission("gitsync.merge")) {
            sender.sendMessage(getString("permission-denied"));
            return true;
        }
        if(busy) {
            sender.sendMessage(getString("busy"));
            return true;
        }
        if(args.length == 0) {
            sender.sendMessage(getString("missing-argument"));
            return true;
        }
        Plugin pl = null;
        for(Plugin p: plugin.getServer().getPluginManager().getPlugins()) {
            if(args[0].toLowerCase().matches(p.getName().toLowerCase())) {
                pl = p;
                break;
            }
        }
        if(pl == null) {
            sender.sendMessage(getString("invalid-plugin"));
            return true;
        }
        busy = true;
        Plugin finalPl = pl;
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            CommandGitMerge.fetchFiles(plugin);
            Path src = Path.of(plugin.getDataFolder().getAbsolutePath() + "/RepoClone/plugins/" + finalPl.getName());
            mergeFiles(src);
            Bukkit.getScheduler().runTask(plugin, () -> {
                if(args.length > 1) {
                    if(args[1].matches("-r")) {
                        plugin.getServer().dispatchCommand(sender, "restart");
                    }
                    else if(args[1].matches("-b")) {
                        plugin.getServer().dispatchCommand(sender, "biletools:bile reload " + finalPl.getName());
                    }
                    log("False");
                }
                else {
                    plugin.getServer().dispatchCommand(sender, finalPl.getName() + " reload");
                }
                sender.sendMessage(getString("successful-plugin"));
                busy = false;
                if(sender instanceof Player) {
                    try {
                        ((Player) sender).playSound(((Player) sender).getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 2f);
                        Thread.sleep(300);
                        ((Player) sender).playSound(((Player) sender).getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 2f);
                        Thread.sleep(300);
                        ((Player) sender).playSound(((Player) sender).getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 2f);
                    } catch (Exception ignored) {}
                }
            });
        });
        return true;
    }
}