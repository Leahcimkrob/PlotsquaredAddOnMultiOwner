package de.ethria.plotsquerdaddonmultiowner;

import com.plotsquared.core.PlotSquared;
//import com.plotsquared.core.location.Location;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.plot.PlotArea;
import com.plotsquared.core.plot.PlotId;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.UUID;

public class PlotUtil {

    /**
     * Konvertiert eine Bukkit-Location zu einer PlotSquared-Location.
     *
     * @param loc Bukkit-Location
     * @return PlotSquared-Location
     */
    public static com.plotsquared.core.location.Location bukkitToPlotSquaredLocation(org.bukkit.Location loc) {
        return com.plotsquared.core.location.Location.at(
                loc.getWorld().getName(),
                loc.getBlockX(),
                loc.getBlockY(),
                loc.getBlockZ()
        );
    }

    /**
     * Gibt die PlotId (z.B. "world;12,34") an der Position des Spielers zurück.
     *
     * @param player Der Spieler
     * @return PlotId-String oder null, wenn kein Plot gefunden
     */
    public static String getPlotIdFromPlayerLocation(Player player) {
        org.bukkit.Location bukkitLoc = player.getLocation();
        com.plotsquared.core.location.Location psLoc = bukkitToPlotSquaredLocation(bukkitLoc);
        PlotArea area = PlotSquared.get().getPlotAreaManager().getPlotArea(psLoc);
        if (area == null) return null;
        Plot plot = area.getPlot(psLoc);
        if (plot == null) return null;
        PlotId plotId = plot.getId();
        return player.getWorld().getName() + ";" + plotId.getX() + "," + plotId.getY();
    }


    /**
     * Holt den Owner eines Plots anhand der PlotId (Format: "welt;x,z") via PlotSquared.
     *
     * @param plotIdStr z.B. "world;12,34"
     * @return UUID des Owners oder null, falls Plot nicht existiert.
     */
    public static UUID getOwnerFromPlotSquared(String plotIdStr) {
        if (plotIdStr == null) return null;
        try {
            String[] parts = plotIdStr.split(";", 2);
            if (parts.length != 2) return null;
            String worldName = parts[0];
            String[] coords = parts[1].replace(';', ',').split(",");
            if (coords.length != 2) return null;

            int plotX = Integer.parseInt(coords[0].trim());
            int plotZ = Integer.parseInt(coords[1].trim());

            int plotSize = 32; // Optional: Aus Config holen!
            int x = plotX * plotSize + plotSize / 2;
            int z = plotZ * plotSize + plotSize / 2;

            com.plotsquared.core.location.Location psLoc = com.plotsquared.core.location.Location.at(worldName, x, 64, z);
            PlotArea area = PlotSquared.get().getPlotAreaManager().getPlotArea(psLoc);
            if (area == null) return null;

            PlotId plotId = PlotId.of(plotX, plotZ);
            Plot plot = area.getPlot(plotId);
            if (plot == null) return null;

            return plot.getOwner();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Prüft, ob ein Spieler mit der gegebenen UUID der Owner des Plots ist.
     *
     * @param plotId     Plot-ID im Format "welt;x,z"
     * @param playerUuid UUID des Spielers
     * @return true, wenn Spieler Owner ist
     */
    public static boolean isOwner(String plotId, UUID playerUuid) {
        UUID owner = getOwnerFromPlotSquared(plotId);
        return owner != null && owner.equals(playerUuid);
    }

    /**
     * Gibt den Spieler in Blickrichtung (bis zu 'range' Blöcke) zurück, oder null.
     *
     * @param player Spieler, dessen Blickrichtung geprüft wird
     * @param range  Maximaler Abstand in Blöcken
     * @return Spieler in Blickrichtung oder null
     */
    public static Player getPlayerInSight(Player player, int range) {
        org.bukkit.Location eye = player.getEyeLocation();
        Vector dir = eye.getDirection().normalize();
        double step = 0.5;
        for (double i = 0; i <= range; i += step) {
            org.bukkit.Location check = eye.clone().add(dir.clone().multiply(i));
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (p == player) continue;
                if (p.getWorld().equals(player.getWorld()) && p.getLocation().distance(check) < 1.5) {
                    return p;
                }
            }
        }
        return null;
    }

    public static boolean mergePlots(String plotId1, String plotId2, org.bukkit.entity.Player executor) {
        // Prüfen, dass beide PlotIds gültig sind
        if (plotId1 == null || plotId2 == null) return false;
        try {
            // Plot 1
            String[] parts1 = plotId1.split(";", 2);
            String[] coords1 = parts1[1].split(",");
            String world1 = parts1[0];
            int plotX1 = Integer.parseInt(coords1[0].trim());
            int plotZ1 = Integer.parseInt(coords1[1].trim());
            com.plotsquared.core.location.Location loc1 = com.plotsquared.core.location.Location.at(world1, plotX1 * 32 + 16, 64, plotZ1 * 32 + 16);

            // Plot 2
            String[] parts2 = plotId2.split(";", 2);
            String[] coords2 = parts2[1].split(",");
            String world2 = parts2[0];
            int plotX2 = Integer.parseInt(coords2[0].trim());
            int plotZ2 = Integer.parseInt(coords2[1].trim());
            com.plotsquared.core.location.Location loc2 = com.plotsquared.core.location.Location.at(world2, plotX2 * 32 + 16, 64, plotZ2 * 32 + 16);

            PlotArea area1 = com.plotsquared.core.PlotSquared.get().getPlotAreaManager().getPlotArea(loc1);
            PlotArea area2 = com.plotsquared.core.PlotSquared.get().getPlotAreaManager().getPlotArea(loc2);
            if (area1 == null || area2 == null) return false;

            Plot plot1 = area1.getPlot(loc1);
            Plot plot2 = area2.getPlot(loc2);
            if (plot1 == null || plot2 == null) return false;

            boolean wasOp = executor.isOp();
            if (!wasOp) executor.setOp(true);

            try {
                // Merge ausführen: PlotSquared-API verlangt /plot merge Befehl als Spieler
                // Wir führen den Befehl im Namen des Spielers aus
                String cmd = "plot merge " + plot2.getId().getX() + ";" + plot2.getId().getY();
                executor.performCommand(cmd);
                // Es gibt keine direkte Rückgabe, wir nehmen an, dass es geklappt hat.
                return true;
            } finally {
                if (!wasOp) executor.setOp(false);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Findet das erste Plot in der Blickrichtung des Spielers bis zu einer bestimmten Reichweite.
     * Gibt das Plot (PlotSquared-API) zurück oder null, wenn keins gefunden wurde.
     *
     * @param player      Der Spieler, von dessen Augen aus gesucht wird
     * @param maxDistance Maximale Entfernung (in Blöcken), die geprüft wird
     * @return Das gefundene Plot oder null
     */
    public static Plot findNextPlotInSight(Player player, int maxDistance) {
        Location origin = player.getEyeLocation();
        Vector direction = origin.getDirection().normalize();

        for (int i = 0; i <= maxDistance; i++) {
            Location check = origin.clone().add(direction.clone().multiply(i));
            com.plotsquared.core.location.Location psLoc = bukkitToPlotSquaredLocation(check);

            PlotArea area = PlotSquared.get().getPlotAreaManager().getPlotArea(psLoc);
            if (area == null) continue;
            Plot plot = area.getPlot(psLoc);
            if (plot != null) {
                return plot;
            }
        }
        return null;
    }
}