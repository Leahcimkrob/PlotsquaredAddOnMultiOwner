package de.ethria.plotsquerdaddonmultiowner;

import java.util.*;
import java.io.*;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;

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
            Bukkit.getLogger().severe("[MultiOwnerAddon] YAML init error: " + e.getMessage());
        }
    }

    @Override
    public void addCoOwner(String plotId, UUID uuid) {
        List<String> owners = yaml.getStringList(plotId);
        if (!owners.contains(uuid.toString())) {
            owners.add(uuid.toString());
            yaml.set(plotId, owners);
            save();
        }
    }

    @Override
    public boolean removeCoOwner(String plotId, UUID uuid) {
        List<String> owners = yaml.getStringList(plotId);
        boolean removed = owners.remove(uuid.toString());
        yaml.set(plotId, owners);
        save();
        return removed;
    }

    @Override
    public void removeAllCoOwners(String plotId) {
        yaml.set(plotId, null);
        save();
    }

    public Set<UUID> getCoOwners(String plotId) {
        List<String> owners = yaml.getStringList(plotId);
        Set<UUID> result = new HashSet<>();
        for (String s : owners) {
            try {
                result.add(UUID.fromString(s));
            } catch (IllegalArgumentException ignore) {}
        }
        return result;
    }

    @Override
    public Set<String> getAllPlotIdsWithCoOwners() {
        return yaml.getKeys(false);
    }

    // HIER: Owner-Prüfung via PlotSquared!
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
        } catch (IOException ignore) {}
    }
}