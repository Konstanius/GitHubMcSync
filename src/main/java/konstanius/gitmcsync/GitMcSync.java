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
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.sql.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static konstanius.gitmcsync.EventCommit.eventCommit;

public final class GitMcSync extends JavaPlugin {
    public static String license;
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
//        license = getString("license");
//        try {
//            if(!verifyLicense()) {
//                log("Plugin license is invalid. Please contact Konstanius#3698 / eukonstanius@gmail.com to purchase a license.");
//                getServer().getPluginManager().disablePlugin(this);
//            }
//        } catch (IOException e) {
//            log("Plugin license is invalid. Please contact Konstanius#3698 / eukonstanius@gmail.com to purchase a license.");
//            getServer().getPluginManager().disablePlugin(this);
//        }

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

        getCommand("gitmerge").setExecutor(new CommandGitMerge(this));
        getCommand("gitpull").setExecutor(new CommandGitPull(this));
        getCommand("gitupgrade").setExecutor(new CommandGitUpgrade(this));
        getCommand("gitmute").setExecutor(new CommandGitMute(this));
        getCommand("gitexport").setExecutor(new CommandGitExport(this));
        getCommand("gitclean").setExecutor(new CommandGitClean(this));

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
        logger.log(Level.INFO, "§2§l" + l + "§r");
    }

    public static boolean verifyLicense() throws IOException {
        return true;
//        try {
//            MessageDigest md = null;
//            try {
//                md = MessageDigest.getInstance("SHA-512");
//            } catch (Exception e) {
//                return false;
//            }
//            byte[] messageDigest = md.digest(license.getBytes());
//            BigInteger no = new BigInteger(1, messageDigest);
//            String licenseHash = no.toString(16);
//            while (licenseHash.length() < 32) {
//                licenseHash = "0" + licenseHash;
//            }
//            URL gitURL = new URL("https://raw.githubusercontent.com/Konstanius/Konstanius/main/GitMcSync-Hashes.txt");
//            BufferedReader in = new BufferedReader(new InputStreamReader(gitURL.openStream()));
//            String inputLine;
//            while ((inputLine = in.readLine()) != null) {
//                if(inputLine.equals(licenseHash)) {
//                    in.close();
//                    Class.forName("com.mysql.jdbc.Driver");
//                    Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
//                    long timeNow = cal.getTimeInMillis();
//                    Connection newConnection = DriverManager.getConnection("jdbc:mysql://remotemysql.com:3306/4eECDcb5Lx", "4eECDcb5Lx", "vXD3rnQxa4");
//                    Statement statement = newConnection.createStatement();
//                    ResultSet result = statement.executeQuery("SELECT * FROM Licensing WHERE Hash = '" + licenseHash + "';");
//                    String ip = new BufferedReader(new InputStreamReader(new URL("http://checkip.amazonaws.com").openStream())).readLine();
//                    long time = 0;
//                    String recentIP = "";
//                    while(result.next()) {
//                        if(result.getLong("Time") > time) {
//                            time = result.getLong("Time");
//                            recentIP = result.getString("IP");
//                        }
//                    }
//                    if(recentIP.equals(ip) || time <= timeNow - 3600000) {
//                        statement.executeUpdate("INSERT INTO Licensing (Hash, IP, Time) VALUES ('" + licenseHash + "','" + ip + "','" + timeNow + "');");
//                        return true;
//                    }
//                    else {
//                        return false;
//                    }
//                }
//            }
//            in.close();
//            return false;
//        } catch (Exception e) {
//            e.printStackTrace();
//            return false;
//        }
    }
}
