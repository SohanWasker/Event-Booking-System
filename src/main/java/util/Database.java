package util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Utility class for initializing and providing database connections.
 */
public class Database {
    private static final String URL = "jdbc:sqlite:TheSuperEvent.db";

    // Creation of tables if they do not exist
     
    public static void init() throws SQLException {
        try (Connection conn = getConnection(); Statement st = conn.createStatement()) {
            st.execute("CREATE TABLE IF NOT EXISTS users (id INTEGER PRIMARY KEY AUTOINCREMENT, username TEXT UNIQUE, password TEXT, preferredName TEXT, isAdmin BOOLEAN)");
            st.execute("CREATE TABLE IF NOT EXISTS events (id INTEGER PRIMARY KEY AUTOINCREMENT, title TEXT, venue TEXT, day TEXT, price REAL, sold INTEGER, capacity INTEGER, disabled BOOLEAN)");
            st.execute("CREATE TABLE IF NOT EXISTS carts (userId INTEGER PRIMARY KEY)");
            st.execute("CREATE TABLE IF NOT EXISTS cartItems (" +
                    "userId INTEGER, " +
                    "eventId INTEGER, " +
                    "quantity INTEGER, " +
                    "PRIMARY KEY (userId, eventId))");

            st.execute("CREATE TABLE IF NOT EXISTS orders (orderNumber TEXT PRIMARY KEY, userId INTEGER, dateTime TEXT, totalPrice REAL)");
            st.execute("CREATE TABLE IF NOT EXISTS orderItems (orderNumber TEXT, eventId INTEGER, quantity INTEGER)");
            
            int count = 0;
            try (ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM events")) {
                rs.next();
                count = rs.getInt(1);
            }

            if (count == 0) {
                // Loads events.dat from classpath
                try (InputStream in = Thread.currentThread()
                                            .getContextClassLoader()
                                            .getResourceAsStream("events.dat");
                     BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {
                    String line;
                    PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO events(title,venue,day,price,sold,capacity,disabled) VALUES(?,?,?,?,?,?,?)"
                    );
                    while ((line = reader.readLine()) != null) {
                        String[] parts = line.split(";");
                        ps.setString(1, parts[0]);
                        ps.setString(2, parts[1]);
                        ps.setString(3, parts[2]);
                        ps.setDouble(4, Double.parseDouble(parts[3]));
                        ps.setInt(5, Integer.parseInt(parts[4]));
                        ps.setInt(6, Integer.parseInt(parts[5]));
                        ps.setBoolean(7, false);
                        ps.addBatch();
                    }
                    ps.executeBatch();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
           
        }
    }
    //Returns a new DB connection
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL);
    }
}