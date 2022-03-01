package konstanius.gitmcsync;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.json.JSONObject;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import static konstanius.gitmcsync.EventCommit.eventCommit;

public final class GitMcSync extends JavaPlugin {
    public static FileConfiguration config;
    public static Logger logger = Bukkit.getLogger();
    public static HttpServer webServer;
    public static String current = " ";
    public static String old = " ";
    public static boolean ready = false;
    public static boolean busy = false;
    public static List<Player> muteList = new ArrayList<>();
    public static Plugin plugin;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        config = getConfig();
        plugin = this;

        try {
            URL whatismyip = new URL("http://checkip.amazonaws.com");
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    whatismyip.openStream()));
            String ip = in.readLine();
            log("GitMcSync started listening on: http://" + ip + ":" + getString("webhook-port") + "/webhook");
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            Files.createDirectory(Path.of(this.getDataFolder() + "/RepoOld"));
        } catch (Exception ignored) {
        }
        try {
            Files.createDirectory(Path.of(this.getDataFolder() + "/RepoClone"));
        } catch (Exception ignored) {
        }

        getCommand("gitmerge").setExecutor(new CommandGitMerge());
        getCommand("gitpull").setExecutor(new CommandGitPull());
        getCommand("gitupgrade").setExecutor(new CommandGitUpgrade());
        getCommand("gitmute").setExecutor(new CommandGitMute());
        getCommand("gitexport").setExecutor(new CommandGitExport());
        getCommand("gitclean").setExecutor(new CommandGitClean());

        try {
            webServer = HttpServer.create(new InetSocketAddress(Integer.parseInt(getString("webhook-port"))), 0);
        } catch (IOException e) {
            e.printStackTrace();
        }
        webServer.createContext("/webhook", new MyHandler());
        webServer.setExecutor(null);
        webServer.start();
    }

    @Override
    public void onDisable() {
        webServer.stop(0);
    }

    static class MyHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            StringBuilder sb = new StringBuilder();
            InputStream ios = t.getRequestBody();
            int i;
            while ((i = ios.read()) != -1) {
                sb.append((char) i);
            }
            String response = "Response";
            t.sendResponseHeaders(200, response.length());
            OutputStream os = t.getResponseBody();
            os.write(response.getBytes());
            os.close();

            JSONObject json = new JSONObject(sb.toString());
            eventCommit(json);
        }
    }

    public static String getString(String ln) {
        return Objects.requireNonNull(config.getString(ln)).replace("&", "§");
    }

    public static void log(String l) {
        logger.log(Level.INFO, "§2§l" + l);
    }
}
