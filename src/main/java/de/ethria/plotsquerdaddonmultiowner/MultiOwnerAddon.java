package de.ethria.plotsquerdaddonmultiowner;

import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.plot.PlotId;
import com.plotsquared.core.plot.PlotArea;
import com.plotsquared.core.location.Location;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class MultiOwnerAddon extends JavaPlugin implements CommandExecutor {

    private final Map<UUID, MultiownerRequest> pendingRequests = new ConcurrentHashMap<>();
    private CoOwnerStorage coOwnerStorage;
    public static MultiOwnerAddon instance;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        String storageType = getConfig().getString("storage-type", "yaml");
        if (storageType.equalsIgnoreCase("mysql")) {
            coOwnerStorage = new MySQLCoOwnerStorage(this);
        } else {
            coOwnerStorage = new YamlCoOwnerStorage(this);
        }
        coOwnerStorage.init();

        List<String> aliases = getConfig().getStringList("command-aliases");
        PluginCommand cmd = this.getCommand("multiowner");
        if (cmd != null) {
            cmd.setExecutor(this);
            cmd.setAliases(aliases);
        }

        // Polling-Task zum Aufräumen von CoOwner-Einträgen
        new BukkitRunnable() {
            @Override
            public void run() {
                cleanupCoOwners();
            }
        }.runTaskTimer(this, 20 * 60, 20 * 60 * 10); // alle 10 Minuten
    }

    @Override
    public void onDisable() {
        coOwnerStorage.close();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) return true;
        Player player = (Player) sender;
        if (args.length < 1) {
            player.sendMessage(getMsg("msg_multiowner_usage"));
            return true;
        }

        org.bukkit.Location bukkitLoc = player.getLocation();
        String world = bukkitLoc.getWorld().getName();
        int x = bukkitLoc.getBlockX();
        int y = bukkitLoc.getBlockY();
        int z = bukkitLoc.getBlockZ();
        Location psLoc = Location.at(world, x, y, z);
        Plot plot = Plot.getPlot(psLoc);

        if (plot == null) {
            player.sendMessage(getMsg("msg_no_plot"));
            return true;
        }

        String sub = args[0].toLowerCase(Locale.ROOT);

        switch (sub) {
            case "add":
                if (args.length != 2) {
                    player.sendMessage(getMsg("msg_usage_request"));
                    return true;
                }
                Player owner = Bukkit.getPlayer(args[1]);
                if (owner == null) {
                    player.sendMessage(getMsg("msg_owner_not_found"));
                    return true;
                }
                if (!plot.getOwner().equals(owner.getUniqueId())) {
                    player.sendMessage(getMsg("msg_not_owner"));
                    return true;
                }
                pendingRequests.put(owner.getUniqueId(),
                        new MultiownerRequest(player.getUniqueId(), plot.getId().toString()));
                owner.sendMessage(getMsg("msg_request_notify")
                        .replace("%player%", player.getName())
                        .replace("%plot%", plot.getId().toString()));
                player.sendMessage(getMsg("msg_request_sent"));
                return true;

            case "accept":
                if (args.length != 2) {
                    player.sendMessage(getMsg("msg_usage_accept"));
                    return true;
                }
                Player requester = Bukkit.getPlayer(args[1]);
                if (requester == null) {
                    player.sendMessage(getMsg("msg_player_not_found"));
                    return true;
                }
                MultiownerRequest req = pendingRequests.get(player.getUniqueId());
                if (req == null || !req.applicant.equals(requester.getUniqueId())) {
                    player.sendMessage(getMsg("msg_no_request"));
                    return true;
                }
                coOwnerStorage.addCoOwner(req.plotId, requester.getUniqueId());
                pendingRequests.remove(player.getUniqueId());
                player.sendMessage(getMsg("msg_accept_success")
                        .replace("%player%", requester.getName())
                        .replace("%plot%", req.plotId));
                requester.sendMessage(getMsg("msg_you_are_coowner")
                        .replace("%plot%", req.plotId));
                return true;

            case "deny":
                if (args.length != 2) {
                    player.sendMessage(getMsg("msg_usage_deny"));
                    return true;
                }
                Player denier = Bukkit.getPlayer(args[1]);
                if (denier == null) {
                    player.sendMessage(getMsg("msg_player_not_found"));
                    return true;
                }
                MultiownerRequest reqDeny = pendingRequests.get(player.getUniqueId());
                if (reqDeny == null || !reqDeny.applicant.equals(denier.getUniqueId())) {
                    player.sendMessage(getMsg("msg_no_request"));
                    return true;
                }
                pendingRequests.remove(player.getUniqueId());
                player.sendMessage(getMsg("msg_deny_owner")
                        .replace("%player%", denier.getName()));
                denier.sendMessage(getMsg("msg_deny_requester"));
                return true;

            case "remove":
                if (args.length != 2) {
                    player.sendMessage(getMsg("msg_usage_remove"));
                    return true;
                }
                if (!plot.getOwner().equals(player.getUniqueId())) {
                    player.sendMessage(getMsg("msg_remove_not_owner"));
                    return true;
                }
                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) {
                    player.sendMessage(getMsg("msg_player_not_found"));
                    return true;
                }
                boolean success = coOwnerStorage.removeCoOwner(plot.getId().toString(), target.getUniqueId());
                if (success) {
                    player.sendMessage(getMsg("msg_remove_success")
                            .replace("%player%", target.getName())
                            .replace("%plot%", plot.getId().toString()));
                    target.sendMessage(getMsg("msg_remove_notify")
                            .replace("%plot%", plot.getId().toString()));
                } else {
                    player.sendMessage(getMsg("msg_remove_not_found")
                            .replace("%player%", target.getName()));
                }
                return true;

            default:
                player.sendMessage(getMsg("msg_multiowner_usage"));
                return true;
        }
    }

    /**
     * Pollt regelmäßig alle gespeicherten Plots und räumt CoOwner auf,
     * falls das Plot nicht mehr existiert oder der Owner sich geändert hat.
     */
    private void cleanupCoOwners() {
        Set<String> plotIds = coOwnerStorage.getAllPlotIdsWithCoOwners();
        for (String plotIdStr : plotIds) {
            PlotId plotId = PlotId.fromString(plotIdStr);
            PlotArea area = PlotSquared.get().getPlotAreaManager().getPlotArea(plotId);
            Plot plot = (area != null) ? area.getPlot(plotId) : null;
            UUID owner = (plot != null) ? plot.getOwner() : null;
            if (plot == null || !coOwnerStorage.isOwnerValid(plotIdStr, owner)) {
                coOwnerStorage.removeAllCoOwners(plotIdStr);
            }
        }
    }

    private static class MultiownerRequest {
        public final UUID applicant;
        public final String plotId;

        public MultiownerRequest(UUID applicant, String plotId) {
            this.applicant = applicant;
            this.plotId = plotId;
        }
    }

    public String getMsg(String key) {
        String msg = getConfig().getString("messages." + key, "&cNachricht nicht definiert: " + key);
        return ChatColor.translateAlternateColorCodes('&', msg);
    }
}