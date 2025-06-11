package de.ethria.plotsquerdaddonmultiowner;

import de.ethria.plotsquerdaddonmultiowner.commands.MultiOwnerCommand;
import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.location.Location;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.plot.PlotArea;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class MultiOwnerAddon extends JavaPlugin {

    private final Map<UUID, MultiownerRequest> pendingRequests = new ConcurrentHashMap<>();
    private CoOwnerStorage coOwnerStorage;
    public static MultiOwnerAddon instance;

    private FileConfiguration messages;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        saveMessagesFile();

        loadMessages();

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
            MultiOwnerCommand commandHandler = new MultiOwnerCommand();
            cmd.setExecutor(commandHandler);
            cmd.setTabCompleter(commandHandler);
            cmd.setAliases(aliases);
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                cleanupCoOwners();
            }
        }.runTaskTimer(this, 20 * 60, 20 * 60 * 10);
    }

    private void saveMessagesFile() {
        File msgFile = new File(getDataFolder(), "messages.yml");
        if (!msgFile.exists()) {
            saveResource("messages.yml", false);
        }
    }

    public void loadMessages() {
        File msgFile = new File(getDataFolder(), "messages.yml");
        messages = YamlConfiguration.loadConfiguration(msgFile);
    }

    @Override
    public void onDisable() {
        coOwnerStorage.close();
    }

    private void cleanupCoOwners() {
        Set<String> plotIds = coOwnerStorage.getAllPlotIdsWithCoOwners();
        for (String plotIdStr : plotIds) {
            try {
                String[] parts = plotIdStr.split(";", 2);
                if (parts.length != 2) continue;
                String worldName = parts[0];
                String[] coords = parts[1].split(",");
                if (coords.length != 2) continue;
                int px = Integer.parseInt(coords[0].trim());
                int pz = Integer.parseInt(coords[1].trim());

                int plotSize = getPlotSizeFromPlotId(plotIdStr);
                int bx = px * plotSize + plotSize / 2;
                int bz = pz * plotSize + plotSize / 2;

                Location loc = Location.at(worldName, bx, 64, bz);
                Plot plot = Plot.getPlot(loc);
                UUID owner = (plot != null) ? plot.getOwner() : null;

                if (plot == null || !coOwnerStorage.isOwnerValid(plotIdStr, owner)) {
                    coOwnerStorage.removeAllCoOwners(plotIdStr);
                }
            } catch (Exception e) {
                String msg = getMsg("msg_cleanup_error")
                        .replace("{plotid}", plotIdStr)
                        .replace("{error}", e.getMessage() == null ? "unknown" : e.getMessage());
                getLogger().warning(msg);
            }
        }
    }

    public static int getPlotSizeFromPlotId(String plotIdStr) {
        try {
            String[] parts = plotIdStr.split(";", 2);
            if (parts.length != 2) return 32;
            String world = parts[0];
            String[] coords = parts[1].split(",");
            if (coords.length != 2) return 32;
            int px = Integer.parseInt(coords[0].trim());
            int pz = Integer.parseInt(coords[1].trim());

            int fallbackPlotSize = 32;
            int bx = px * fallbackPlotSize + fallbackPlotSize / 2;
            int bz = pz * fallbackPlotSize + fallbackPlotSize / 2;

            Location loc = Location.at(world, bx, 64, bz);
            PlotArea area = PlotSquared.get().getPlotAreaManager().getPlotArea(loc);
            if (area == null) return fallbackPlotSize;

            try {
                Method m = area.getClass().getMethod("getPlotWidth");
                m.setAccessible(true);
                Object result = m.invoke(area);
                if (result instanceof Integer) {
                    return (Integer) result;
                }
                if (result instanceof Number) {
                    return ((Number) result).intValue();
                }
            } catch (Exception e) {
                // fallback
            }
            return fallbackPlotSize;
        } catch (Exception e) {
            return 32;
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
        String msg = messages.getString(key, "&cNachricht nicht definiert: " + key);
        return ChatColor.translateAlternateColorCodes('&', msg);
    }

    public CoOwnerStorage getCoOwnerStorage() {
        return coOwnerStorage;
    }

    public Map<UUID, MultiownerRequest> getPendingRequests() {
        return pendingRequests;
    }

    public Plot getPlayerStandingPlot(Player player) {
        org.bukkit.Location bukkitLoc = player.getLocation();
        String world = bukkitLoc.getWorld().getName();
        int x = bukkitLoc.getBlockX();
        int y = bukkitLoc.getBlockY();
        int z = bukkitLoc.getBlockZ();
        Location psLoc = Location.at(world, x, y, z);
        return Plot.getPlot(psLoc);
    }
}