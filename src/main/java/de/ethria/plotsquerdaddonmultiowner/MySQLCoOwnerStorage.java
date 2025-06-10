package de.ethria.plotsquerdaddonmultiowner;

import org.bukkit.Bukkit;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MySQLCoOwnerStorage implements CoOwnerStorage {
    private final MultiOwnerAddon plugin;
    private Connection connection;

    public MySQLCoOwnerStorage(MultiOwnerAddon plugin) {
        this.plugin = plugin;
    }

    @Override
    public void init() {
        try {
            String host = plugin.getConfig().getString("mysql.host");
            int port = plugin.getConfig().getInt("mysql.port");
            String db = plugin.getConfig().getString("mysql.database");
            String user = plugin.getConfig().getString("mysql.user");
            String pw = plugin.getConfig().getString("mysql.password");
            String url = "jdbc:mysql://" + host + ":" + port + "/" + db + "?useSSL=false";
            connection = DriverManager.getConnection(url, user, pw);

            Statement st = connection.createStatement();
            st.executeUpdate("CREATE TABLE IF NOT EXISTS coowners (plotid VARCHAR(64), uuid VARCHAR(36), UNIQUE KEY(plotid, uuid));");
        } catch (SQLException e) {
            Bukkit.getLogger().severe("[MultiOwnerAddon] MySQL init error: " + e.getMessage());
        }
    }

    @Override
    public void addCoOwner(String plotId, UUID uuid) {
        try {
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT IGNORE INTO coowners (plotid, uuid) VALUES (?, ?);");
            ps.setString(1, plotId);
            ps.setString(2, uuid.toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            Bukkit.getLogger().severe("[MultiOwnerAddon] MySQL add error: " + e.getMessage());
        }
    }

    @Override
    public boolean removeCoOwner(String plotId, UUID uuid) {
        try {
            PreparedStatement ps = connection.prepareStatement(
                    "DELETE FROM coowners WHERE plotid=? AND uuid=?;");
            ps.setString(1, plotId);
            ps.setString(2, uuid.toString());
            int updated = ps.executeUpdate();
            return updated > 0;
        } catch (SQLException e) {
            Bukkit.getLogger().severe("[MultiOwnerAddon] MySQL remove error: " + e.getMessage());
        }
        return false;
    }

    @Override
    public void removeAllCoOwners(String plotId) {
        try {
            PreparedStatement ps = connection.prepareStatement(
                    "DELETE FROM coowners WHERE plotid=?;");
            ps.setString(1, plotId);
            ps.executeUpdate();
        } catch (SQLException e) {
            Bukkit.getLogger().severe("[MultiOwnerAddon] MySQL remove all error: " + e.getMessage());
        }
    }

    @Override
    public List<UUID> getCoOwners(String plotId) {
        List<UUID> result = new ArrayList<>();
        try {
            PreparedStatement ps = connection.prepareStatement(
                    "SELECT uuid FROM coowners WHERE plotid=?;");
            ps.setString(1, plotId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                result.add(UUID.fromString(rs.getString("uuid")));
            }
        } catch (SQLException e) {
            Bukkit.getLogger().severe("[MultiOwnerAddon] MySQL get error: " + e.getMessage());
        }
        return result;
    }

    @Override
    public void close() {
        try {
            if (connection != null) connection.close();
        } catch (SQLException ignored) {}
    }
}