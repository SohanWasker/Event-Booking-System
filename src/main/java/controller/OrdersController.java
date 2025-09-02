package controller;

import dao.OrderDAO;

import dao.impl.OrderDAOImpl;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import model.Order;
import model.OrderItem;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller for viewing and exporting a user's past orders.
 */
public class OrdersController {
    @FXML private TableView<Order> ordersTable;
    @FXML private TableColumn<Order, String> orderNumCol;
    @FXML private TableColumn<Order, String> dateCol;
    @FXML private TableColumn<Order, String> itemsCol;
    @FXML private TableColumn<Order, Double> totalCol;
    @FXML private Button exportButton;
    @FXML private Label messageLabel;

    private OrderDAO orderDAO = new OrderDAOImpl();
    private int currentUserId;

    
    //  Called by the dashboard to pass in the loggedin user ID.
     
    public void setUser(int userId) {
        this.currentUserId = userId;
        loadOrders();
    }

    @FXML
    public void initialize() {
        // Format order number 
        orderNumCol.setCellValueFactory(
            new PropertyValueFactory<>("formattedOrderNumber")
        );

        // Format date/time as a string
        dateCol.setCellValueFactory(o ->
            new SimpleStringProperty(
                o.getValue()
                 .getOrderDateTime()    
                 .toString()
            )
        );

        // Joins item descriptions like "Jazz Night x2; Mozart x1"
        itemsCol.setCellValueFactory(o ->
            new SimpleStringProperty(
                o.getValue().getItems().stream()
                 .map(i -> i.getEvent().getTitle() + " x" + i.getQuantity())
                 .collect(Collectors.joining("; "))
            )
        );

        // Show total price
        totalCol.setCellValueFactory(
            new PropertyValueFactory<>("totalPrice")
        );
    }

    // Load this user’s orders from the database into the table
    private void loadOrders() {
        try {
            List<Order> orders = orderDAO.findByUser(currentUserId);
            ordersTable.setItems(FXCollections.observableArrayList(orders));
        } catch (Exception e) {
            e.printStackTrace();
            messageLabel.setText("Failed to load orders.");
        }
    }
}
