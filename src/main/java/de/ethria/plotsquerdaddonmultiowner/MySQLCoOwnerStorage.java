package de.ethria.plotsquerdaddonmultiowner;

import org.bukkit.Bukkit;

import java.sql.*;
import java.util.*;

public class MySQLCoOwnerStorage implements CoOwnerStorage {
    private final MultiOwnerAddon plugin;
    private Connection connection;

    public MySQLCoOwnerStorage(MultiOwnerAddon plugin) {
        this.plugin = plugin;
    }

    @Override
    public void init() {
        try {
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/deinedatenbank", "user", "pass");
            Statement st = connection.createStatement();
            st.executeUpdate("CREATE TABLE IF NOT EXISTS coowners (" +
                    "plotid VARCHAR(128) NOT NULL, " +
                    "uuid VARCHAR(36) NOT NULL, " +
                    "name VARCHAR(32) NOT NULL, " +
                    "PRIMARY KEY (plotid, uuid)" +
                    ");");
        } catch (SQLException e) {
            String msg = plugin.getMsg("msg_error_mysql_init")
                    .replace("{error}", e.getMessage() == null ? "unknown" : e.getMessage());
            plugin.getLogger().severe(msg);
        }
    }

    @Override
    public void addCoOwner(String plotId, UUID uuid) {
        String name = Bukkit.getOfflinePlayer(uuid).getName();
        try {
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO coowners (plotid, uuid, name) VALUES (?, ?, ?) " +
                            "ON DUPLICATE KEY UPDATE name=?;"
            );
            ps.setString(1, plotId);
            ps.setString(2, uuid.toString());
            ps.setString(3, name);
            ps.setString(4, name);
            ps.executeUpdate();
        } catch (SQLException e) {
            String msg = plugin.getMsg("msg_error_mysql_add")
                    .replace("{error}", e.getMessage() == null ? "unknown" : e.getMessage());
            plugin.getLogger().severe(msg);
        }
    }

    @Override
    public boolean removeCoOwner(String plotId, UUID uuid) {
        try {
            PreparedStatement ps = connection.prepareStatement(
                    "DELETE FROM coowners WHERE plotid=? AND uuid=?;"
            );
            ps.setString(1, plotId);
            ps.setString(2, uuid.toString());
            int updated = ps.executeUpdate();
            return updated > 0;
        } catch (SQLException e) {
            String msg = plugin.getMsg("msg_error_mysql_remove")
                    .replace("{error}", e.getMessage() == null ? "unknown" : e.getMessage());
            plugin.getLogger().severe(msg);
        }
        return false;
    }

    @Override
    public void removeAllCoOwners(String plotId) {
        try {
            PreparedStatement ps = connection.prepareStatement(
                    "DELETE FROM coowners WHERE plotid=?;"
            );
            ps.setString(1, plotId);
            ps.executeUpdate();
        } catch (SQLException e) {
            String msg = plugin.getMsg("msg_error_mysql_removeall")
                    .replace("{error}", e.getMessage() == null ? "unknown" : e.getMessage());
            plugin.getLogger().severe(msg);
        }
    }

    @Override
    public Set<UUID> getCoOwners(String plotId) {
        Set<UUID> result = new HashSet<>();
        try {
            PreparedStatement ps = connection.prepareStatement(
                    "SELECT uuid FROM coowners WHERE plotid=?;"
            );
            ps.setString(1, plotId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                result.add(UUID.fromString(rs.getString("uuid")));
            }
        } catch (SQLException e) {
            String msg = plugin.getMsg("msg_error_mysql_getcoowners")
                    .replace("{error}", e.getMessage() == null ? "unknown" : e.getMessage());
            plugin.getLogger().severe(msg);
        }
        return result;
    }

    public List<CoOwnerInfo> getCoOwnerInfos(String plotId) {
        List<CoOwnerInfo> result = new ArrayList<>();
        try {
            PreparedStatement ps = connection.prepareStatement(
                    "SELECT uuid, name FROM coowners WHERE plotid=?;"
            );
            ps.setString(1, plotId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                UUID uuid = UUID.fromString(rs.getString("uuid"));
                String name = rs.getString("name");
                result.add(new CoOwnerInfo(uuid, name));
            }
        } catch (SQLException e) {
            String msg = plugin.getMsg("msg_error_mysql_getcoowners")
                    .replace("{error}", e.getMessage() == null ? "unknown" : e.getMessage());
            plugin.getLogger().severe(msg);
        }
        return result;
    }

    @Override
    public Set<String> getAllPlotIdsWithCoOwners() {
        Set<String> plotIds = new HashSet<>();
        try {
            Statement st = connection.createStatement();
            ResultSet rs = st.executeQuery("SELECT DISTINCT plotid FROM coowners;");
            while (rs.next()) {
                plotIds.add(rs.getString("plotid"));
            }
        } catch (SQLException e) {
            String msg = plugin.getMsg("msg_error_mysql_getall")
                    .replace("{error}", e.getMessage() == null ? "unknown" : e.getMessage());
            plugin.getLogger().warning(msg);
        }
        return plotIds;
    }

    @Override
    public boolean isOwnerValid(String plotId, UUID ownerUuid) {
        if (ownerUuid == null) return false;
        UUID actualOwner = PlotUtil.getOwnerFromPlotSquared(plotId);
        return ownerUuid.equals(actualOwner);
    }

    @Override
    public void close() {
        try {
            if (connection != null) connection.close();
        } catch (SQLException e) {
            String msg = plugin.getMsg("msg_error_mysql_close")
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