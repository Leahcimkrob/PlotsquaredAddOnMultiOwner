package de.ethria.plotsquerdaddonmultiowner;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface CoownerLogInterface {
    void logMerge(String plotId, UUID applicantUuid, UUID acceptorUuid);
    List<Map<String, Object>> getAllLogs();
}