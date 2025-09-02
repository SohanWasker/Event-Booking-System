package dao;

import model.CartItem;
import java.sql.SQLException;
import java.util.List;

public interface CartDAO {
    // Return all cart items for a given user id 
    List<CartItem> findByUser(int userId) throws SQLException;

    // Insert a new cart item or update if it already exists 
    void addOrUpdate(CartItem item) throws SQLException;

    // Remove a single event from a user’s cart 
    void remove(int userId, int eventId) throws SQLException;

    // Clear every item in that user’s cart 
    void clearByUser(int userId) throws SQLException;
}
