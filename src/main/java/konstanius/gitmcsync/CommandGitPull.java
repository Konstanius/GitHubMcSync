package konstanius.gitmcsync;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Arrays;

import static konstanius.gitmcsync.EventCommit.eventCommit;
import static konstanius.gitmcsync.GitMcSync.*;
import static konstanius.gitmcsync.GitMcSync.log;
import static org.bukkit.Bukkit.getServer;

public class CommandGitPull implements CommandExecutor {
    private final Plugin plugin;

    public CommandGitPull(GitMcSync gitMcSync) {
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

        if (!sender.hasPermission("gitsync.pull")) {
            sender.sendMessage(getString("permission-denied"));
            return true;
        }
        JSONObject json = new JSONObject("{\n" +
                "  \"ref\": \"refs/heads/main\",\n" +
                "  \"pusher\": {\n" +
                "    \"name\": \"" + sender.getName() + "\"\n" +
                "  },\n" +
                "  \"head_commit\": {\n" +
                "    \"message\": \"MANUAL COMMIT\"\n" +
                "  },\n" +
                "  \"compare\": \"" + getString("repository-url") + "\"\n" +
                "}");
        ready = true;
        eventCommit(json);
        if (args.length > 0 && Arrays.asList(args).contains("-ai") && Arrays.asList(args).contains("-r")) {
            sender.getServer().dispatchCommand(sender, "gitmerge " + getString("repository-url") + " -r");
        } else if (args.length > 0 && Arrays.asList(args).contains("-ai") && Arrays.asList(args).contains("-s")) {
            sender.getServer().dispatchCommand(sender, "gitmerge " + getString("repository-url") + " -s");
        } else if (args.length > 0 && Arrays.asList(args).contains("-ai")) {
            sender.getServer().dispatchCommand(sender, "gitmerge " + getString("repository-url"));
        }
        return true;
    }
}
