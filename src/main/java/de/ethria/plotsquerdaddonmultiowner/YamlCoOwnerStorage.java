package de.ethria.plotsquerdaddonmultiowner;

import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class YamlCoOwnerStorage implements CoOwnerStorage {
    private final MultiOwnerAddon plugin;
    private final File file;
    private YamlConfiguration config;

    public YamlCoOwnerStorage(MultiOwnerAddon plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "coowners.yml");
    }

    @Override
    public void init() {
        config = YamlConfiguration.loadConfiguration(file);
    }

    @Override
    public void addCoOwner(String plotId, UUID uuid) {
        List<String> coowners = config.getStringList(plotId);
        if (!coowners.contains(uuid.toString())) {
            coowners.add(uuid.toString());
            config.set(plotId, coowners);
            save();
        }
    }

    @Override
    public boolean removeCoOwner(String plotId, UUID uuid) {
        List<String> coowners = config.getStringList(plotId);
        boolean removed = coowners.remove(uuid.toString());
        config.set(plotId, coowners);
        save();
        return removed;
    }

    @Override
    public void removeAllCoOwners(String plotId) {
        config.set(plotId, null);
        save();
    }

    @Override
    public List<UUID> getCoOwners(String plotId) {
        List<String> coowners = config.getStringList(plotId);
        List<UUID> result = new ArrayList<>();
        for (String s : coowners) {
            try {
                result.add(UUID.fromString(s));
            } catch (Exception ignored) {}
        }
        return result;
    }

    @Override
    public void close() {
        save();
    }

    private void save() {
        try {
            config.save(file);
        } catch (IOException ignored) {}
    }
}