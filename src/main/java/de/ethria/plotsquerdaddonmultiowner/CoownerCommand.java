package de.ethria.plotsquerdaddonmultiowner;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.text.SimpleDateFormat;
import java.util.*;

public class CoownerCommand implements CommandExecutor {

    // Map<UUID, PendingRequest> für offene Anfragen (Key = Zielspieler)
    private final Map<UUID, PendingRequest> pendingRequests = new HashMap<>();

    public static class PendingRequest {
        public final UUID requester;
        public final String plotId;
        public final long timestamp;
        public PendingRequest(UUID requester, String plotId) {
            this.requester = requester;
            this.plotId = plotId;
            this.timestamp = System.currentTimeMillis();
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        // Nur Spieler dürfen die Befehle verwenden
        if (!(sender instanceof Player player)) {
            sender.sendMessage(CoownerAddon.getInstance().getMsg("msg_not_player"));
            return true;
        }

        // Mindestens 1 Argument erwartet (subcommand)
        if (args.length < 1) {
            player.sendMessage(CoownerAddon.getInstance().getMsg("msg_usage_default"));
            return true;
        }

        String sub = args[0].toLowerCase(Locale.ROOT);

        switch (sub) {
            case "add": {
                // 1. Prüfen, ob Spieler Owner des aktuellen Plots ist
                String plotId = PlotUtil.getPlotIdFromPlayerLocation(player);
                if (plotId == null) {
                    player.sendMessage(CoownerAddon.getInstance().getMsg("msg_no_plot"));
                    return true;
                }
                UUID owner = PlotUtil.getOwnerFromPlotSquared(plotId);
                if (owner == null || !owner.equals(player.getUniqueId())) {
                    player.sendMessage(CoownerAddon.getInstance().getMsg("msg_only_owner_request"));
                    return true;
                }

                // 2. Plot-Owner in Blickrichtung bestimmen
                Player target = PlotUtil.getPlayerInSight(player, 5);
                if (target == null) {
                    player.sendMessage("§cKein Spieler in Blickrichtung gefunden.");
                    return true;
                }
                String targetPlotId = PlotUtil.getPlotIdFromPlayerLocation(target);
                if (targetPlotId == null) {
                    player.sendMessage("§cDer Spieler steht auf keinem Plot.");
                    return true;
                }
                UUID targetOwner = PlotUtil.getOwnerFromPlotSquared(targetPlotId);
                if (targetOwner == null) {
                    player.sendMessage("§cDer Plot hat keinen Owner.");
                    return true;
                }

                // 3. Anfrage verschicken (nur an den Owner des Ziel-Plots!)
                pendingRequests.put(targetOwner, new PendingRequest(player.getUniqueId(), plotId));
                OfflinePlayer targetOwnerPlayer = Bukkit.getOfflinePlayer(targetOwner);

                // Meldungen senden
                if (targetOwnerPlayer.isOnline()) {
                    ((Player) targetOwnerPlayer).sendMessage(CoownerAddon.getInstance().getMsg("msg_request_received"));
                }
                player.sendMessage(CoownerAddon.getInstance().getMsg("msg_request_sent"));
                break;
            }

            case "accept": {
                // 1. Prüfen, ob Spieler Owner des Plots in dem er steht
                String plotId = PlotUtil.getPlotIdFromPlayerLocation(player);
                if (plotId == null) {
                    player.sendMessage(CoownerAddon.getInstance().getMsg("msg_no_plot"));
                    return true;
                }
                UUID owner = PlotUtil.getOwnerFromPlotSquared(plotId);
                if (owner == null || !owner.equals(player.getUniqueId())) {
                    player.sendMessage(CoownerAddon.getInstance().getMsg("msg_only_owner_accept"));
                    return true;
                }

                // 2. Gibt es eine offene Anfrage für diesen Spieler?
                PendingRequest req = pendingRequests.get(player.getUniqueId());
                if (req == null) {
                    player.sendMessage("§cKeine offene Anfrage vorhanden.");
                    return true;
                }
                // Zeit ggf. prüfen (Timeout)

                // 3. Plot-Merge durchführen (als OP)
                boolean mergeOk = PlotUtil.mergePlots(req.plotId, plotId, player);
                if (mergeOk) {
                    // Nachricht an beide Spieler
                    Player anfragender = Bukkit.getPlayer(req.requester);
                    if (anfragender != null) {
                        anfragender.sendMessage("§aPlots wurden gemerged!");
                    }
                    player.sendMessage("§aPlots wurden erfolgreich gemerged!");
                    // Loggen
                    CoownerAddon.getInstance().getCoownerLog().logMerge(
                            plotId, req.requester, player.getUniqueId(), false
                    );
                } else {
                    player.sendMessage("§cFehler beim Plot-Merge!");
                }
                // Anfrage entfernen
                pendingRequests.remove(player.getUniqueId());
                break;
            }

            case "deny": {
                PendingRequest req = pendingRequests.get(player.getUniqueId());
                if (req == null) {
                    player.sendMessage("§cKeine offene Anfrage vorhanden.");
                    return true;
                }
                Player anfragender = Bukkit.getPlayer(req.requester);
                if (anfragender != null) {
                    anfragender.sendMessage(CoownerAddon.getInstance().getMsg("msg_request_denied"));
                }
                player.sendMessage(CoownerAddon.getInstance().getMsg("msg_request_denied"));
                pendingRequests.remove(player.getUniqueId());
                break;
            }
            case "adminadd": {
                // Beispiel: Teamler merged 2 Plots von unterschiedlichen Spieler
                // TODO: Permission prüfen und Logik ergänzen
                player.sendMessage(CoownerAddon.getInstance().getMsg("msg_admin_add"));
            }
            case "reload": {
                CoownerAddon.getInstance().reloadConfig();
                CoownerAddon.getInstance().loadMessages();
                player.sendMessage(CoownerAddon.getInstance().getMsg("msg_reload"));
                // Konfiguration und Nachrichten neu laden
            }
            case "log": {
                int page = 1;
                int resultsPerPage = 10;
                if (args.length >= 2) {
                    try {
                        page = Integer.parseInt(args[1]);
                        if (page < 1) page = 1;
                    } catch (NumberFormatException e) {
                        // ignorieren und Seite 1 verwenden
                    }
                }

                List<Map<String, Object>> logs = CoownerAddon.getInstance().getCoownerLog().getAllLogs();
                int totalPages = (int)Math.ceil((double)logs.size() / resultsPerPage);
                int fromIndex = (page - 1) * resultsPerPage;
                int toIndex = Math.min(fromIndex + resultsPerPage, logs.size());

                player.sendMessage(CoownerAddon.getInstance().getMsg("msg_log_header") + " Seite " + page + "/" + totalPages);

                for (Map<String, Object> log : logs.subList(fromIndex, toIndex)) {
                    // Zeit formatieren
                    long timestamp = (log.get("timestamp") instanceof Long)
                            ? (Long) log.get("timestamp")
                            : Long.parseLong(String.valueOf(log.get("timestamp")));
                    Date date = new Date(timestamp * 1000L);
                    SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy | HH:mm:ss");
                    String formattedTime = sdf.format(date);

                    // Adminmerge-Text statt true/false
                    String adminmergeText = "spieler";
                    Object adminmergeObj = log.get("adminmerge");
                    if (adminmergeObj instanceof Boolean && (Boolean)adminmergeObj) adminmergeText = "admin";
                    if (adminmergeObj instanceof Number && ((Number)adminmergeObj).intValue() == 1) adminmergeText = "admin";
                    if (adminmergeObj instanceof String && adminmergeObj.equals("true")) adminmergeText = "admin";

                    String logMsg = CoownerAddon.getInstance().getMsg("msg_log_entry")
                            .replace("{plotid}", String.valueOf(log.get("plotid")))
                            .replace("{plot1_name}", String.valueOf(log.get("plot1_name")))
                            .replace("{plot2_name}", String.valueOf(log.get("plot2_name")))
                            .replace("{adminmerge}", adminmergeText)
                            .replace("{timestamp}", formattedTime);

                    player.sendMessage(logMsg);
                }

                if (totalPages > 1) {
                    player.sendMessage("§7Weitere Seiten anzeigen mit: /multiowner log <Seite>");
                }
            }
            default:
                player.sendMessage(CoownerAddon.getInstance().getMsg("msg_unknown_subcommand"));
        }

        return true;
    }

    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return Arrays.asList("add", "accept", "deny", "adminadd", "reload", "log");
        }
        return Collections.emptyList();
    }


}