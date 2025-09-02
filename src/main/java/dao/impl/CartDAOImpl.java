package dao.impl;

import dao.CartDAO;
import model.CartItem;
import util.Database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CartDAOImpl implements CartDAO {
    
    public CartDAOImpl() {}

    @Override
    public List<CartItem> findByUser(int userId) throws SQLException {
        List<CartItem> list = new ArrayList<>();
         String sql = "SELECT ci.eventId AS event_id, ci.quantity AS quantity,"
         		+ "e.title, e.venue, e.day, e.price, e.sold, e.capacity "
         		+ "FROM cartItems ci"
         		+ "  JOIN events e ON ci.eventId = e.id "
         		+ "WHERE ci.userId = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
               System.out.println(rs.getInt("event_id"));
               System.out.println(rs.getInt("quantity"));
               while (rs.next()) {
                    CartItem ci = new CartItem(
                        userId,
                        new model.Event(
                            rs.getInt("event_id"),
                            rs.getString("title"),
                            rs.getString("venue"),
                            rs.getString("day"),
                            rs.getDouble("price"),
                            rs.getInt("sold"),
                            rs.getInt("capacity"),
                            false
                        ),
                        rs.getInt("quantity")
                    );
                    list.add(ci);
                }
            }
        }
        return list;
    }

    //Adds a cart item or updates quantity if it already exists
       
    @Override
    public void addOrUpdate(CartItem item) throws SQLException {
        String upsert =
            "INSERT INTO cartItems(userId, eventId, quantity) VALUES(?,?,?) " +
            "ON CONFLICT(userId,eventId) DO UPDATE SET quantity = excluded.quantity";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(upsert)) {
            ps.setInt(1, item.getUserId());
            ps.setInt(2, item.getEvent().getId());
            ps.setInt(3, item.getQuantity());
            ps.executeUpdate();
        }
    }

    @Override
    public void remove(int userId, int eventId) throws SQLException {
        String sql = "DELETE FROM cartItems WHERE userId = ? AND eventId = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, eventId);
            ps.executeUpdate();
        }
    }

    @Override
    public void clearByUser(int userId) throws SQLException {
        String sql = "DELETE FROM cartItems WHERE userId = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.executeUpdate();
        }
    }
}
