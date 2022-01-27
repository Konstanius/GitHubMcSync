package konstanius.gitmcsync;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import static konstanius.gitmcsync.GitMcSync.getString;
import static konstanius.gitmcsync.GitMcSync.muteList;

public class CommandGitMute implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(!sender.hasPermission("gitsync.mute")) {
            sender.sendMessage(getString("no-permission"));
            return true;
        }
        if(!muteList.contains((Player) sender)) {
            sender.sendMessage(getString("muted").replace("%status%", "muted"));
            muteList.add((Player) sender);
        }
        else {
            sender.sendMessage(getString("muted").replace("%status%", "unmuted"));
            muteList.remove((Player) sender);
        }
        return true;
    }
}
