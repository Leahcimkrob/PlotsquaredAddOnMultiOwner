package de.ethria.plotsquerdaddonmultiowner;

import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.location.Location;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.plot.PlotArea;
import com.plotsquared.core.plot.PlotId;

import java.util.UUID;

public class PlotUtil {

    /**
     * Liest den Owner eines Plots anhand der PlotId (Format: "welt;x,z") aus PlotSquared aus.
     * Gibt die UUID des Owners zurück oder null, falls kein Plot existiert.
     */
    public static UUID getOwnerFromPlotSquared(String plotIdStr) {
        try {
            if (plotIdStr == null) return null;

            // Akzeptiere sowohl ";" als auch "," als Trenner für Koordinaten
            String[] parts = plotIdStr.split(";", 2);
            if (parts.length != 2) return null;
            String worldName = parts[0];
            String coordsRaw = parts[1].replace(';', ','); // Ersetze evtl. falsche Trenner

            String[] coords = coordsRaw.split(",");
            if (coords.length != 2) return null;

            int plotX = Integer.parseInt(coords[0].trim());
            int plotZ = Integer.parseInt(coords[1].trim());

            // Plotgröße ggf. aus PlotSquared-Config nehmen!
            int plotSize = 32;
            int x = plotX * plotSize + plotSize / 2;
            int z = plotZ * plotSize + plotSize / 2;

            Location loc = Location.at(worldName, x, 64, z);
            PlotArea area = PlotSquared.get().getPlotAreaManager().getPlotArea(loc);
            if (area == null) return null;

            PlotId pId = PlotId.of(plotX, plotZ);
            Plot plot = area.getPlot(pId);
            if (plot == null) return null;

            return plot.getOwner();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Prüft, ob ein Spieler mit der gegebenen UUID der Owner des Plots ist.
     */
    public static boolean isOwner(String plotId, UUID playerUuid) {
        UUID owner = getOwnerFromPlotSquared(plotId);
        return owner != null && owner.equals(playerUuid);
    }
}