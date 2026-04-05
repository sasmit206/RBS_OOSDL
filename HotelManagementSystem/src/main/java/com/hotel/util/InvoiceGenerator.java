package com.hotel.util;

import com.hotel.model.Booking;
import com.hotel.model.Room;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

/**
 * Utility class for generating plain-text booking receipts / invoices.
 * Uses FileChooser so the user picks the save location.
 */
public class InvoiceGenerator {

    private static final String HOTEL_NAME    = "Grand Vista Hotel";
    private static final String HOTEL_ADDRESS = "123 Sunset Boulevard, Cityville";
    private static final String HOTEL_PHONE   = "+1 (555) 000-1234";
    private static final String SEPARATOR     = "=".repeat(50);
    private static final String THIN_SEP      = "-".repeat(50);

    private InvoiceGenerator() {}  // utility class — no instantiation

    /**
     * Opens a FileChooser and writes a formatted TXT receipt for the given booking.
     *
     * @param booking   the completed booking record
     * @param room      the room that was booked (used for type and price per night)
     * @param owner     the JavaFX window to anchor the FileChooser to
     * @return true if the file was successfully written, false if cancelled
     */
    public static boolean generate(Booking booking, Room room, Window owner) {
        // ── File picker ───────────────────────────────────────────────────────
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Save Invoice");
        chooser.setInitialFileName("invoice_booking_" + booking.getId() + ".txt");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Text Files (*.txt)", "*.txt"));

        File file = chooser.showSaveDialog(owner);
        if (file == null) return false;  // user cancelled

        // ── Calculate totals ──────────────────────────────────────────────────
        LocalDate checkIn  = LocalDate.parse(booking.getCheckInDate());
        LocalDate checkOut;
        if (booking.getPlannedCheckOutDate() != null && !booking.getPlannedCheckOutDate().isBlank()) {
            checkOut = LocalDate.parse(booking.getPlannedCheckOutDate());
        } else {
            checkOut = LocalDate.now();
        }
        long   nights      = ChronoUnit.DAYS.between(checkIn, checkOut);
        double pricePerNight = room.getPrice();
        double total       = nights * pricePerNight;

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd MMM yyyy");

        // ── Write receipt ─────────────────────────────────────────────────────
        try (PrintWriter pw = new PrintWriter(new FileWriter(file))) {
            pw.println(SEPARATOR);
            pw.printf("  %-48s%n", HOTEL_NAME);
            pw.printf("  %-48s%n", HOTEL_ADDRESS);
            pw.printf("  Tel: %-44s%n", HOTEL_PHONE);
            pw.println(SEPARATOR);
            pw.println();
            pw.printf("  %-28s %s%n", "BOOKING RECEIPT",
                    "Issue Date: " + LocalDate.now().format(fmt));
            pw.println();
            pw.println(THIN_SEP);
            pw.printf("  %-28s %s%n", "Booking ID:",      "#" + booking.getId());
            pw.printf("  %-28s %s%n", "Customer Name:",   booking.getCustomerName());
            pw.printf("  %-28s %s%n", "Room Number:",     booking.getRoomNumber());
            pw.printf("  %-28s %s%n", "Room Type:",       room.getType());
            pw.println(THIN_SEP);
            pw.printf("  %-28s %s%n", "Check-In Date:",   checkIn.format(fmt));
            pw.printf("  %-28s %s%n", "Check-Out Date:",  checkOut.format(fmt));
            pw.printf("  %-28s %d night(s)%n", "Duration:", nights);
            pw.println(THIN_SEP);
            pw.printf("  %-28s $%.2f%n", "Price per Night:", pricePerNight);
            pw.printf("  %-28s %d%n",    "Number of Nights:", nights);
            pw.println(THIN_SEP);
            pw.printf("  %-28s $%.2f%n", "TOTAL AMOUNT:", total);
            pw.println(SEPARATOR);
            pw.println();
            pw.println("  Thank you for choosing " + HOTEL_NAME + "!");
            pw.println("  We hope to see you again soon.");
            pw.println();
            pw.println(SEPARATOR);
        } catch (IOException e) {
            return false;
        }

        return true;
    }
}
