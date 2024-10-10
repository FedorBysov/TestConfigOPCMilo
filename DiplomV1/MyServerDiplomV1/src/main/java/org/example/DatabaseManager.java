package org.example;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;


public class DatabaseManager {


        private Connection connection;

        public DatabaseManager(String url, String user, String pass) throws SQLException {
            connection = DriverManager.getConnection(url, user, pass);
        }

        public void insertSensorData(int sensorId, double value, String timestamp) {
            String sql = "INSERT INTO sensor_data (sensor_id, value, timestamp) VALUES (?, ?, ?)";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setInt(1, sensorId);
                stmt.setDouble(2, value);
                stmt.setString(3, timestamp);
                stmt.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        public List<SensorData> readSensorData() {
            List<SensorData> dataList = new ArrayList<>();
            String sql = "SELECT * FROM plc_data";
            try (PreparedStatement stmt = connection.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int sensorId = rs.getInt("sensor_id");
                    double value = rs.getDouble("value");
                    String timestamp = rs.getString("timestamp");
                    dataList.add(new SensorData(sensorId, value, timestamp));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return dataList;
        }

        public void disconnect() {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }



//    private Connection connection;
//
//    public void connect() throws SQLException {
//        connection = DriverManager.getConnection(
//                "jdbc:mysql://localhost:3306/sensor_data", "root", "password");
//    }
//
//    public void insertSensorData(Integer sensorId, double value, String timestamp) {
//        String sql = "INSERT INTO sensor_data (sensor_id, value, timestamp) VALUES (?, ?, ?)";
//
//        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
//            stmt.setInt(1, sensorId);
//            stmt.setDouble(2, value);
//            stmt.setString(3, timestamp);
//            stmt.executeUpdate();
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//    }
//
//    public void disconnect() {
//        if (connection != null) {
//            try {
//                connection.close();
//            } catch (SQLException e) {
//                e.printStackTrace();
//            }
//        }
//    }
//}
