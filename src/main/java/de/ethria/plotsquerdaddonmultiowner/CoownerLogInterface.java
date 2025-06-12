package de.ethria.plotsquerdaddonmultiowner;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface CoownerLogInterface {
    void logMerge(String plotId, UUID plot1Uuid, UUID plot2Uuid, boolean adminmerge);
    List<Map<String, Object>> getAllLogs();
}