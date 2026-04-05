package com.hotel.service;

import com.hotel.dao.RoomDAO;
import com.hotel.model.Room;

import java.sql.SQLException;
import java.util.List;

/**
 * Service layer for room-related business logic.
 * Acts as an intermediary between the controller and RoomDAO.
 */
public class RoomService {

    private final RoomDAO roomDAO;

    public RoomService() {
        this.roomDAO = new RoomDAO();
    }

    /**
     * Validates and adds a new room.
     * @throws IllegalArgumentException if validation fails
     * @throws SQLException on DB error
     */
    public void addRoom(int roomNumber, String type, double price) throws SQLException {
        if (roomNumber <= 0) {
            throw new IllegalArgumentException("Room number must be a positive integer.");
        }
        if (type == null || type.isBlank()) {
            throw new IllegalArgumentException("Room type cannot be empty.");
        }
        if (price <= 0) {
            throw new IllegalArgumentException("Price must be greater than zero.");
        }
        // Check for duplicate
        if (roomDAO.getRoomByNumber(roomNumber) != null) {
            throw new IllegalArgumentException("Room " + roomNumber + " already exists.");
        }
        roomDAO.addRoom(new Room(roomNumber, type, price, true));
    }

    /** Returns all rooms from the database. */
    public List<Room> getAllRooms() throws SQLException {
        return roomDAO.getAllRooms();
    }

    /** Returns rooms matching a search keyword. */
    public List<Room> searchRooms(String keyword) throws SQLException {
        if (keyword == null || keyword.isBlank()) {
            return getAllRooms();
        }
        return roomDAO.searchRooms(keyword);
    }

    /** Returns total room count. */
    public int getTotalRooms() throws SQLException {
        return roomDAO.countTotalRooms();
    }

    /** Returns count of available rooms. */
    public int getAvailableRooms() throws SQLException {
        return roomDAO.countAvailableRooms();
    }

    /** Returns count of booked (unavailable) rooms. */
    public int getBookedRooms() throws SQLException {
        return getTotalRooms() - getAvailableRooms();
    }
}
