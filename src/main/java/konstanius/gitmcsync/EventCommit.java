package konstanius.gitmcsync;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.json.JSONObject;

import java.util.Objects;

import static konstanius.gitmcsync.GitMcSync.*;


public class EventCommit {
    public static void eventCommit(JSONObject json) {
        if(!json.getString("ref").equals("refs/heads/main")) {
            return;
        }
        ready = true;
        String pusher = json.getJSONObject("pusher").getString("name");
        StringBuilder message = new StringBuilder(json.getJSONObject("head_commit").getString("message"));
        current = json.getString("compare");
        TextComponent tc = new TextComponent(getString("commit-accept"));
        tc.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/gitmerge " + current));
        tc.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(getString("commit-accept-hover"))));
        String command = "";
        if(message.toString().startsWith("/") && message.toString().contains(" / ")) {
            String[] array = message.toString().split("/");
            command = array[1];
            message = new StringBuilder();
            for(int i = 2; i < array.length; i++) {
                message.append(array[i]);
                if(!(i + 1 == array.length)) {
                    message.append("/");
                }
            }
        }
        String mess = message.toString().replaceFirst(" ", "");
        log("========= Commit detected =========");
        log("Pusher : " + pusher);
        log("Message: " + mess);
        log("Changes: " + current);
        log("===================================");
        TextComponent tc2 = new TextComponent(getString("commit-changes"));
        tc2.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL,  current));
        tc2.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(getString("commit-changes-hover").replace("%link%", current))));
        boolean auto = false;
        if(Objects.requireNonNull(config.getList("op-pushers")).contains(pusher)) {
            String finalCommand = command;
            Bukkit.getScheduler().runTask(plugin, () -> {
                Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), finalCommand);
            });
            auto = true;
        }
        for(Player p: Bukkit.getOnlinePlayers()) {
            if(p.hasPermission("gitsync.notify") && !muteList.contains(p)) {
                p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 2f);
                p.sendMessage(getString("commit-header"));
                p.sendMessage(getString("commit-name").replace("%name%", pusher));
                p.sendMessage(getString("commit-message").replace("%message%", mess));
                p.spigot().sendMessage(tc2);
                if(auto) {
                    p.sendMessage(getString("commit-auto").replace("%command%", command));
                }
                else {
                    p.spigot().sendMessage(tc);
                }
                p.sendMessage(getString("commit-footer"));
            }
        }
    }
}
