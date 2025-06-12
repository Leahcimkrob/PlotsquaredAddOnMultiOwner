package de.ethria.plotsquerdaddonmultiowner;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

public class CoownerCommand implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(CoownerAddon.getInstance().getMsg("msg_not_player"));
            return true;
        }

        if (args.length < 1) {
            player.sendMessage(CoownerAddon.getInstance().getMsg("msg_usage_default"));
            return true;
        }

        String sub = args[0].toLowerCase(Locale.ROOT);

        if (sub.equals("add")) {
            // Beispiel: Füge den aktuellen Spieler als Coowner hinzu (System bestimmt Ziel)
            // Implementiere hier deine Logik
            player.sendMessage(CoownerAddon.getInstance().getMsg("msg_coowner_added"));
            // TODO: Eigene Logik für das Hinzufügen
        } else if (sub.equals("accept")) {
            // Beispiel: Akzeptiere eine Coowner-Anfrage für den aktuellen Spieler
            // Implementiere hier deine Logik
            player.sendMessage(CoownerAddon.getInstance().getMsg("msg_request_accepted"));
            // TODO: Eigene Logik für das Akzeptieren
        } else if (sub.equals("deny")) {
            // Beispiel: Lehne eine Coowner-Anfrage ab
            player.sendMessage(CoownerAddon.getInstance().getMsg("msg_request_denied"));
            // TODO: Eigene Logik für das Ablehnen
        } else if (sub.equals("adminadd")) {
            // Beispiel: Teamler merged 2 Plots von unterschiedlichen Spieler
            // TODO: Permission prüfen und Logik ergänzen
            player.sendMessage(CoownerAddon.getInstance().getMsg("msg_admin_add"));
        } else if (sub.equals("reload")) {
            // Konfiguration und Nachrichten neu laden
            CoownerAddon.getInstance().reloadConfig();
            CoownerAddon.getInstance().loadMessages();
            player.sendMessage(CoownerAddon.getInstance().getMsg("msg_reload"));
        } else if (sub.equals("log")) {
            List<Map<String, Object>> logs = CoownerAddon.getInstance().getCoownerLog().getAllLogs();
            player.sendMessage(CoownerAddon.getInstance().getMsg("msg_log_header"));
            for (Map<String, Object> log : logs.stream().limit(10).collect(Collectors.toList())) {
                String logMsg = CoownerAddon.getInstance().getMsg("msg_log_entry")
                        .replace("{plotid}", String.valueOf(log.get("plotid")))
                        .replace("{applicant_name}", String.valueOf(log.get("applicant_name")))
                        .replace("{acceptor_name}", String.valueOf(log.get("acceptor_name")))
                        .replace("{timestamp}", String.valueOf(log.get("timestamp")));
                player.sendMessage(logMsg);
            }
        } else {
            player.sendMessage(CoownerAddon.getInstance().getMsg("msg_unknown_subcommand"));
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return Arrays.asList("add", "accept", "deny", "adminadd", "reload", "log");
        }
        return Collections.emptyList();
    }
}