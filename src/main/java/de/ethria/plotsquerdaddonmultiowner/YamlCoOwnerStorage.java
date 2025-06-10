package de.ethria.plotsquerdaddonmultiowner;

import com.plotsquared.core.PlotSquared;
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
            PlotArea area = PlotSquared.get().getPlotAreaManager().getPlotArea(plotId);
            Plot plot = (area != null) ? area.getPlot(plotId) : null;
            return (plot != null) ? plot.getOwner() : null;
        } catch (Exception e) {
            return null;
        }
    }
}