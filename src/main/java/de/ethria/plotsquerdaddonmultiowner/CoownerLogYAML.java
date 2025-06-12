package de.ethria.plotsquerdaddonmultiowner;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class CoownerLogYAML implements CoownerLogInterface {
    private final File file;
    private final YamlConfiguration yaml;

    public CoownerLogYAML(File dataFolder) {
        this.file = new File(dataFolder, "coownerLog.yml");
        this.yaml = YamlConfiguration.loadConfiguration(file);
    }

    @Override
    public void logMerge(String plotId, UUID plot1Uuid, UUID plot2Uuid, boolean adminmerge) {
        String plot1Name = Bukkit.getOfflinePlayer(plot1Uuid).getName();
        String plot2Name = Bukkit.getOfflinePlayer(plot2Uuid).getName();
        long timestamp = System.currentTimeMillis() / 1000L;

        Map<String, Object> entry = new LinkedHashMap<>();
        entry.put("plotid", plotId);
        entry.put("plot1_name", plot1Name);
        entry.put("plot1_uuid", plot1Uuid.toString());
        entry.put("plot2_name", plot2Name);
        entry.put("plot2_uuid", plot2Uuid.toString());
        entry.put("timestamp", timestamp);
        entry.put("adminmerge", adminmerge);

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

    @Override
    public List<Map<String, Object>> getAllLogs() {
        List<Map<String, Object>> logs = (List<Map<String, Object>>) yaml.getList("coownerLog");
        return logs != null ? logs : new ArrayList<>();
    }
}