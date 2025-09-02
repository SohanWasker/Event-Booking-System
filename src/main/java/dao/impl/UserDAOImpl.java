package dao.impl;

import dao.UserDAO;
import model.User;
import util.Database;
import util.Encryptor;

import java.sql.*;
import java.util.Optional;

/**
 * JDBC-based implementation of UserDAO.
 */
public class UserDAOImpl implements UserDAO {
    @Override
    public Optional<User> findByUsername(String username) throws SQLException {
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM users WHERE username=?")) {
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                
                return Optional.of(new User(  // Mapping database row to User object
                    rs.getInt("id"),
                    rs.getString("username"),
                    rs.getString("password"),
                    rs.getString("preferredName"),
                    rs.getBoolean("isAdmin")
                ));
            }
        }
        return Optional.empty();
    }

    @Override
    public void add(User u) throws SQLException {
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "INSERT INTO users(username, password, preferredName, isAdmin) VALUES(?,?,?,?)")) {
         // Encryption of password before storing
            ps.setString(1, u.getUsername());
            ps.setString(2, Encryptor.shiftEncrypt(u.getEncryptedPassword()));
            ps.setString(3, u.getPreferredName());
            ps.setBoolean(4, u.isAdmin());
            ps.executeUpdate();
        }
    }

    
    @Override
    public void updatePassword(int userId, String newEncryptedPassword) throws SQLException {
        String sql = "UPDATE users SET password = ? WHERE id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newEncryptedPassword);
            ps.setInt(2, userId);
            ps.executeUpdate();
        }
    }

}