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
            sender.sendMessage("§cNur Spieler können diesen Befehl nutzen.");
            return true;
        }

        if (args.length < 2) {
            player.sendMessage("§cVerwendung: /multiowner request <Spieler> oder /multiowner accept <Spieler>");
            return true;
        }

        String sub = args[0].toLowerCase(Locale.ROOT);
        String targetName = args[1];
        OfflinePlayer targetPlayer = Bukkit.getOfflinePlayer(targetName);

        if (sub.equals("request")) {
            if (!targetPlayer.hasPlayedBefore() && !targetPlayer.isOnline()) {
                player.sendMessage("§cDer Spieler " + targetName + " wurde nicht gefunden.");
                return true;
            }

            // Plot unter den Füßen finden
            String plotId = getPlotIdOfPlayer(player);
            if (plotId == null) {
                player.sendMessage("§cDu stehst auf keinem Plot.");
                return true;
            }

            // Owner-Check (nur der Owner darf anfragen)
            if (!PlotUtil.isOwner(plotId, player.getUniqueId())) {
                player.sendMessage("§cNur der Plot-Owner kann eine Anfrage stellen.");
                return true;
            }

            // Optional: Du kannst hier PendingRequests-Logik hinzufügen
            player.sendMessage("§aAnfrage an " + targetName + " gestellt. (Akzeptieren mit /multiowner accept " + player.getName() + ")");
            // Anfrage-Logik implementieren, falls nötig (z.B. PendingRequests)

        } else if (sub.equals("accept")) {
            if (!targetPlayer.hasPlayedBefore() && !targetPlayer.isOnline()) {
                player.sendMessage("§cDer Spieler " + targetName + " wurde nicht gefunden.");
                return true;
            }

            // Plot unter den Füßen finden
            String plotId = getPlotIdOfPlayer(player);
            if (plotId == null) {
                player.sendMessage("§cDu stehst auf keinem Plot.");
                return true;
            }

            // Owner-Check (nur der Owner darf akzeptieren)
            if (!PlotUtil.isOwner(plotId, player.getUniqueId())) {
                player.sendMessage("§cNur der Plot-Owner kann akzeptieren.");
                return true;
            }

            // Log-Eintrag schreiben
            UUID applicantUuid = targetPlayer.getUniqueId();
            UUID acceptorUuid = player.getUniqueId();
            CoownerAddon.getInstance().getCoownerLog().logMerge(plotId, applicantUuid, acceptorUuid);
            player.sendMessage("§aDu hast die Multiowner-Anfrage von " + targetName + " akzeptiert und geloggt.");
        } else if (sub.equals("log")) {
            // Optional: Zeige letzten Einträge
            List<Map<String, Object>> logs = CoownerAddon.getInstance().getCoownerLog().getAllLogs();
            player.sendMessage("§e--- CoownerLog ---");
            for (Map<String, Object> log : logs.stream().limit(10).collect(Collectors.toList())) {
                player.sendMessage("Plot: " + log.get("plotid") + " | Antrag: " + log.get("applicant_name") +
                        " | Akzeptiert von: " + log.get("acceptor_name") + " | Zeit: " + log.get("timestamp"));
            }
        } else {
            player.sendMessage("§cUnbekannter Subbefehl.");
        }

        return true;
    }

    /**
     * Versucht die Plot-ID zu finden, auf der der Spieler steht.
     * Gibt null zurück, wenn auf keinem Plot.
     */
    private String getPlotIdOfPlayer(Player player) {
        // PlotSquared-Nutzung:
        try {
            com.plotsquared.core.location.Location loc =
                    com.plotsquared.core.location.Location.at(
                            player.getWorld().getName(),
                            player.getLocation().getBlockX(),
                            player.getLocation().getBlockY(),
                            player.getLocation().getBlockZ()
                    );
            com.plotsquared.core.plot.Plot plot = com.plotsquared.core.plot.Plot.getPlot(loc);
            if (plot == null) return null;
            String plotId = plot.getArea().getWorldName() + ";" + plot.getId().getX() + "," + plot.getId().getY();
            return plotId;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return Arrays.asList("request", "accept", "log");
        }
        if (args.length == 2 && (args[0].equalsIgnoreCase("request") || args[0].equalsIgnoreCase("accept"))) {
            return Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }
}