package de.ethria.plotsquerdaddonmultiowner.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MultiOwnerCommand implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Nur Spieler dürfen den Befehl ausführen
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cDieser Befehl kann nur von Spielern ausgeführt werden.");
            return true;
        }
        Player player = (Player) sender;

        if (args.length == 0) {
            player.sendMessage("§eVerwende /multiowner <Subcommand>");
            return true;
        }

        String sub = args[0].toLowerCase();

        switch (sub) {
            case "add":
                if (!player.hasPermission("multiowner.add")) {
                    player.sendMessage("§cDu hast keine Berechtigung für diesen Befehl.");
                    return true;
                }
                // Logik für add
                player.sendMessage("§aAdd-Logik ausführen...");
                return true;

            case "accept":
                if (!player.hasPermission("multiowner.accept")) {
                    player.sendMessage("§cDu hast keine Berechtigung für diesen Befehl.");
                    return true;
                }
                // Logik für accept
                player.sendMessage("§aAccept-Logik ausführen...");
                return true;

            case "deny":
                if (!player.hasPermission("multiowner.deny")) {
                    player.sendMessage("§cDu hast keine Berechtigung für diesen Befehl.");
                    return true;
                }
                // Logik für deny
                player.sendMessage("§aDeny-Logik ausführen...");
                return true;

            case "remove":
                if (!player.hasPermission("multiowner.remove")) {
                    player.sendMessage("§cDu hast keine Berechtigung für diesen Befehl.");
                    return true;
                }
                // Logik für remove
                player.sendMessage("§aRemove-Logik ausführen...");
                return true;

            case "reload":
                if (!(player.hasPermission("multiowner.admin.reload") || player.hasPermission("multiowner.admin.*") || player.hasPermission("multiowner.admin"))) {
                    player.sendMessage("§cDu hast keine Berechtigung für diesen Befehl.");
                    return true;
                }
                // Logik für reload
                player.sendMessage("§aKonfiguration neu geladen.");
                return true;

            case "adminadd":
                if (!(player.hasPermission("multiowner.admin.add") || player.hasPermission("multiowner.admin.*") || player.hasPermission("multiowner.admin"))) {
                    player.sendMessage("§cDu hast keine Berechtigung für diesen Befehl.");
                    return true;
                }
                // Logik für adminadd
                player.sendMessage("§aAdminAdd-Logik ausführen...");
                return true;

            case "adminremove":
                if (!(player.hasPermission("multiowner.admin.remove") || player.hasPermission("multiowner.admin.*") || player.hasPermission("multiowner.admin"))) {
                    player.sendMessage("§cDu hast keine Berechtigung für diesen Befehl.");
                    return true;
                }
                // Logik für adminremove
                player.sendMessage("§aAdminRemove-Logik ausführen...");
                return true;

            default:
                player.sendMessage("§cUnbekannter Subcommand.");
                return true;
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        // Tab-Completion nur für Spieler
        if (!(sender instanceof Player)) {
            return Collections.emptyList();
        }
        Player player = (Player) sender;

        if (args.length == 1) {
            List<String> subcommands = new ArrayList<>();

            if (player.hasPermission("multiowner.add")) subcommands.add("add");
            if (player.hasPermission("multiowner.accept")) subcommands.add("accept");
            if (player.hasPermission("multiowner.deny")) subcommands.add("deny");
            if (player.hasPermission("multiowner.remove")) subcommands.add("remove");
            if (player.hasPermission("multiowner.admin.reload") || player.hasPermission("multiowner.admin.*") || player.hasPermission("multiowner.admin"))
                subcommands.add("reload");
            if (player.hasPermission("multiowner.admin.add") || player.hasPermission("multiowner.admin.*") || player.hasPermission("multiowner.admin"))
                subcommands.add("adminadd");
            if (player.hasPermission("multiowner.admin.remove") || player.hasPermission("multiowner.admin.*") || player.hasPermission("multiowner.admin"))
                subcommands.add("adminremove");

            if (args[0].isEmpty()) {
                return subcommands;
            } else {
                String prefix = args[0].toLowerCase();
                List<String> filtered = new ArrayList<>();
                for (String sub : subcommands) {
                    if (sub.startsWith(prefix)) {
                        filtered.add(sub);
                    }
                }
                return filtered;
            }
        }

        // Beispiel: Spielernamen als Tab-Vervollständigung für Subcommands, die es brauchen
        if (args.length == 2 && (args[0].equalsIgnoreCase("add") || args[0].equalsIgnoreCase("adminadd") || args[0].equalsIgnoreCase("remove") || args[0].equalsIgnoreCase("adminremove"))) {
            List<String> names = new ArrayList<>();
            for (Player p : Bukkit.getOnlinePlayers()) {
                names.add(p.getName());
            }
            if (args[1].isEmpty()) {
                return names;
            } else {
                String prefix = args[1].toLowerCase();
                List<String> filtered = new ArrayList<>();
                for (String name : names) {
                    if (name.toLowerCase().startsWith(prefix)) {
                        filtered.add(name);
                    }
                }
                return filtered;
            }
        }

        return Collections.emptyList();
    }
}