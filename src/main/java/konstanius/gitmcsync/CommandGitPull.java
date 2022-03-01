package konstanius.gitmcsync;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.util.Arrays;

import static konstanius.gitmcsync.EventCommit.eventCommit;
import static konstanius.gitmcsync.GitMcSync.getString;
import static konstanius.gitmcsync.GitMcSync.ready;

public class CommandGitPull implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
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
