package model;

//Represents a single line item within an order.
 
public class OrderItem {
    private String orderNumber;  
    private Event event;         
    private int quantity;        

    public OrderItem() {}

    public OrderItem(String orderNumber, Event event, int quantity) {
        this.orderNumber = orderNumber;
        this.event = event;
        this.quantity = quantity;
    }

    public String getOrderNumber() { return orderNumber; }
    public void setOrderNumber(String orderNumber) { this.orderNumber = orderNumber; }
    public Event getEvent() { return event; }
    public void setEvent(Event event) { this.event = event; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
}