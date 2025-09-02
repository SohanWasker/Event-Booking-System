package dao;

import model.Order;
	

import java.sql.SQLException;
import java.util.List;

/**
 * DAO interface for persisting and querying Orders
 */
public interface OrderDAO {
    //Persist a new Order (and its items) into the database
     void add(Order order) throws SQLException;

    //Retrieve all orders for a given user, newest first
    List<Order> findByUser(int userId) throws SQLException;
}