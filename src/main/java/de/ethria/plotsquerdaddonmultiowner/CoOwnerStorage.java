package de.ethria.plotsquerdaddonmultiowner;

import java.util.Set;
import java.util.UUID;

public interface CoOwnerStorage {
    void init();
    void addCoOwner(String plotId, UUID uuid);
    boolean removeCoOwner(String plotId, UUID uuid);
    void removeAllCoOwners(String plotId);
    Set<UUID> getCoOwners(String plotId); // <--- HINZUFÜGEN!
    Set<String> getAllPlotIdsWithCoOwners();
    boolean isOwnerValid(String plotId, UUID ownerUuid);
    void close();
}