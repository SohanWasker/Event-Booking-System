package model;

/**
 * Represents an item in a user's shopping cart.
 */
public class CartItem {
    private int userId;   
    private Event event;  
    private int quantity; 

    public CartItem() {}

    public CartItem(int userId, Event event, int quantity) {
        this.userId = userId;
        this.event = event;
        this.quantity = quantity;
    }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public Event getEvent() { return event; }
    public void setEvent(Event event) { this.event = event; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
}