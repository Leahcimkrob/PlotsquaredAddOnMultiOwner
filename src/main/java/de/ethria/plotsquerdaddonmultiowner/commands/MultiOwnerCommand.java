package de.ethria.plotsquerdaddonmultiowner.commands;

import de.ethria.plotsquerdaddonmultiowner.MultiOwnerAddon;
import com.plotsquared.core.plot.Plot;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class MultiOwnerCommand implements CommandExecutor, TabCompleter {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(MultiOwnerAddon.instance.getMsg("msg_not_player"));
            return true;
        }
        Player player = (Player) sender;
        MultiOwnerAddon plugin = MultiOwnerAddon.instance;

        if (args.length == 0) {
            player.sendMessage(plugin.getMsg("msg_usage_default"));
            return true;
        }

        String cmd = args[0].toLowerCase();

        switch (cmd) {
            case "add":
                if (!player.hasPermission("multiowner.add")) {
                    player.sendMessage(plugin.getMsg("msg_no_permission"));
                    return true;
                }
                if (args.length < 2) {
                    player.sendMessage(plugin.getMsg("msg_usage_add"));
                    return true;
                }
                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) {
                    player.sendMessage(plugin.getMsg("msg_no_such_player"));
                    return true;
                }
                if (target.getUniqueId().equals(player.getUniqueId())) {
                    player.sendMessage(plugin.getMsg("msg_self"));
                    return true;
                }
                Plot plot = plugin.getPlayerStandingPlot(player);
                if (plot == null) {
                    player.sendMessage(plugin.getMsg("msg_no_plot"));
                    return true;
                }
                String plotId = plot.getArea().getWorldName() + ";" + plot.getId().toString();

                // --- Debug-Ausgabe ---
                String worldName = plot.getArea().getWorldName();
                String plotName = plot.getId().toString();
                plugin.getLogger().info("[DEBUG] Weltname: " + worldName + " | PlotId: " + plotName);
                plugin.getLogger().info("[DEBUG] PlotOwner-UUID: " + plot.getOwner() + " | Name: " + Bukkit.getOfflinePlayer(plot.getOwner()).getName());

                // Neue Owner-Prüfung direkt am Plot-Objekt:
                if (!plot.getOwner().equals(player.getUniqueId())) {
                    player.sendMessage("Du bist nicht der Owner dieses Plots!");
                    return true;
                }

                if (plugin.getCoOwnerStorage().getCoOwners(plotId).contains(target.getUniqueId())) {
                    player.sendMessage(plugin.getMsg("msg_already_coowner"));
                    return true;
                }
                plugin.getCoOwnerStorage().addCoOwner(plotId, target.getUniqueId());
                player.sendMessage(plugin.getMsg("msg_coowner_added").replace("{player}", target.getName()));
                target.sendMessage(plugin.getMsg("msg_you_are_coowner").replace("{plot}", plotId));
                return true;

            case "remove":
                if (!player.hasPermission("multiowner.remove")) {
                    player.sendMessage(plugin.getMsg("msg_no_permission"));
                    return true;
                }
                if (args.length < 2) {
                    player.sendMessage(plugin.getMsg("msg_usage_remove"));
                    return true;
                }
                Player remTarget = Bukkit.getPlayer(args[1]);
                if (remTarget == null) {
                    player.sendMessage(plugin.getMsg("msg_no_such_player"));
                    return true;
                }
                Plot remPlot = plugin.getPlayerStandingPlot(player);
                if (remPlot == null) {
                    player.sendMessage(plugin.getMsg("msg_no_plot"));
                    return true;
                }
                String remPlotId = remPlot.getArea().getWorldName() + ";" + remPlot.getId().toString();

                // Owner-Prüfung direkt am Plot-Objekt:
                if (!remPlot.getOwner().equals(player.getUniqueId())) {
                    player.sendMessage("Du bist nicht der Owner dieses Plots!");
                    return true;
                }

                if (!plugin.getCoOwnerStorage().getCoOwners(remPlotId).contains(remTarget.getUniqueId())) {
                    player.sendMessage(plugin.getMsg("msg_not_coowner"));
                    return true;
                }
                plugin.getCoOwnerStorage().removeCoOwner(remPlotId, remTarget.getUniqueId());
                player.sendMessage(plugin.getMsg("msg_coowner_removed").replace("{player}", remTarget.getName()));
                remTarget.sendMessage(plugin.getMsg("msg_you_are_no_longer_coowner").replace("{plot}", remPlotId));
                return true;

            case "reload":
                if (!player.hasPermission("multiowner.reload")) {
                    player.sendMessage(plugin.getMsg("msg_no_permission"));
                    return true;
                }
                plugin.reloadConfig();
                plugin.loadMessages();
                player.sendMessage(plugin.getMsg("msg_reload"));
                return true;

            // Admin-Commands analog, z.B.:
            case "adminadd":
                if (!player.hasPermission("multiowner.admin.add")) {
                    player.sendMessage(plugin.getMsg("msg_no_permission"));
                    return true;
                }
                if (args.length < 2) {
                    player.sendMessage(plugin.getMsg("msg_usage_adminadd"));
                    return true;
                }
                // Bei adminadd KEINE Owner-Prüfung! (Admin darf unabhängig vom Owner CoOwner setzen)
                // Restliche Logik ...
                return true;

            case "adminremove":
                if (!player.hasPermission("multiowner.admin.remove")) {
                    player.sendMessage(plugin.getMsg("msg_no_permission"));
                    return true;
                }
                if (args.length < 2) {
                    player.sendMessage(plugin.getMsg("msg_usage_adminremove"));
                    return true;
                }
                // Bei adminremove KEINE Owner-Prüfung! (Admin darf unabhängig vom Owner CoOwner entfernen)
                // Restliche Logik ...
                return true;

            default:
                // Unbekannter Befehl: Admin Usage, falls Permission, sonst Default
                if (player.hasPermission("multiowner.admin.add") || player.hasPermission("multiowner.admin.remove")) {
                    player.sendMessage(plugin.getMsg("msg_usage_admin"));
                } else {
                    player.sendMessage(plugin.getMsg("msg_usage_default"));
                }
                return true;
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player)) return Collections.emptyList();
        List<String> subs = new ArrayList<>();
        if (args.length == 1) {
            subs.add("add");
            subs.add("remove");
            subs.add("accept");
            subs.add("deny");
            subs.add("adminadd");
            subs.add("adminremove");
            subs.add("reload");
            return subs;
        }
        return Collections.emptyList();
    }
}