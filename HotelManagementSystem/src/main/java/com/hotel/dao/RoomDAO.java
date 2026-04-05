package com.hotel.dao;

import com.hotel.model.Room;
import com.hotel.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for the rooms table.
 * Provides CRUD operations and search/filter support.
 */
public class RoomDAO {

    private final Connection conn;

    public RoomDAO() {
        this.conn = DatabaseConnection.getInstance().getConnection();
    }

    /** Fetches all rooms from the database. */
    public List<Room> getAllRooms() throws SQLException {
        List<Room> rooms = new ArrayList<>();
        String sql = "SELECT room_number, type, price, available FROM rooms ORDER BY room_number";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                rooms.add(mapRow(rs));
            }
        }
        return rooms;
    }

    /** Fetches rooms filtered by a search keyword (matches room type or number). */
    public List<Room> searchRooms(String keyword) throws SQLException {
        List<Room> rooms = new ArrayList<>();
        String sql = "SELECT room_number, type, price, available FROM rooms "
                   + "WHERE LOWER(type) LIKE ? OR CAST(room_number AS TEXT) LIKE ? "
                   + "ORDER BY room_number";
        String like = "%" + keyword.toLowerCase() + "%";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, like);
            ps.setString(2, like);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rooms.add(mapRow(rs));
                }
            }
        }
        return rooms;
    }

    /** Inserts a new room. Throws SQLException if room number already exists. */
    public void addRoom(Room room) throws SQLException {
        String sql = "INSERT INTO rooms (room_number, type, price, available) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, room.getRoomNumber());
            ps.setString(2, room.getType());
            ps.setDouble(3, room.getPrice());
            ps.setInt(4, room.isAvailable() ? 1 : 0);
            ps.executeUpdate();
        }
    }

    /** Updates the availability status of a room. */
    public void updateAvailability(int roomNumber, boolean available) throws SQLException {
        String sql = "UPDATE rooms SET available = ? WHERE room_number = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, available ? 1 : 0);
            ps.setInt(2, roomNumber);
            ps.executeUpdate();
        }
    }

    /** Returns a single room by its room number, or null if not found. */
    public Room getRoomByNumber(int roomNumber) throws SQLException {
        String sql = "SELECT room_number, type, price, available FROM rooms WHERE room_number = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, roomNumber);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    /** Counts total rooms. */
    public int countTotalRooms() throws SQLException {
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM rooms")) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    /** Counts rooms that are currently available. */
    public int countAvailableRooms() throws SQLException {
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM rooms WHERE available = 1")) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    // Helper: maps a ResultSet row to a Room object
    private Room mapRow(ResultSet rs) throws SQLException {
        return new Room(
                rs.getInt("room_number"),
                rs.getString("type"),
                rs.getDouble("price"),
                rs.getInt("available") == 1
        );
    }
}
