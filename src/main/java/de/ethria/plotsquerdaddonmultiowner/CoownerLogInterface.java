package de.ethria.plotsquerdaddonmultiowner;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface CoownerLogInterface {
    /**
     * Loggt ein Merge-Ereignis.
     * @param plotId Plot-ID (z.B. "welt;12,34")
     * @param applicantUuid UUID des Antragstellers (der den Merge anstößt)
     * @param acceptorUuid UUID des Zustimmenden (der akzeptiert)
     */
    void logMerge(String plotId, UUID applicantUuid, UUID acceptorUuid);

    /**
     * Liefert alle gespeicherten Merge-Logs als Liste von Maps.
     * Jede Map enthält: plotid, applicant_name, applicant_uuid, acceptor_name, acceptor_uuid, timestamp
     */
    List<Map<String, Object>> getAllLogs();
}