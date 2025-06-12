package de.ethria.plotsquerdaddonmultiowner;

import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.plot.PlotArea;
import com.plotsquared.core.plot.PlotId;
import com.plotsquared.core.location.Location;

import java.util.UUID;

public class PlotUtil {

    /**
     * Holt den Owner eines Plots anhand der PlotId (Format: "welt;x,z") via PlotSquared.
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

            Location loc = Location.at(worldName, x, 64, z);
            PlotArea area = PlotSquared.get().getPlotAreaManager().getPlotArea(loc);
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
     * @param plotId Plot-ID im Format "welt;x,z"
     * @param playerUuid UUID des Spielers
     * @return true, wenn Spieler Owner ist
     */
    public static boolean isOwner(String plotId, UUID playerUuid) {
        UUID owner = getOwnerFromPlotSquared(plotId);
        return owner != null && owner.equals(playerUuid);
    }
}