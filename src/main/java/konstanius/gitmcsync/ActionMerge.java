package konstanius.gitmcsync;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.eclipse.jgit.util.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import static konstanius.gitmcsync.GitMcSync.*;

public class ActionMerge {
    public static void mergeReloads(Plugin plugin, CommandSender sender) {
        Path src = Path.of(plugin.getDataFolder().getAbsolutePath() + "/RepoClone");
        mergeFiles(src);
        ready = false;
        busy = false;
        sender.sendMessage(getString("successful-commit"));

        Plugin[] plugins = plugin.getServer().getPluginManager().getPlugins();
        Arrays.sort(plugins, Comparator.comparing(Plugin::getName));
        for (int i = 0; i < plugins.length; i += 3) {
            Plugin p1 = plugins[i];
            TextComponent tc1 = new TextComponent(getString("reload-format").replace("%plugin%", p1.getName()));
            tc1.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/BileTools:bile reload " + p1.getName()));
            tc1.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(getString("reload-hover").replace("%plugin%", p1.getName()))));
            TextComponent tc2;
            if (plugins.length > i + 1) {
                Plugin p2 = plugins[i + 1];
                tc2 = new TextComponent(getString("reload-format").replace("%plugin%", p2.getName()));
                tc2.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/BileTools:bile reload " + p2.getName()));
                tc2.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(getString("reload-hover").replace("%plugin%", p2.getName()))));
                tc1.addExtra(tc2);
            }
            TextComponent tc3;
            if (plugins.length > i + 2) {
                Plugin p3 = plugins[i + 2];
                tc3 = new TextComponent(getString("reload-format").replace("%plugin%", p3.getName()));
                tc3.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/BileTools:bile reload " + p3.getName()));
                tc3.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(getString("reload-hover").replace("%plugin%", p3.getName()))));
                tc1.addExtra(tc3);
            }
            sender.spigot().sendMessage(tc1);
        }
    }

    public static void mergeFiles(Path src) {
        boolean whitelistFiletypes = Boolean.parseBoolean(getString("whitelist-filetypes"));
        boolean jars = Boolean.parseBoolean(getString("compare-jar"));
        boolean others = Boolean.parseBoolean(getString("compare-other"));
        List<String> fileTypes = config.getStringList("filetypes");
        List<String> pathsCreated = new ArrayList<>();
        List<String> pathsModified = new ArrayList<>();
        List<String> pathsDeleted = new ArrayList<>();

        List<Path> pathsNew = new ArrayList<>();
        try {
            Files.walkFileTree(src, new FileVisitor<Path>() {
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                    return FileVisitResult.CONTINUE;
                }

                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    outerLoop:
                    if (whitelistFiletypes) {
                        for (String type : fileTypes) {
                            if (file.toAbsolutePath().toString().contains(type) || !file.toAbsolutePath().toString().contains(".")) {
                                pathsNew.add(file);
                                break outerLoop;
                            }
                        }
                        (new File(String.valueOf(file))).delete();
                    } else {
                        pathsNew.add(file);
                    }
                    return FileVisitResult.CONTINUE;
                }

                public FileVisitResult visitFileFailed(Path file, IOException exc) {
                    return FileVisitResult.CONTINUE;
                }

                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    Path newPath = Path.of(String.valueOf(dir).replace("plugins/GitMcSync/RepoClone/", ""));
                    try {
                        Files.copy(Path.of(dir.toFile().getAbsolutePath()), newPath, StandardCopyOption.REPLACE_EXISTING);
                    } catch (DirectoryNotEmptyException ignored) {
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

        List<String> pathsNewer = new ArrayList<>();
        for (Path p : pathsNew) {
            Path newPath = Path.of(p.toString().replace("plugins/GitMcSync/RepoClone/", ""));
            try {
                pathsNewer.add(p.toString());
                File f = new File(newPath.toFile().getAbsolutePath());
                if (!f.exists()) {
                    Files.copy(p, newPath, StandardCopyOption.REPLACE_EXISTING);
                    pathsCreated.add(p.toString());
                    continue;
                }
                if (newPath.endsWith(".jar") && jars && Files.size(newPath) == Files.size(p)) {
                    continue;
                }
                if (others && Files.size(newPath) == Files.size(p)) {
                    continue;
                }
                pathsModified.add(p.toString());
                Files.copy(p, newPath, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        List<String> pathsOld = new ArrayList<>();
        src = Path.of(plugin.getDataFolder().getAbsolutePath().replace("/.", "") + "/RepoOld");
        try {
            Files.walkFileTree(src, new FileVisitor<Path>() {
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                    return FileVisitResult.CONTINUE;
                }

                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    outerLoop:
                    if (whitelistFiletypes) {
                        for (String type : fileTypes) {
                            if (file.toAbsolutePath().toString().contains(type) || !file.toAbsolutePath().toString().contains(".")) {
                                pathsOld.add(file.toAbsolutePath().toString());
                                break outerLoop;
                            }
                        }
                        (new File(String.valueOf(file))).delete();
                    } else {
                        pathsOld.add(file.toAbsolutePath().toString());
                    }
                    return FileVisitResult.CONTINUE;
                }

                public FileVisitResult visitFileFailed(Path file, IOException exc) {
                    return FileVisitResult.CONTINUE;
                }

                public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (Boolean.parseBoolean(getString("log-changes"))) {
            for (String s : pathsOld) {
                if (!pathsNewer.contains(s.replace("/plugins/GitMcSync/RepoOld", "/plugins/GitMcSync/RepoClone"))) {
                    pathsDeleted.add(s.replace("/plugins/GitMcSync/RepoOld", ""));
                    try {
                        FileUtils.delete(new File(s.replace("/plugins/GitMcSync/RepoOld", "")));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            for (String s : pathsCreated) {
                log(s.replace("/plugins/GitMcSync/RepoClone", "") + " has been created");
            }
            for (String s : pathsModified) {
                log(s.replace("/plugins/GitMcSync/RepoClone", "") + " has been modified");
            }
            for (String s : pathsDeleted) {
                log(s.replace("/plugins/GitMcSync/RepoOld", "") + " has been deleted");
            }
        }
    }
}
