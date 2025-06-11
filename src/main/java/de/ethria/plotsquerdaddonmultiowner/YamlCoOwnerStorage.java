package de.ethria.plotsquerdaddonmultiowner;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class YamlCoOwnerStorage implements CoOwnerStorage {
    private final MultiOwnerAddon plugin;
    private File file;
    private YamlConfiguration yaml;

    public YamlCoOwnerStorage(MultiOwnerAddon plugin) {
        this.plugin = plugin;
    }

    @Override
    public void init() {
        try {
            file = new File(plugin.getDataFolder(), "coowners.yml");
            if (!file.exists()) file.createNewFile();
            yaml = YamlConfiguration.loadConfiguration(file);
        } catch (IOException e) {
            // Nur ins Log schreiben, nicht an Spieler
            String msg = plugin.getMsg("msg_error_yaml_init")
                    .replace("{error}", e.getMessage() == null ? "unknown" : e.getMessage());
            plugin.getLogger().severe(msg);
        }
    }

    @Override
    public void addCoOwner(String plotId, UUID uuid) {
        String name = Bukkit.getOfflinePlayer(uuid).getName();
        List<Map<?, ?>> rawList = yaml.getMapList(plotId);
        List<Map<String, Object>> owners = new ArrayList<>();
        boolean found = false;
        for (Map<?, ?> entry : rawList) {
            Map<String, Object> casted = new HashMap<>();
            for (Map.Entry<?, ?> e : entry.entrySet()) {
                casted.put(String.valueOf(e.getKey()), e.getValue());
            }
            if (casted.get("uuid") != null && casted.get("uuid").equals(uuid.toString())) {
                casted.put("name", name);
                found = true;
            }
            owners.add(casted);
        }
        if (!found) {
            Map<String, Object> newOwner = new HashMap<>();
            newOwner.put("uuid", uuid.toString());
            newOwner.put("name", name);
            owners.add(newOwner);
        }
        yaml.set(plotId, owners);
        save();
    }

    @Override
    public boolean removeCoOwner(String plotId, UUID uuid) {
        List<Map<?, ?>> rawList = yaml.getMapList(plotId);
        List<Map<String, Object>> owners = new ArrayList<>();
        boolean removed = false;
        for (Map<?, ?> entry : rawList) {
            Map<String, Object> casted = new HashMap<>();
            for (Map.Entry<?, ?> e : entry.entrySet()) {
                casted.put(String.valueOf(e.getKey()), e.getValue());
            }
            if (casted.get("uuid") != null && casted.get("uuid").equals(uuid.toString())) {
                removed = true;
                continue;
            }
            owners.add(casted);
        }
        yaml.set(plotId, owners);
        save();
        return removed;
    }

    @Override
    public void removeAllCoOwners(String plotId) {
        yaml.set(plotId, null);
        save();
    }

    @Override
    public Set<UUID> getCoOwners(String plotId) {
        Set<UUID> result = new HashSet<>();
        List<Map<?, ?>> rawList = yaml.getMapList(plotId);
        for (Map<?, ?> entry : rawList) {
            Object uuidObj = entry.get("uuid");
            if (uuidObj != null) {
                try {
                    result.add(UUID.fromString(uuidObj.toString()));
                } catch (IllegalArgumentException ignore) {}
            }
        }
        return result;
    }

    public List<CoOwnerInfo> getCoOwnerInfos(String plotId) {
        List<CoOwnerInfo> result = new ArrayList<>();
        List<Map<?, ?>> rawList = yaml.getMapList(plotId);
        for (Map<?, ?> entry : rawList) {
            Object uuidObj = entry.get("uuid");
            Object nameObj = entry.get("name");
            if (uuidObj != null) {
                try {
                    UUID uuid = UUID.fromString(uuidObj.toString());
                    String name = nameObj != null ? nameObj.toString() : null;
                    result.add(new CoOwnerInfo(uuid, name));
                } catch (IllegalArgumentException ignore) {}
            }
        }
        return result;
    }

    @Override
    public Set<String> getAllPlotIdsWithCoOwners() {
        return yaml.getKeys(false);
    }

    @Override
    public boolean isOwnerValid(String plotId, UUID ownerUuid) {
        if (ownerUuid == null) return false;
        UUID actualOwner = PlotUtil.getOwnerFromPlotSquared(plotId);
        return ownerUuid.equals(actualOwner);
    }

    @Override
    public void close() {
        save();
    }

    private void save() {
        try {
            yaml.save(file);
        } catch (IOException e) {
            // Nur ins Log schreiben, nicht an Spieler
            String msg = plugin.getMsg("msg_error_yaml_save")
                    .replace("{error}", e.getMessage() == null ? "unknown" : e.getMessage());
            plugin.getLogger().severe(msg);
        }
    }

    public static class CoOwnerInfo {
        public final UUID uuid;
        public final String name;

        public CoOwnerInfo(UUID uuid, String name) {
            this.uuid = uuid;
            this.name = name;
        }
    }
}