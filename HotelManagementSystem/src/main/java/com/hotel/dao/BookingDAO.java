package com.hotel.dao;

import com.hotel.model.Booking;
import com.hotel.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for the bookings table.
 * Includes revenue calculation and conflict-detection queries.
 */
public class BookingDAO {

    private final Connection conn;

    public BookingDAO() {
        this.conn = DatabaseConnection.getInstance().getConnection();
    }

    /**
     * Inserts a new booking record and returns the generated booking ID.
     * Now stores planned_checkout_date and total_amount.
     */
    public int addBooking(Booking booking) throws SQLException {
        String sql = "INSERT INTO bookings "
                   + "(customer_name, room_number, check_in_date, planned_checkout_date, check_out_date, total_amount) "
                   + "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, booking.getCustomerName());
            ps.setInt(2, booking.getRoomNumber());
            ps.setString(3, booking.getCheckInDate());
            ps.setString(4, booking.getPlannedCheckOutDate());
            ps.setString(5, booking.getCheckOutDate());
            ps.setDouble(6, booking.getTotalAmount());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        }
        return -1;
    }

    /** Fetches all bookings ordered by id descending (newest first). */
    public List<Booking> getAllBookings() throws SQLException {
        List<Booking> bookings = new ArrayList<>();
        String sql = "SELECT id, customer_name, room_number, check_in_date, "
                   + "planned_checkout_date, check_out_date, total_amount "
                   + "FROM bookings ORDER BY id DESC";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) bookings.add(mapRow(rs));
        }
        return bookings;
    }

    /**
     * Returns all ACTIVE (not checked-out) bookings for a specific room.
     * Used for date-range conflict detection.
     */
    public List<Booking> getActiveBookingsForRoom(int roomNumber) throws SQLException {
        List<Booking> bookings = new ArrayList<>();
        String sql = "SELECT id, customer_name, room_number, check_in_date, "
                   + "planned_checkout_date, check_out_date, total_amount "
                   + "FROM bookings WHERE room_number = ? "
                   + "AND (check_out_date IS NULL OR check_out_date = '')";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, roomNumber);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) bookings.add(mapRow(rs));
            }
        }
        return bookings;
    }

    /** Sets the check-out date for an active booking (by booking ID). */
    public void checkoutBooking(int bookingId, String checkOutDate) throws SQLException {
        String sql = "UPDATE bookings SET check_out_date = ? WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, checkOutDate);
            ps.setInt(2, bookingId);
            ps.executeUpdate();
        }
    }

    /** Returns the active booking for a given room number, or null. */
    public Booking getActiveBookingForRoom(int roomNumber) throws SQLException {
        String sql = "SELECT id, customer_name, room_number, check_in_date, "
                   + "planned_checkout_date, check_out_date, total_amount "
                   + "FROM bookings WHERE room_number = ? "
                   + "AND (check_out_date IS NULL OR check_out_date = '') "
                   + "ORDER BY id DESC LIMIT 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, roomNumber);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    /** Counts the number of active bookings (no check-out date). */
    public int countActiveBookings() throws SQLException {
        String sql = "SELECT COUNT(*) FROM bookings WHERE check_out_date IS NULL OR check_out_date = ''";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    /**
     * Returns total revenue from all bookings (sum of total_amount).
     * Revenue tracking for the dashboard.
     */
    public double getTotalRevenue() throws SQLException {
        String sql = "SELECT SUM(total_amount) FROM bookings";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            return rs.next() ? rs.getDouble(1) : 0.0;
        }
    }

    // Helper: maps a ResultSet row to a Booking object
    private Booking mapRow(ResultSet rs) throws SQLException {
        return new Booking(
                rs.getInt("id"),
                rs.getString("customer_name"),
                rs.getInt("room_number"),
                rs.getString("check_in_date"),
                rs.getString("planned_checkout_date"),
                rs.getString("check_out_date"),
                rs.getDouble("total_amount")
        );
    }
}
