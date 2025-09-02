package model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a user’s order containing one or more order items
 */
public class Order {
    private String orderNumber;                
    private final int userId;                
    private LocalDateTime orderDateTime;     
    private double totalPrice;               
    private final List<OrderItem> items;     

    /**
     * Creates a new Order for the given user
     * orderDateTime is auto‐set to now
     */
    private static int counter = 1;
    public Order(int userId) {
        this.userId = userId;
        this.orderDateTime = LocalDateTime.now();
        this.orderNumber = String.format("%04d", counter++);
        this.items = new ArrayList<>();
    }

    // Used by DAO when reading from the database to set the generated orderNumber
    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public int getUserId() {
        return userId;
    }

    public LocalDateTime getOrderDateTime() {
        return orderDateTime;
    }

    /** 
     * Used by the DAO when loading an existing order 
     * so it reflects the correct time stamp from the database
     */
    public void setOrderDateTime(LocalDateTime orderDateTime) {
        this.orderDateTime = orderDateTime;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public void addItem(OrderItem item) {
        this.items.add(item);
    }

    public String getFormattedOrderNumber() {
    	try {
            return String.format("%04d", Integer.parseInt(orderNumber));
        } catch (NumberFormatException e) {
            return orderNumber; 
        }
    }
}
