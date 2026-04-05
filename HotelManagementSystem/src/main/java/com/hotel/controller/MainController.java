package com.hotel.controller;

import com.hotel.model.Booking;
import com.hotel.model.Room;
import com.hotel.service.BookingService;
import com.hotel.service.RoomService;
import com.hotel.util.InvoiceGenerator;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;

/**
 * Main application controller.
 * Routes sidebar navigation and delegates all business logic to the
 * Service layer. All database calls run on background threads.
 */
public class MainController implements Initializable {

    // ── Services ──────────────────────────────────────────────────────────────
    private final RoomService    roomService    = new RoomService();
    private final BookingService bookingService = new BookingService();

    // ── Navigation ────────────────────────────────────────────────────────────
    @FXML private VBox  dashboardPane, roomsPane, bookingsPane;
    @FXML private Button btnNavDashboard, btnNavRooms, btnNavBookings;

    // ── Dashboard ─────────────────────────────────────────────────────────────
    @FXML private Label txtTotalRooms, txtAvailableRooms, txtBookedRooms,
                        txtActiveBookings, txtTotalRevenue;
    @FXML private Label lblRevenueSummary;

    // ── Room form ─────────────────────────────────────────────────────────────
    @FXML private TextField tfRoomNumber, tfRoomPrice, tfRoomSearch;
    @FXML private ComboBox<String> cbRoomType;
    @FXML private Label errRoomNumber, errRoomType, errRoomPrice;
    @FXML private Button btnAddRoom;

    // ── Room table ────────────────────────────────────────────────────────────
    @FXML private TableView<Room>         roomTable;
    @FXML private TableColumn<Room, Integer> colRoomNumber;
    @FXML private TableColumn<Room, String>  colRoomType, colRoomStatus;
    @FXML private TableColumn<Room, Double>  colRoomPrice;

    // ── Booking form ──────────────────────────────────────────────────────────
    @FXML private TextField tfCustomerName, tfBookRoomNumber;
    @FXML private DatePicker dpCheckIn, dpCheckOut;
    @FXML private Label errCustomerName, errBookRoomNumber, errCheckIn, errCheckOut;
    @FXML private Button btnBookRoom;

    // ── Checkout form ─────────────────────────────────────────────────────────
    @FXML private TextField tfCheckoutRoomNumber;
    @FXML private Label errCheckoutRoom;
    @FXML private Button btnCheckout, btnExportReceipt;

    // ── Booking table ─────────────────────────────────────────────────────────
    @FXML private TableView<Booking>           bookingTable;
    @FXML private TableColumn<Booking, Integer> colBookingId, colBookingRoom;
    @FXML private TableColumn<Booking, String>  colCustomerName, colCheckIn,
                                                colPlannedCheckOut, colCheckOut,
                                                colTotalAmount, colBookingStatus;

    // ─────────────────────────────────────────────────────────────────────────
    // Initialise
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupRoomTable();
        setupBookingTable();
        setupRoomForm();
        setupBookingForm();
        refreshDashboard();
        refreshRoomTable(null);
        refreshBookingTable();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Navigation
    // ─────────────────────────────────────────────────────────────────────────

    @FXML private void showDashboard() {
        show(dashboardPane);
        setActive(btnNavDashboard);
        refreshDashboard();
    }

    @FXML private void showRooms() {
        show(roomsPane);
        setActive(btnNavRooms);
        refreshRoomTable(null);
    }

    @FXML private void showBookings() {
        show(bookingsPane);
        setActive(btnNavBookings);
        refreshBookingTable();
    }

    private void show(VBox pane) {
        dashboardPane.setVisible(false); dashboardPane.setManaged(false);
        roomsPane.setVisible(false);     roomsPane.setManaged(false);
        bookingsPane.setVisible(false);  bookingsPane.setManaged(false);
        pane.setVisible(true);           pane.setManaged(true);
    }

