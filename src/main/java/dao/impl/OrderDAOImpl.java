package dao.impl;

import dao.OrderDAO;
//import dao.impl.EventDAOImpl;
import model.Event;
import model.Order;
import model.OrderItem;
import util.Database;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class OrderDAOImpl implements OrderDAO {
    public OrderDAOImpl() {}
    
    private String getNextOrderNumber(Connection conn) throws SQLException {
        String sql = "SELECT MAX(CAST(orderNumber AS INTEGER)) FROM orders";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            int next = 1;
            if (rs.next()) {
                next = rs.getInt(1) + 1;
            }
            return String.format("%04d", next); 
        }
    }

    @Override
    public void add(Order order) throws SQLException {
        try (Connection conn = Database.getConnection()) {
            //  Generate and assign a unique 4 digit order number
            String nextOrderNumber = getNextOrderNumber(conn);
            order.setOrderNumber(nextOrderNumber);

            //  Insert into orders table
            String insertOrder = "INSERT INTO orders(orderNumber, userId, dateTime, totalPrice) VALUES(?,?,?,?)";
            try (PreparedStatement ps = conn.prepareStatement(insertOrder)) {
                ps.setString(1, order.getOrderNumber());
                ps.setInt(2, order.getUserId());
                ps.setString(3, order.getOrderDateTime().toString());
                ps.setDouble(4, order.getTotalPrice());
                ps.executeUpdate();
            }

            //  Insert into orderItems table
            String insertItem = "INSERT INTO orderItems(orderNumber, eventId, quantity) VALUES(?,?,?)";
            try (PreparedStatement ps = conn.prepareStatement(insertItem)) {
                for (OrderItem item : order.getItems()) {
                    ps.setString(1, order.getOrderNumber());
                    ps.setInt(2, item.getEvent().getId());
                    ps.setInt(3, item.getQuantity());
                    ps.addBatch();
                }
                ps.executeBatch();
            }
        }
    }

    @Override
    public List<Order> findByUser(int userId) throws SQLException {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT orderNumber, dateTime, totalPrice FROM orders " +
                     "WHERE userId = ? ORDER BY dateTime DESC";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            EventDAOImpl eventDAO = new EventDAOImpl();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Order o = new Order(userId);
                    o.setOrderNumber(rs.getString("orderNumber"));
                    o.setOrderDateTime(LocalDateTime.parse(rs.getString("dateTime")));
                    o.setTotalPrice(rs.getDouble("totalPrice"));
                 
                    PreparedStatement ps2 = conn.prepareStatement(
                    	    "SELECT eventId, quantity FROM orderItems WHERE orderNumber = ?"
                    	);
                    	ps2.setString(1, o.getOrderNumber());
                    	ResultSet itemsRs = ps2.executeQuery();
                    	
                    	while (itemsRs.next()) {
                    	    int eventId = itemsRs.getInt("eventId");
                    	    int quantity = itemsRs.getInt("quantity");
                    	    Event event = eventDAO.findById(eventId); 
                    	    o.addItem(new OrderItem(o.getOrderNumber(), event, quantity));
                    	}
                    orders.add(o);
                    System.out.println("Fetched order number: " + rs.getString("orderNumber"));

                }
            }
            

        }
        return orders;
    }
    
    public static List<Order> findAllOrders() throws SQLException {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT * FROM orders ORDER BY dateTime DESC";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            EventDAOImpl eventDAO = new EventDAOImpl();

            while (rs.next()) {
                int userId = rs.getInt("userId");
                Order o = new Order(userId);
                o.setOrderNumber(rs.getString("orderNumber"));
                o.setOrderDateTime(LocalDateTime.parse(rs.getString("dateTime")));
                o.setTotalPrice(rs.getDouble("totalPrice"));

                // Load associated order items
                try (PreparedStatement ps2 = conn.prepareStatement(
                        "SELECT eventId, quantity FROM orderItems WHERE orderNumber = ?")) {
                    ps2.setString(1, o.getOrderNumber());
                    ResultSet itemsRs = ps2.executeQuery();

                    while (itemsRs.next()) {
                        int eventId = itemsRs.getInt("eventId");
                        int quantity = itemsRs.getInt("quantity");
                        Event event = eventDAO.findById(eventId); 
                        if (event != null) {
                            o.addItem(new OrderItem(o.getOrderNumber(), event, quantity));
                        }
                    }
                }

                orders.add(o);
            }
        }
        return orders;
    }



}
