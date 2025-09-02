package controller;

import dao.CartDAO;
import dao.EventDAO;
import dao.OrderDAO;
import dao.impl.CartDAOImpl;
import dao.impl.EventDAOImpl;
import dao.impl.OrderDAOImpl;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.converter.IntegerStringConverter;
import model.CartItem;
import model.Event;
import model.Order;
import model.OrderItem;
import model.User;

import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Controller for managing the shopping cart.
 */
public class CartController {
    @FXML private Label cartWelcomeLabel;
    @FXML private TableView<CartItem> cartTable;
    @FXML private TableColumn<CartItem, String> eventCol;
    @FXML private TableColumn<CartItem, String> venueCol;
    @FXML private TableColumn<CartItem, String> dayCol;
    @FXML private TableColumn<CartItem, Integer> qtyCol;
    @FXML private TableColumn<CartItem, Void> actionCol;
    @FXML private Button checkoutButton;

    private CartDAO  cartDAO   = new CartDAOImpl();
    private EventDAO eventDAO  = new EventDAOImpl();
    private OrderDAO orderDAO  = new OrderDAOImpl();
    private User     currentUser;
    private ObservableList<CartItem> cartItems;

    // Set the current user and load cart items
    public void setUser(User user) {
        this.currentUser = user;
        cartWelcomeLabel.setText("Cart for " + user.getPreferredName());
        loadCartItems();
    }

    // Load cart items from the database to the table
    private void loadCartItems() {
        try {
            List<CartItem> list = cartDAO.findByUser(currentUser.getId());
            cartItems = FXCollections.observableArrayList(list);
            cartTable.setItems(cartItems);
        } catch (SQLException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Failed to load cart.").showAndWait();
        }
    }

    @FXML
    public void initialize() {
        cartTable.setEditable(true);

        // Columns bind to CartItem properties
        eventCol.setCellValueFactory(ci ->
            new SimpleStringProperty(ci.getValue().getEvent().getTitle()));
        venueCol.setCellValueFactory(ci ->
            new SimpleStringProperty(ci.getValue().getEvent().getVenue()));
        dayCol.setCellValueFactory(ci ->
            new SimpleStringProperty(ci.getValue().getEvent().getDay()));

        // Quantity editable cell
        qtyCol.setCellValueFactory(ci ->
            new SimpleIntegerProperty(ci.getValue().getQuantity()).asObject());
        qtyCol.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
        qtyCol.setOnEditCommit(ev -> {
            CartItem ci = ev.getRowValue();
            int newQty = ev.getNewValue();
            Event e = ci.getEvent();
            int available = e.getCapacity() - e.getSold();
            if (newQty < 1 || newQty > available) {
                new Alert(Alert.AlertType.WARNING,
                    "Invalid quantity. Only " + available + " seats available.")
                  .showAndWait();
                cartTable.refresh();
            } else {
                try {
                    ci.setQuantity(newQty);
                    cartDAO.addOrUpdate(ci);
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    new Alert(Alert.AlertType.ERROR, "Failed to update quantity.").showAndWait();
                }
            }
        });

        //Remove button
        actionCol.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button("Remove");
            {
                btn.setOnAction(e -> {
                    int idx = getIndex();
                    CartItem item = cartItems.get(idx);
                    try {
                        cartDAO.remove(currentUser.getId(), item.getEvent().getId());
                        cartItems.remove(idx);
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                        new Alert(Alert.AlertType.ERROR, "Failed to remove item.").showAndWait();
                    }
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn);
            }
        });
    }
    
    private DayOfWeek mapShortDay(String shortDay) {
        return switch (shortDay.toUpperCase()) {
            case "MON" -> DayOfWeek.MONDAY;
            case "TUE" -> DayOfWeek.TUESDAY;
            case "WED" -> DayOfWeek.WEDNESDAY;
            case "THU" -> DayOfWeek.THURSDAY;
            case "FRI" -> DayOfWeek.FRIDAY;
            case "SAT" -> DayOfWeek.SATURDAY;
            case "SUN" -> DayOfWeek.SUNDAY;
            default -> throw new IllegalArgumentException("Invalid day: " + shortDay);
        };
    }

    // Handle the checkout button: payment and cart clearing
    @FXML
    public void onCheckout() {
        try {
            List<CartItem> items = cartDAO.findByUser(currentUser.getId());
            if (items.isEmpty()) {
                new Alert(Alert.AlertType.WARNING, "Your cart is empty.").showAndWait();
                return;
            }

            double total = items.stream()
                                .mapToDouble(ci -> ci.getQuantity() * ci.getEvent().getPrice())
                                .sum();

            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Confirm Checkout");
            confirm.setHeaderText(String.format("Total: $%.2f", total));
            confirm.setContentText("Proceed to payment?");
            Optional<ButtonType> ok = confirm.showAndWait();
            if (ok.isEmpty() || ok.get() != ButtonType.OK) return;

            // Date validation
            DayOfWeek today = LocalDate.now().getDayOfWeek();
            for (CartItem ci : items) {
            	DayOfWeek ed = mapShortDay(ci.getEvent().getDay());
                if (ed.getValue() < today.getValue()) {
                    new Alert(Alert.AlertType.WARNING,
                        "Cannot book past event: " + ci.getEvent().getTitle())
                      .showAndWait();
                    return;
                }
            }

            TextInputDialog codeDialog = new TextInputDialog();   // Payment
            codeDialog.setTitle("Payment Confirmation");
            codeDialog.setHeaderText("Enter the 6-digit confirmation code:");
            Optional<String> codeOpt = codeDialog.showAndWait();
            if (codeOpt.isEmpty()) return;
            String code = codeOpt.get().trim();
            if (!code.matches("\\d{6}")) {
                new Alert(Alert.AlertType.ERROR,
                          "Invalid code. Must be exactly 6 digits.")
                    .showAndWait();
                return;
            }

            // Create order and update sold tickets
            Order order = new Order(currentUser.getId());
            for (CartItem ci : items) {
                Event e = ci.getEvent();
                e.setSold(e.getSold() + ci.getQuantity());
                eventDAO.update(e);
                order.addItem(new OrderItem(order.getOrderNumber(), e, ci.getQuantity()));
            }
            order.setTotalPrice(total);
            orderDAO.add(order);

            // Clears cart
            cartDAO.clearByUser(currentUser.getId());
            cartItems.clear();

            new Alert(Alert.AlertType.INFORMATION,
                      "Payment successful! Order #" + order.getFormattedOrderNumber())
                .showAndWait();

        } catch (SQLException ex) {
            ex.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Checkout failed.").showAndWait();
        }
    }
}
