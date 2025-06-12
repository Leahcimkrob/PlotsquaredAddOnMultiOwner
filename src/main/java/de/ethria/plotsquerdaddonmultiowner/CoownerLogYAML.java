package de.ethria.plotsquerdaddonmultiowner;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class CoownerLogYAML {
    private final File file;
    private final YamlConfiguration yaml;

    public CoownerLogYAML(File dataFolder) {
        this.file = new File(dataFolder, "coownerLog.yml");
        this.yaml = YamlConfiguration.loadConfiguration(file);
    }

    public void logMerge(String plotId, UUID applicantUuid, UUID acceptorUuid) {
        String applicantName = Bukkit.getOfflinePlayer(applicantUuid).getName();
        String acceptorName = Bukkit.getOfflinePlayer(acceptorUuid).getName();
        long timestamp = System.currentTimeMillis() / 1000L;

        // Daten als Map für YAML
        Map<String, Object> entry = new LinkedHashMap<>();
        entry.put("plotid", plotId);
        entry.put("applicant_name", applicantName);
        entry.put("applicant_uuid", applicantUuid.toString());
        entry.put("acceptor_name", acceptorName);
        entry.put("acceptor_uuid", acceptorUuid.toString());
        entry.put("timestamp", timestamp);

        List<Map<String, Object>> logs = (List<Map<String, Object>>) yaml.getList("coownerLog");
        if (logs == null) logs = new ArrayList<>();
        logs.add(entry);

        yaml.set("coownerLog", logs);
        try {
            yaml.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<Map<String, Object>> getAllLogs() {
        List<Map<String, Object>> logs = (List<Map<String, Object>>) yaml.getList("coownerLog");
        return logs != null ? logs : new ArrayList<>();
    }
}