package com.hotel.model;

/**
 * Model class representing a hotel room.
 */
public class Room {

    private int roomNumber;
    private String type;       // Single, Double, Suite
    private double price;
    private boolean available;

    public Room() {}

    public Room(int roomNumber, String type, double price, boolean available) {
        this.roomNumber = roomNumber;
        this.type = type;
        this.price = price;
        this.available = available;
    }

    // --- Getters & Setters ---

    public int getRoomNumber() { return roomNumber; }
    public void setRoomNumber(int roomNumber) { this.roomNumber = roomNumber; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }

    /** Convenience label for the TableView. */
    public String getAvailabilityLabel() {
        return available ? "Available" : "Booked";
    }

    @Override
    public String toString() {
        return "Room{" + roomNumber + ", " + type + ", $" + price + ", " + getAvailabilityLabel() + "}";
    }
}
