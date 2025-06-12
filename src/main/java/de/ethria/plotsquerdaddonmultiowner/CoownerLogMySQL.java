package de.ethria.plotsquerdaddonmultiowner;

import org.bukkit.Bukkit;

import java.sql.*;
import java.util.*;

public class CoownerLogMySQL {
    private final Connection connection;

    public CoownerLogMySQL(Connection connection) {
        this.connection = connection;
        createTableIfNotExists();
    }

    private void createTableIfNotExists() {
        try (Statement st = connection.createStatement()) {
            st.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS coownerLog (" +
                            "id INT AUTO_INCREMENT PRIMARY KEY, " +
                            "plotid VARCHAR(64) NOT NULL, " +
                            "applicant_name VARCHAR(32) NOT NULL, " +
                            "applicant_uuid CHAR(36) NOT NULL, " +
                            "acceptor_name VARCHAR(32) NOT NULL, " +
                            "acceptor_uuid CHAR(36) NOT NULL, " +
                            "timestamp BIGINT NOT NULL" +
                            ");"
            );
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void logMerge(String plotId, UUID applicantUuid, UUID acceptorUuid) {
        String applicantName = Bukkit.getOfflinePlayer(applicantUuid).getName();
        String acceptorName = Bukkit.getOfflinePlayer(acceptorUuid).getName();
        long timestamp = System.currentTimeMillis() / 1000L;

        String sql = "INSERT INTO coownerLog (plotid, applicant_name, applicant_uuid, acceptor_name, acceptor_uuid, timestamp) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, plotId);
            ps.setString(2, applicantName);
            ps.setString(3, applicantUuid.toString());
            ps.setString(4, acceptorName);
            ps.setString(5, acceptorUuid.toString());
            ps.setLong(6, timestamp);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Map<String, Object>> getAllLogs() {
        List<Map<String, Object>> logs = new ArrayList<>();
        String sql = "SELECT * FROM coownerLog ORDER BY id ASC";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Map<String, Object> entry = new LinkedHashMap<>();
                entry.put("id", rs.getInt("id"));
                entry.put("plotid", rs.getString("plotid"));
                entry.put("applicant_name", rs.getString("applicant_name"));
                entry.put("applicant_uuid", rs.getString("applicant_uuid"));
                entry.put("acceptor_name", rs.getString("acceptor_name"));
                entry.put("acceptor_uuid", rs.getString("acceptor_uuid"));
                entry.put("timestamp", rs.getLong("timestamp"));
                logs.add(entry);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return logs;
    }
}