package de.ethria.plotsquerdaddonmultiowner;

import java.util.List;
import java.util.UUID;

public interface CoOwnerStorage {
    void init();
    void addCoOwner(String plotId, UUID uuid);
    boolean removeCoOwner(String plotId, UUID uuid);
    void removeAllCoOwners(String plotId);
    List<UUID> getCoOwners(String plotId);
    void close();
}