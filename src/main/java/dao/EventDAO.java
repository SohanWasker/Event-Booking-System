package dao;

import model.Event;
import java.util.List;
import java.sql.SQLException;

/**
 * DAO interface for Event CRUD operations.
 */
public interface EventDAO {
    List<Event> findAll();
    Event findById(int id) throws SQLException;
    void add(Event e) throws SQLException;
    void update(Event e) throws SQLException;
    void delete(int id) throws SQLException;
    void setDisabled(int id, boolean disabled) throws SQLException;
}