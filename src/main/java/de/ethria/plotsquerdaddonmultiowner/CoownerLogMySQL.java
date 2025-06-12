package de.ethria.plotsquerdaddonmultiowner;

import org.bukkit.Bukkit;

import java.sql.*;
import java.util.*;

public class CoownerLogMySQL implements CoownerLogInterface {
    private final Connection connection;

    public CoownerLogMySQL(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void logMerge(String plotId, UUID plot1Uuid, UUID plot2Uuid, boolean adminmerge) {
        String plot1Name = Bukkit.getOfflinePlayer(plot1Uuid).getName();
        String plot2Name = Bukkit.getOfflinePlayer(plot2Uuid).getName();
        long timestamp = System.currentTimeMillis() / 1000L;

        String sql = "INSERT INTO coownerLog (plotid, plot1_name, plot1_uuid, plot2_name, plot2_uuid, timestamp, adminmerge) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, plotId);
            ps.setString(2, plot1Name);
            ps.setString(3, plot1Uuid.toString());
            ps.setString(4, plot2Name);
            ps.setString(5, plot2Uuid.toString());
            ps.setLong(6, timestamp);
            ps.setBoolean(7, adminmerge);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<Map<String, Object>> getAllLogs() {
        List<Map<String, Object>> logs = new ArrayList<>();
        String sql = "SELECT * FROM coownerLog ORDER BY id ASC";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Map<String, Object> entry = new LinkedHashMap<>();
                entry.put("id", rs.getInt("id"));
                entry.put("plotid", rs.getString("plotid"));
                entry.put("plot1_name", rs.getString("plot1_name"));
                entry.put("plot1_uuid", rs.getString("plot1_uuid"));
                entry.put("plot2_name", rs.getString("plot2_name"));
                entry.put("plot2_uuid", rs.getString("plot2_uuid"));
                entry.put("timestamp", rs.getLong("timestamp"));
                entry.put("adminmerge", rs.getBoolean("adminmerge"));
                logs.add(entry);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return logs;
    }
}