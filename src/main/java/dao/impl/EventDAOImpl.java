package dao.impl;

import dao.EventDAO;
import model.Event;
import util.Database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC implementation of EventDAO using SQLite.
 */
public class EventDAOImpl implements EventDAO {
    @Override
    public List<Event> findAll() {
        List<Event> list = new ArrayList<>();
        try (Connection conn = Database.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM events")) {
            while (rs.next()) {
                // Maps each row to an Event object
                list.add(new Event(
                    rs.getInt("id"),
                    rs.getString("title"),
                    rs.getString("venue"),
                    rs.getString("day"),
                    rs.getDouble("price"),
                    rs.getInt("sold"),
                    rs.getInt("capacity"),
                    rs.getBoolean("disabled")
                ));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return list;
    }

    @Override
    public Event findById(int id) throws SQLException {
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM events WHERE id = ?")) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new Event(
                    id,
                    rs.getString("title"),
                    rs.getString("venue"),
                    rs.getString("day"),
                    rs.getDouble("price"),
                    rs.getInt("sold"),
                    rs.getInt("capacity"),
                    rs.getBoolean("disabled")
                );
            }
        }
        return null;
    }

    @Override
    public void add(Event e) throws SQLException {
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "INSERT INTO events(title, venue, day, price, sold, capacity, disabled) VALUES(?,?,?,?,?,?,?)")) {
            // Fill in parameters and execute
            ps.setString(1, e.getTitle());
            ps.setString(2, e.getVenue());
            ps.setString(3, e.getDay());
            ps.setDouble(4, e.getPrice());
            ps.setInt(5, e.getSold());
            ps.setInt(6, e.getCapacity());
            ps.setBoolean(7, e.isDisabled());
            ps.executeUpdate();
        }
    }

    @Override
    public void update(Event e) throws SQLException {
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "UPDATE events SET title=?, venue=?, day=?, price=?, sold=?, capacity=?, disabled=? WHERE id=?")) {
            // Update fields
            ps.setString(1, e.getTitle());
            ps.setString(2, e.getVenue());
            ps.setString(3, e.getDay());
            ps.setDouble(4, e.getPrice());
            ps.setInt(5, e.getSold());
            ps.setInt(6, e.getCapacity());
            ps.setBoolean(7, e.isDisabled());
            ps.setInt(8, e.getId());
            ps.executeUpdate();
        }
    }

    @Override
    public void delete(int id) throws SQLException {
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM events WHERE id = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    @Override
    public void setDisabled(int id, boolean disabled) throws SQLException {
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "UPDATE events SET disabled = ? WHERE id = ?")) {
            ps.setBoolean(1, disabled);
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }
}