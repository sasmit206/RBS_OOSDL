package com.hotel.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.Stage;

/**
 * Controller for the Booking Confirmation popup window.
 * Receives booking details from MainController and displays them.
 */
public class BookingConfirmationController {

    @FXML private Label lblCustomer;
    @FXML private Label lblRoom;
    @FXML private Label lblType;
    @FXML private Label lblPrice;
    @FXML private Label lblCheckIn;
    @FXML private Label lblBookingId;

    /**
     * Populates the confirmation window with booking details.
     * Called by MainController after creating the window.
     */
    public void setBookingDetails(String customerName, int roomNumber, String roomType,
                                  double pricePerNight, String checkInDate, int bookingId) {
        lblCustomer.setText(customerName);
        lblRoom.setText(String.valueOf(roomNumber));
        lblType.setText(roomType);
        lblPrice.setText(String.format("$%.2f", pricePerNight));
        lblCheckIn.setText(checkInDate);
        lblBookingId.setText("#" + bookingId);
    }

    @FXML
    private void handleClose() {
        Stage stage = (Stage) lblCustomer.getScene().getWindow();
        stage.close();
    }
}
