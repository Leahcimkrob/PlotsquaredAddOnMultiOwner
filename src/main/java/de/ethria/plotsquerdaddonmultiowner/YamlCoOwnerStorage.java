package de.ethria.plotsquerdaddonmultiowner;

import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.location.Location;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.plot.PlotId;
import com.plotsquared.core.plot.PlotArea;

import java.util.*;

public class YamlCoOwnerStorage implements CoOwnerStorage {
    private final MultiOwnerAddon plugin;
    // Map: plotId -> coownerUUIDs
    private final Map<String, Set<UUID>> coowners = new HashMap<>();
    // Map: plotId -> ownerUUID (dies ist für die Überprüfung nötig)
    private final Map<String, UUID> plotOwners = new HashMap<>();

    public YamlCoOwnerStorage(MultiOwnerAddon plugin) {
        this.plugin = plugin;
    }

    @Override
    public void init() {
        // Lade aus YAML, falls benötigt
    }

    @Override
    public void close() {
        // Speichere nach YAML, falls benötigt
    }

    @Override
    public void addCoOwner(String plotId, UUID uuid) {
        coowners.computeIfAbsent(plotId, k -> new HashSet<>()).add(uuid);
        plotOwners.put(plotId, getOwner(plotId));
    }

    @Override
    public boolean removeCoOwner(String plotId, UUID uuid) {
        Set<UUID> co = coowners.get(plotId);
        if (co != null) {
            boolean removed = co.remove(uuid);
            if (co.isEmpty()) {
                coowners.remove(plotId);
                plotOwners.remove(plotId);
            }
            return removed;
        }
        return false;
    }

    @Override
    public void removeAllCoOwners(String plotId) {
        coowners.remove(plotId);
        plotOwners.remove(plotId);
    }

    @Override
    public Set<String> getAllPlotIdsWithCoOwners() {
        return new HashSet<>(coowners.keySet());
    }

    @Override
    public boolean isOwnerValid(String plotIdStr, UUID ownerUuid) {
        UUID savedOwner = plotOwners.get(plotIdStr);
        if (savedOwner == null || ownerUuid == null) return false;
        return savedOwner.equals(ownerUuid);
    }

    // Hilfsmethode: Holt aktuellen Owner per PlotSquared API
    private UUID getOwner(String plotIdStr) {
        try {
            PlotId plotId = PlotId.fromString(plotIdStr);
            String[] parts = plotIdStr.split(";", 2);
            if (parts.length != 2) return null;

            String worldName = parts[0];
            String[] coords = parts[1].split(",");
            if (coords.length != 2) return null;

            int px = Integer.parseInt(coords[0]);
            int pz = Integer.parseInt(coords[1]);

            // Verwende passende Plotgröße (z.B. 32, evtl. dynamisch auslesen)
            int plotSize = 32;
            int bx = px * plotSize + plotSize / 2;
            int bz = pz * plotSize + plotSize / 2;

            com.plotsquared.core.location.Location loc =
                    com.plotsquared.core.location.Location.at(worldName, bx, 64, bz); // Y-Wert mittelmäßig

            Plot plot = Plot.getPlot(loc);
            return (plot != null) ? plot.getOwner() : null;

        } catch (Exception e) {
            plugin.getLogger().warning("Fehler beim Ermitteln des Plot-Owners für '" + plotIdStr + "': " + e.getMessage());
            return null;
        }
    }
}