    private void setActive(Button btn) {
        btnNavDashboard.getStyleClass().remove("nav-btn-active");
        btnNavRooms.getStyleClass().remove("nav-btn-active");
        btnNavBookings.getStyleClass().remove("nav-btn-active");
        btn.getStyleClass().add("nav-btn-active");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Table Setup
    // ─────────────────────────────────────────────────────────────────────────

    private void setupRoomTable() {
        colRoomNumber.setCellValueFactory(new PropertyValueFactory<>("roomNumber"));
        colRoomType.setCellValueFactory(new PropertyValueFactory<>("type"));
        colRoomPrice.setCellValueFactory(new PropertyValueFactory<>("price"));
        colRoomStatus.setCellValueFactory(new PropertyValueFactory<>("availabilityLabel"));

        // Colour-code status badge
        colRoomStatus.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setGraphic(null); return; }
                Label badge = new Label(item);
                badge.getStyleClass().add(
                    "Available".equals(item) ? "badge-available" : "badge-booked");
                setGraphic(badge);
                setText(null);
            }
        });
    }

    private void setupBookingTable() {
        colBookingId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colCustomerName.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        colBookingRoom.setCellValueFactory(new PropertyValueFactory<>("roomNumber"));
        colCheckIn.setCellValueFactory(new PropertyValueFactory<>("checkInDate"));

        colPlannedCheckOut.setCellValueFactory(c -> {
            String v = c.getValue().getPlannedCheckOutDate();
            return new SimpleStringProperty(v != null && !v.isBlank() ? v : "—");
        });
        colCheckOut.setCellValueFactory(c -> {
            String v = c.getValue().getCheckOutDate();
            return new SimpleStringProperty(v != null && !v.isBlank() ? v : "—");
        });
        colTotalAmount.setCellValueFactory(c ->
            new SimpleStringProperty(String.format("$%.2f", c.getValue().getTotalAmount())));
        colBookingStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        // Status badge
        colBookingStatus.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setGraphic(null); return; }
                Label badge = new Label(item);
                badge.getStyleClass().add(
                    "Active".equals(item) ? "badge-available" : "badge-booked");
                setGraphic(badge);
                setText(null);
            }
        });
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Form setup (inline validation)
    // ─────────────────────────────────────────────────────────────────────────

    private void setupRoomForm() {
        cbRoomType.setItems(FXCollections.observableArrayList(
                "Single", "Double", "Suite", "Deluxe", "Presidential"));
        btnAddRoom.setDisable(true);

        tfRoomNumber.textProperty().addListener((o, ov, nv) -> validateRoomForm());
        cbRoomType.valueProperty().addListener((o, ov, nv) -> validateRoomForm());
        tfRoomPrice.textProperty().addListener((o, ov, nv) -> validateRoomForm());
    }

    private void setupBookingForm() {
        btnBookRoom.setDisable(true);
        btnCheckout.setDisable(true);
        btnExportReceipt.setDisable(true);

        tfCustomerName.textProperty().addListener((o, ov, nv) -> validateBookingForm());
        tfBookRoomNumber.textProperty().addListener((o, ov, nv) -> validateBookingForm());
        dpCheckIn.valueProperty().addListener((o, ov, nv) -> validateBookingForm());
        dpCheckOut.valueProperty().addListener((o, ov, nv) -> validateBookingForm());

        tfCheckoutRoomNumber.textProperty().addListener((o, ov, nv) -> {
            boolean empty = tfCheckoutRoomNumber.getText().isBlank();
            btnCheckout.setDisable(empty);
            btnExportReceipt.setDisable(empty);
        });
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Inline validation
    // ─────────────────────────────────────────────────────────────────────────

    private boolean validateRoomForm() {
        boolean ok = true;

        // Room number
        String numStr = tfRoomNumber.getText().trim();
        if (numStr.isBlank()) {
            setError(errRoomNumber, "Room number is required.");  ok = false;
        } else {
            try { int n = Integer.parseInt(numStr); setError(errRoomNumber, n <= 0 ? "Must be > 0." : null); if (n <= 0) ok = false; }
            catch (NumberFormatException ex) { setError(errRoomNumber, "Must be a valid integer."); ok = false; }
        }

        // Type
        if (cbRoomType.getValue() == null) {
            setError(errRoomType, "Please select a room type."); ok = false;
        } else setError(errRoomType, null);

        // Price
        String priceStr = tfRoomPrice.getText().trim();
        if (priceStr.isBlank()) {
            setError(errRoomPrice, "Price is required."); ok = false;
        } else {
            try { double p = Double.parseDouble(priceStr); setError(errRoomPrice, p <= 0 ? "Must be > 0." : null); if (p <= 0) ok = false; }
            catch (NumberFormatException ex) { setError(errRoomPrice, "Must be a number."); ok = false; }
        }

        btnAddRoom.setDisable(!ok);
        return ok;
    }

    private boolean validateBookingForm() {
        boolean ok = true;

        if (tfCustomerName.getText().isBlank()) {
            setError(errCustomerName, "Name is required."); ok = false;
        } else setError(errCustomerName, null);

        String rNumStr = tfBookRoomNumber.getText().trim();
        if (rNumStr.isBlank()) {
            setError(errBookRoomNumber, "Room number is required."); ok = false;
        } else {
            try { int n = Integer.parseInt(rNumStr); setError(errBookRoomNumber, n <= 0 ? "Must be > 0." : null); if (n <= 0) ok = false; }
            catch (NumberFormatException ex) { setError(errBookRoomNumber, "Must be an integer."); ok = false; }
        }

        LocalDate ci = dpCheckIn.getValue();
        LocalDate co = dpCheckOut.getValue();

        if (ci == null) { setError(errCheckIn, "Check-in date is required."); ok = false; }
        else setError(errCheckIn, null);

        if (co == null) { setError(errCheckOut, "Check-out date is required."); ok = false; }
        else if (ci != null && !co.isAfter(ci)) {
            setError(errCheckOut, "Must be after check-in date."); ok = false;
        } else setError(errCheckOut, null);

        btnBookRoom.setDisable(!ok);
        return ok;
    }

    private void setError(Label lbl, String msg) {
        if (msg == null || msg.isBlank()) {
            lbl.setVisible(false); lbl.setManaged(false);
        } else {
            lbl.setText(msg); lbl.setVisible(true); lbl.setManaged(true);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Handlers — Room
    // ─────────────────────────────────────────────────────────────────────────

    @FXML private void handleAddRoom() {
        if (!validateRoomForm()) return;
        int roomNum    = Integer.parseInt(tfRoomNumber.getText().trim());
        String type    = cbRoomType.getValue();
        double price   = Double.parseDouble(tfRoomPrice.getText().trim());
        btnAddRoom.setDisable(true);

        runBg(() -> roomService.addRoom(roomNum, type, price),
              () -> {
                  handleClearRoomForm();
                  refreshRoomTable(null);
                  refreshDashboard();
              },
              ex -> {
                  setError(errRoomNumber, ex.getMessage());
                  btnAddRoom.setDisable(false);
              });
    }

    @FXML private void handleClearRoomForm() {
        tfRoomNumber.clear(); cbRoomType.setValue(null); tfRoomPrice.clear();
        setError(errRoomNumber, null); setError(errRoomType, null); setError(errRoomPrice, null);
        btnAddRoom.setDisable(true);
    }

    @FXML private void handleSearchRooms() {
        refreshRoomTable(tfRoomSearch.getText().trim());
    }

    @FXML private void handleClearSearch() {
        tfRoomSearch.clear(); refreshRoomTable(null);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Handlers — Booking
    // ─────────────────────────────────────────────────────────────────────────

    @FXML private void handleBookRoom() {
        if (!validateBookingForm()) return;

        String name      = tfCustomerName.getText().trim();
        int    roomNum   = Integer.parseInt(tfBookRoomNumber.getText().trim());
        LocalDate checkIn  = dpCheckIn.getValue();
        LocalDate checkOut = dpCheckOut.getValue();

        btnBookRoom.setDisable(true);

        // Run booking on background thread — captures bookingId for invoice
        Task<Integer> task = new Task<>() {
            @Override protected Integer call() throws Exception {
                return bookingService.bookRoom(name, roomNum, checkIn, checkOut);
            }
        };

        task.setOnSucceeded(e -> {
            // Clear form
            tfCustomerName.clear(); tfBookRoomNumber.clear();
            dpCheckIn.setValue(null); dpCheckOut.setValue(null);
            refreshBookingTable();
            refreshRoomTable(null);
            refreshDashboard();
        });

        task.setOnFailed(e -> {
            Platform.runLater(() -> {
                setError(errCheckIn, task.getException().getMessage());
                btnBookRoom.setDisable(false);
            });
        });

        new Thread(task).start();
    }

    @FXML private void handleCheckout() {
        String numStr = tfCheckoutRoomNumber.getText().trim();
        int roomNum;
        try { roomNum = Integer.parseInt(numStr); }
        catch (NumberFormatException ex) {
            setError(errCheckoutRoom, "Must be a valid room number."); return;
        }
        setError(errCheckoutRoom, null);

        runBg(() -> bookingService.checkoutRoom(roomNum),
              () -> {
                  tfCheckoutRoomNumber.clear();
                  refreshBookingTable();
                  refreshRoomTable(null);
                  refreshDashboard();
              },
              ex -> setError(errCheckoutRoom, ex.getMessage()));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Invoice
    // ─────────────────────────────────────────────────────────────────────────

    @FXML private void handleExportReceipt() {
        String numStr = tfCheckoutRoomNumber.getText().trim();
        int roomNum;
        try { roomNum = Integer.parseInt(numStr); }
        catch (NumberFormatException ex) {
            setError(errCheckoutRoom, "Enter room number for receipt."); return;
        }
        setError(errCheckoutRoom, null);

        Task<Object[]> task = new Task<>() {
            @Override protected Object[] call() throws Exception {
                // Fetch the booking and its room for invoice details
                Booking active = bookingService.getAllBookings().stream()
                    .filter(b -> b.getRoomNumber() == roomNum && "Active".equals(b.getStatus()))
                    .findFirst().orElse(null);
                if (active != null) {
                    Room room = roomService.getAllRooms().stream()
                        .filter(r -> r.getRoomNumber() == roomNum)
                        .findFirst().orElse(null);
                    if (room != null) return new Object[]{active, room};
                }
                return null;
            }
        };

        task.setOnSucceeded(e -> {
            Object[] data = task.getValue();
            if (data == null) {
                setError(errCheckoutRoom, "No active booking found for this room.");
                return;
            }
            Booking booking = (Booking) data[0];
            Room    room    = (Room)    data[1];
            InvoiceGenerator.generate(
                booking, room,
                btnCheckout.getScene().getWindow());
            tfCheckoutRoomNumber.clear();
        });

        task.setOnFailed(e -> setError(errCheckoutRoom, "Failed to load booking."));
        new Thread(task).start();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Dashboard Refresh
    // ─────────────────────────────────────────────────────────────────────────

    private void refreshDashboard() {
        Task<int[]> task = new Task<>() {
            @Override protected int[] call() throws Exception {
                return new int[]{
                    roomService.getTotalRooms(),
                    roomService.getAvailableRooms(),
                    roomService.getBookedRooms(),
                    bookingService.getActiveBookingsCount()
                };
            }
        };
        Task<Double> revTask = new Task<>() {
            @Override protected Double call() throws Exception {
                return bookingService.getTotalRevenue();
            }
        };

        task.setOnSucceeded(e -> {
            int[] d = task.getValue();
            Platform.runLater(() -> {
                txtTotalRooms.setText(String.valueOf(d[0]));
                txtAvailableRooms.setText(String.valueOf(d[1]));
                txtBookedRooms.setText(String.valueOf(d[2]));
                txtActiveBookings.setText(String.valueOf(d[3]));
            });
        });

        revTask.setOnSucceeded(e -> {
            double rev = revTask.getValue();
            Platform.runLater(() -> {
                String fmt = String.format("$%,.2f", rev);
                txtTotalRevenue.setText(fmt);
                lblRevenueSummary.setText(fmt);
            });
        });

        new Thread(task).start();
        new Thread(revTask).start();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Table Refresh
    // ─────────────────────────────────────────────────────────────────────────

    private void refreshRoomTable(String keyword) {
        Task<java.util.List<Room>> task = new Task<>() {
            @Override protected java.util.List<Room> call() throws Exception {
                return keyword == null || keyword.isBlank()
                    ? roomService.getAllRooms()
                    : roomService.searchRooms(keyword);
            }
        };
        task.setOnSucceeded(e ->
            Platform.runLater(() -> roomTable.setItems(
                FXCollections.observableArrayList(task.getValue()))));
        new Thread(task).start();
    }

    private void refreshBookingTable() {
        Task<java.util.List<Booking>> task = new Task<>() {
            @Override protected java.util.List<Booking> call() throws Exception {
                return bookingService.getAllBookings();
            }
        };
        task.setOnSucceeded(e ->
            Platform.runLater(() -> bookingTable.setItems(
                FXCollections.observableArrayList(task.getValue()))));
        new Thread(task).start();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Utilities
    // ─────────────────────────────────────────────────────────────────────────

    /** Functional interface for void DB operations (throws Exception). */
    @FunctionalInterface
    interface DbVoidOp { void run() throws Exception; }

    /**
     * Runs a void DB operation on a background thread,
     * then executes onSuccess on the FX thread. Fallback to Alert on error.
     */
    private void runBg(DbVoidOp op, Runnable onSuccess, java.util.function.Consumer<Throwable> onFailure) {
        Task<Void> task = new Task<>() {
            @Override protected Void call() throws Exception { op.run(); return null; }
        };
        task.setOnSucceeded(e -> Platform.runLater(onSuccess));
        task.setOnFailed(e -> Platform.runLater(() -> onFailure.accept(task.getException())));
        new Thread(task).start();
    }

}
