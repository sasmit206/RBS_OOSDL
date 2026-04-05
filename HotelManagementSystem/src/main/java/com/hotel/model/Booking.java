package com.hotel.model;

/**
 * Model class representing a hotel booking.
 * Contains both the planned checkout date (set at booking time for
 * conflict detection / invoice) and the actual checkout date (set on checkout).
 */
public class Booking {

    private int id;
    private String customerName;
    private int roomNumber;
    private String checkInDate;          // ISO date (YYYY-MM-DD)
    private String plannedCheckOutDate;  // planned checkout — set at booking time
    private String checkOutDate;         // actual checkout — null until checkout
    private double totalAmount;          // price × nights, stored for revenue tracking

    public Booking() {}

    /** Constructor used when fetching from DB (includes all persisted fields). */
    public Booking(int id, String customerName, int roomNumber,
                   String checkInDate, String plannedCheckOutDate,
                   String checkOutDate, double totalAmount) {
        this.id = id;
        this.customerName = customerName;
        this.roomNumber = roomNumber;
        this.checkInDate = checkInDate;
        this.plannedCheckOutDate = plannedCheckOutDate;
        this.checkOutDate = checkOutDate;
        this.totalAmount = totalAmount;
    }

    // ── Getters & Setters ──────────────────────────────────────────────────────

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String v) { this.customerName = v; }

    public int getRoomNumber() { return roomNumber; }
    public void setRoomNumber(int v) { this.roomNumber = v; }

    public String getCheckInDate() { return checkInDate; }
    public void setCheckInDate(String v) { this.checkInDate = v; }

    public String getPlannedCheckOutDate() { return plannedCheckOutDate; }
    public void setPlannedCheckOutDate(String v) { this.plannedCheckOutDate = v; }

    public String getCheckOutDate() { return checkOutDate; }
    public void setCheckOutDate(String v) { this.checkOutDate = v; }

    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double v) { this.totalAmount = v; }

    /** Convenience label for the status column in TableView. */
    public String getStatus() {
        return (checkOutDate == null || checkOutDate.isBlank()) ? "Active" : "Checked Out";
    }

    @Override
    public String toString() {
        return "Booking{id=" + id + ", customer=" + customerName + ", room=" + roomNumber + "}";
    }
}
