package de.ethria.plotsquerdaddonmultiowner;

import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.List;
import java.util.logging.Logger;

public class CoownerAddon extends JavaPlugin {
    public static CoownerAddon instance;

    private FileConfiguration messages;
    private CoownerLogInterface coownerLog; // <-- Neues Interface!
    private boolean useMySQL;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        saveMessagesFile();
        loadMessages();

        // Backend wählen (YAML oder MySQL)
        useMySQL = getConfig().getString("storage-type", "yaml").equalsIgnoreCase("mysql");
        if (useMySQL) {
            try {
                // Hole MySQL-Zugangsdaten aus config.yml
                String host = getConfig().getString("mysql.host", "localhost");
                int port = getConfig().getInt("mysql.port", 3306);
                String database = getConfig().getString("mysql.database", "plotsquared");
                String user = getConfig().getString("mysql.user", "root");
                String password = getConfig().getString("mysql.password", "");
                String url = "jdbc:mysql://" + host + ":" + port + "/" + database + "?autoReconnect=true";

                java.sql.Connection connection = java.sql.DriverManager.getConnection(url, user, password);
                coownerLog = new CoownerLogInterface(connection);
                getLogger().info("CoownerLogMySQL initialisiert.");
            } catch (Exception e) {
                getLogger().severe("Fehler beim Aufbau der MySQL-Verbindung für coownerLog: " + e.getMessage());
                getLogger().severe("Falle zurück auf YAML!");
                coownerLog = new CoownerLogYAML(getDataFolder());
            }
        } else {
            coownerLog = new CoownerLogYAML(getDataFolder());
            getLogger().info("CoownerLogYAML initialisiert.");
        }

        List<String> aliases = getConfig().getStringList("command-aliases");
        PluginCommand cmd = this.getCommand("multiowner");
        if (cmd != null) {
            CoownerCommand commandHandler = new CoownerCommand();
            cmd.setExecutor(commandHandler);
            cmd.setTabCompleter(commandHandler);
            cmd.setAliases(aliases);
        }
    }

    private void saveMessagesFile() {
        File msgFile = new File(getDataFolder(), "messages.yml");
        if (!msgFile.exists()) {
            saveResource("messages.yml", false);
        }
    }

    public void loadMessages() {
        File msgFile = new File(getDataFolder(), "messages.yml");
        messages = YamlConfiguration.loadConfiguration(msgFile);
    }

    @Override
    public void onDisable() {
        // ggf. close() für MySQL implementieren
        getLogger().info("MultiOwnerAddon deaktiviert.");
    }

    public String getMsg(String key) {
        String msg = messages.getString(key, "&cNachricht nicht definiert: " + key);
        return org.bukkit.ChatColor.translateAlternateColorCodes('&', msg);
    }

    public CoownerLogInterface getCoownerLog() {
        return coownerLog;
    }

    public static CoownerAddon getInstance() {
        return instance;
    }

    public Logger getPluginLogger() {
        return getLogger();
    }
}