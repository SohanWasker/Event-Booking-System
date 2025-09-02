package dao;

import model.User;
import java.util.Optional;
import java.sql.SQLException;

/**
 * Interface for user-related persistence.
 */
public interface UserDAO {
    Optional<User> findByUsername(String username) throws SQLException;
    void add(User u) throws SQLException;
    void updatePassword(int userId, String encrypted) throws SQLException;
}