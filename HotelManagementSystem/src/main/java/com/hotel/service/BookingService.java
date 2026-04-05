package com.hotel.service;

import com.hotel.dao.BookingDAO;
import com.hotel.dao.RoomDAO;
import com.hotel.model.Booking;
import com.hotel.model.Room;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Service layer for booking-related business logic.
 * Implements date-conflict detection, revenue calculation, and clean
 * separation from both the DAO and the controller.
 */
public class BookingService {

    private final BookingDAO bookingDAO;
    private final RoomDAO    roomDAO;

    public BookingService() {
        this.bookingDAO = new BookingDAO();
        this.roomDAO    = new RoomDAO();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Conflict Detection
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Checks whether a room is free for the requested date range.
     *
     * Conflict rule:
     *   new_checkIn  < existing_plannedCheckOut
     *   AND
     *   new_checkOut > existing_checkIn
     *
     * Edge case: new_checkIn == existing_plannedCheckOut is allowed (back-to-back).
     *
     * @return true if no conflict exists (room is available), false otherwise
     */
    public boolean isRoomAvailable(int roomNumber, LocalDate checkIn, LocalDate checkOut)
            throws SQLException {

        List<Booking> active = bookingDAO.getActiveBookingsForRoom(roomNumber);

        for (Booking b : active) {
            LocalDate existingCheckIn = LocalDate.parse(b.getCheckInDate());

            // If planned checkout was not set, treat booking as open-ended (conflict)
            if (b.getPlannedCheckOutDate() == null || b.getPlannedCheckOutDate().isBlank()) {
                return false;
            }
            LocalDate existingCheckOut = LocalDate.parse(b.getPlannedCheckOutDate());

            // Overlap check (back-to-back same day is allowed)
            boolean overlaps = checkIn.isBefore(existingCheckOut)
                            && checkOut.isAfter(existingCheckIn);
            if (overlaps) return false;
        }
        return true;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Booking
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Creates a booking after full validation:
     *  - Input validation
     *  - Room existence + availability check
     *  - Date-range conflict detection
     *  - Total amount calculation (nights × price per night)
     *
     * @return the generated booking ID
     */
    public int bookRoom(String customerName, int roomNumber,
                        LocalDate checkIn, LocalDate checkOut)
            throws IllegalArgumentException, SQLException {

        // ── Input validation ──────────────────────────────────────────────────
        if (customerName == null || customerName.isBlank())
            throw new IllegalArgumentException("Customer name cannot be empty.");
        if (roomNumber <= 0)
            throw new IllegalArgumentException("Please enter a valid room number.");
        if (checkIn == null)
            throw new IllegalArgumentException("Check-in date cannot be empty.");
        if (checkOut == null)
            throw new IllegalArgumentException("Check-out date cannot be empty.");
        if (!checkOut.isAfter(checkIn))
            throw new IllegalArgumentException("Check-out date must be after check-in date.");

        // ── Room existence ────────────────────────────────────────────────────
        Room room = roomDAO.getRoomByNumber(roomNumber);
        if (room == null)
            throw new IllegalArgumentException("Room " + roomNumber + " does not exist.");
        if (!room.isAvailable())
            throw new IllegalArgumentException("Room " + roomNumber + " is currently not available.");

        // ── Conflict detection ────────────────────────────────────────────────
        if (!isRoomAvailable(roomNumber, checkIn, checkOut))
            throw new IllegalArgumentException(
                "Room " + roomNumber + " is already booked for that period. "
                + "Please choose different dates.");

        // ── Revenue calculation ───────────────────────────────────────────────
        long nights = ChronoUnit.DAYS.between(checkIn, checkOut);
        double totalAmount = nights * room.getPrice();

        // ── Persist ───────────────────────────────────────────────────────────
        Booking booking = new Booking(
                0, customerName, roomNumber,
                checkIn.toString(), checkOut.toString(),
                null, totalAmount);

        int bookingId = bookingDAO.addBooking(booking);
        roomDAO.updateAvailability(roomNumber, false);
        return bookingId;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Checkout
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Checks out the active booking for a given room number.
     * Sets actual checkout date to today and marks the room available.
     *
     * @return the booking ID that was checked out
     */
    public int checkoutRoom(int roomNumber) throws IllegalArgumentException, SQLException {
        if (roomNumber <= 0)
            throw new IllegalArgumentException("Please enter a valid room number.");

        Room room = roomDAO.getRoomByNumber(roomNumber);
        if (room == null)
            throw new IllegalArgumentException("Room " + roomNumber + " does not exist.");
        if (room.isAvailable())
            throw new IllegalArgumentException("Room " + roomNumber + " has no active booking.");

        Booking active = bookingDAO.getActiveBookingForRoom(roomNumber);
        if (active == null)
            throw new IllegalArgumentException("No active booking found for room " + roomNumber + ".");

        bookingDAO.checkoutBooking(active.getId(), LocalDate.now().toString());
        roomDAO.updateAvailability(roomNumber, true);
        return active.getId();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Queries
    // ─────────────────────────────────────────────────────────────────────────

    /** Returns all bookings (for the table view). */
    public List<Booking> getAllBookings() throws SQLException {
        return bookingDAO.getAllBookings();
    }

    /** Returns count of active bookings (for the dashboard). */
    public int getActiveBookingsCount() throws SQLException {
        return bookingDAO.countActiveBookings();
    }

    /**
     * Returns total revenue from all bookings.
     * Revenue = SUM(total_amount) across all booking records.
     */
    public double getTotalRevenue() throws SQLException {
        return bookingDAO.getTotalRevenue();
    }
}
