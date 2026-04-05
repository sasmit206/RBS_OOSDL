package com.hotel.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Singleton utility class for managing the SQLite database connection.
 * Creates the required tables if they don't exist and seeds sample data.
 */
public class DatabaseConnection {

    private static final String DB_URL = "jdbc:sqlite:hotel.db";
    private static DatabaseConnection instance;
    private Connection connection;

    // Private constructor — enforces singleton
    private DatabaseConnection() {
        try {
            connection = DriverManager.getConnection(DB_URL);
            initializeTables();
            insertSampleData();
        } catch (SQLException e) {
            System.err.println("Failed to initialise database: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns the singleton instance of DatabaseConnection.
     */
    public static synchronized DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    /** Returns the live JDBC connection. */
    public Connection getConnection() {
        return connection;
    }

    /**
     * Creates the `rooms` and `bookings` tables if they do not already exist.
     */
    private void initializeTables() throws SQLException {
        String createRooms = """
                CREATE TABLE IF NOT EXISTS rooms (
                    room_number INTEGER PRIMARY KEY,
                    type        TEXT    NOT NULL,
                    price       REAL    NOT NULL,
                    available   INTEGER NOT NULL DEFAULT 1
                );
                """;

        // Base bookings table (created on first run)
        String createBookings = """
                CREATE TABLE IF NOT EXISTS bookings (
                    id                   INTEGER PRIMARY KEY AUTOINCREMENT,
                    customer_name        TEXT    NOT NULL,
                    room_number          INTEGER NOT NULL,
                    check_in_date        TEXT    NOT NULL,
                    planned_checkout_date TEXT,
                    check_out_date        TEXT,
                    total_amount          REAL    NOT NULL DEFAULT 0,
                    FOREIGN KEY (room_number) REFERENCES rooms(room_number)
                );
                """;

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(createRooms);
            stmt.execute(createBookings);
            // Migrate existing databases — silently ignored if columns already exist
            migrateColumn(stmt, "bookings", "planned_checkout_date", "TEXT");
            migrateColumn(stmt, "bookings", "total_amount",          "REAL NOT NULL DEFAULT 0");
        }
    }

    /** Adds a column to an existing table only if it doesn't already exist. */
    private void migrateColumn(Statement stmt, String table, String column, String type) {
        try {
            stmt.execute("ALTER TABLE " + table + " ADD COLUMN " + column + " " + type);
        } catch (SQLException ignored) {
            // Column already exists — safe to ignore
        }
    }

    /**
     * Inserts sample room data only if the rooms table is empty.
     */
    private void insertSampleData() throws SQLException {
        String checkEmpty = "SELECT COUNT(*) FROM rooms";
        try (Statement stmt = connection.createStatement();
             var rs = stmt.executeQuery(checkEmpty)) {
            if (rs.next() && rs.getInt(1) == 0) {
                String insertRooms = """
                        INSERT INTO rooms (room_number, type, price, available) VALUES
                            (101, 'Single',  89.99,  1),
                            (102, 'Single',  89.99,  1),
                            (103, 'Single',  89.99,  1),
                            (104, 'Single',  89.99,  1),
                            (201, 'Double',  149.99, 1),
                            (202, 'Double',  149.99, 1),
                            (203, 'Double',  149.99, 1),
                            (204, 'Double',  149.99, 1),
                            (301, 'Deluxe',  199.99, 1),
                            (302, 'Deluxe',  199.99, 1),
                            (303, 'Deluxe',  199.99, 1),
                            (304, 'Deluxe',  199.99, 1),
                            (401, 'Suite',   299.99, 1),
                            (402, 'Suite',   299.99, 1),
                            (403, 'Suite',   299.99, 1);
                        """;
                stmt.execute(insertRooms);
                System.out.println("Sample room data inserted.");
            }
        }
    }
}
