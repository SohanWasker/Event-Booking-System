package model;

/**
 * Represents an event offering tickets for sale.
 */
public class Event {
    private int id;            
    private String title;      
    private String venue;      
    private String day;        
    private double price;     
    private int sold;         
    private int capacity;      
    private boolean disabled;  

    public Event() {}
     
    public Event(int id, String title, String venue, String day, double price, int sold, int capacity, boolean disabled) {
        this.id = id;
        this.title = title;
        this.venue = venue;
        this.day = day;
        this.price = price;
        this.sold = sold;
        this.capacity = capacity;
        this.disabled = disabled;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getVenue() { return venue; }
    public void setVenue(String venue) { this.venue = venue; }
    public String getDay() { return day; }
    public void setDay(String day) { this.day = day; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    public int getSold() { return sold; }
    public void setSold(int sold) { this.sold = sold; }
    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }
    public boolean isDisabled() { return disabled; }
    public void setDisabled(boolean disabled) { this.disabled = disabled; }
}