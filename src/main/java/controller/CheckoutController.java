package controller;

import dao.CartDAO;
import dao.EventDAO;
import dao.OrderDAO;
import dao.impl.CartDAOImpl;
import dao.impl.EventDAOImpl;
import dao.impl.OrderDAOImpl;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import model.CartItem;
import model.Event;
import model.Order;
import model.OrderItem;

import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Controller for the checkout screen.
 * Validates code and dates, creates an Order, updates sold counts and clears the cart
 */
public class CheckoutController {
    @FXML private Label totalLabel;
    @FXML private TextField codeField;
    @FXML private Button confirmButton;
    @FXML private Label messageLabel;

    private CartDAO  cartDAO  = new CartDAOImpl();
    private EventDAO eventDAO = new EventDAOImpl();
    private OrderDAO orderDAO = new OrderDAOImpl();
    private int      currentUserId;

    /**
     * Called by CartController to set the current user and display the total price.
     */
    public void setUser(int userId) {
        this.currentUserId = userId;
        try {
            List<CartItem> cart = cartDAO.findByUser(userId);
            double total = cart.stream()
                               .mapToDouble(ci -> ci.getEvent().getPrice() * ci.getQuantity())
                               .sum();
            totalLabel.setText(String.format("Total: AUD %.2f", total));
        } catch (SQLException e) {
            e.printStackTrace();
            messageLabel.setText("Error loading total.");
        }
    }

    /**
     * Called when the Confirm button is pressed.
     * Runs the code & date validations then processes the order.
     */
    @FXML
    public void onConfirm(ActionEvent event) {
        String code = codeField.getText().trim();
        if (!code.matches("\\d{6}")) {
            messageLabel.setText("Enter a valid 6-digit code.");
            return;
        }

        try {
            // Load the cart
            List<CartItem> cart = cartDAO.findByUser(currentUserId);
            if (cart.isEmpty()) {
                messageLabel.setText("Your cart is empty.");
                return;
            }

            // Date & availability checks
            DayOfWeek today = LocalDate.now().getDayOfWeek();
            for (CartItem ci : cart) {
                Event e = ci.getEvent();
                DayOfWeek eventDay = DayOfWeek.valueOf(e.getDay().toUpperCase());
                if (eventDay.getValue() < today.getValue()) {
                    messageLabel.setText("Cannot book past-day event: " + e.getTitle());
                    return;
                }
                int available = e.getCapacity() - e.getSold();
                if (ci.getQuantity() > available) {
                    messageLabel.setText("Not enough seats for: " + e.getTitle());
                    return;
                }
            }

            // Create Order and update sold counts
            Order order = new Order(currentUserId);
            order.setOrderDateTime(LocalDateTime.now());

            double total = 0;
            for (CartItem ci : cart) {
                Event e = ci.getEvent();
                // update sold in DB
                e.setSold(e.getSold() + ci.getQuantity());
                eventDAO.update(e);

                // add to order
                order.addItem(new OrderItem(order.getOrderNumber(), e, ci.getQuantity()));
                total += e.getPrice() * ci.getQuantity();
            }
            order.setTotalPrice(total);

            //  Persist order
            orderDAO.add(order);

            //  Clear the cart
            cartDAO.clearByUser(currentUserId);

            messageLabel.setText(
                "Checkout successful! Your order #" + order.getFormattedOrderNumber()
            );
        } catch (SQLException ex) {
            ex.printStackTrace();
            messageLabel.setText("Checkout failed. Please try again.");
        }
    }
}
