package controller;

import javafx.beans.property.SimpleIntegerProperty;



import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import model.Order;
import model.OrderItem;
import model.User;
import javafx.scene.control.Alert;

import java.sql.SQLException;
import java.util.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import dao.OrderDAO;
import dao.impl.OrderDAOImpl;
import java.io.PrintWriter;
import javafx.stage.FileChooser;


public class OrderHistoryController {
    @FXML private TableView<Order> orderTable;
    @FXML private TableColumn<Order, String> orderNumberCol;
    @FXML private TableColumn<Order, String> dateCol;
    @FXML private TableColumn<Order, Double> totalCol;

    @FXML private TableView<OrderItem> itemTable;
    @FXML private TableColumn<OrderItem, String> eventCol;
    @FXML private TableColumn<OrderItem, Integer> quantityCol;

    private OrderDAO orderDAO = new OrderDAOImpl();
    private User currentUser;

    public void setUser(User user) {
    	this.currentUser = user;
        if (user != null) {
            loadOrders();
        }
    }

    private void loadOrders() {
        try {
            List<Order> orders = orderDAO.findByUser(currentUser.getId());
            orderTable.setItems(FXCollections.observableArrayList(orders));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void initialize() {
        orderNumberCol.setCellValueFactory(o ->
            new SimpleStringProperty(o.getValue().getFormattedOrderNumber()));
        dateCol.setCellValueFactory(o ->
            new SimpleStringProperty(o.getValue().getOrderDateTime().toString()));
        totalCol.setCellValueFactory(new PropertyValueFactory<>("totalPrice"));

        eventCol.setCellValueFactory(oi ->
            new SimpleStringProperty(oi.getValue().getEvent().getTitle()));
        quantityCol.setCellValueFactory(oi ->
            new SimpleIntegerProperty(oi.getValue().getQuantity()).asObject());

        orderTable.getSelectionModel().selectedItemProperty().addListener((obs, oldOrder, newOrder) -> {
            if (newOrder != null) {
                itemTable.setItems(FXCollections.observableArrayList(newOrder.getItems()));
            }
        });
    }
    
    public void loadAllOrders() {
        try {
            List<Order> allOrders = OrderDAOImpl.findAllOrders(); // new method above
            orderTable.setItems(FXCollections.observableArrayList(allOrders));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    
    @FXML
    public void onClose() {
        // Get the current window and close it
        Stage stage = (Stage) orderTable.getScene().getWindow();
        stage.close();
    }
    @FXML
    public void onExport() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Order History");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text Files", "*.txt"));
        fileChooser.setInitialFileName("orders.txt");

        File file = fileChooser.showSaveDialog(orderTable.getScene().getWindow());

        if (file != null) {
            try (PrintWriter writer = new PrintWriter(file)) {
                for (Order order : orderTable.getItems()) {
                    writer.println("Order #" + order.getFormattedOrderNumber());
                    writer.println("Date: " + order.getOrderDateTime());
                    writer.println("Items:");
                    for (OrderItem item : order.getItems()) {
                        writer.printf("  - %s x%d\n", item.getEvent().getTitle(), item.getQuantity());
                    }
                    writer.printf("Total: $%.2f\n", order.getTotalPrice());
                    writer.println("-------------------------------------");
                }
                new Alert(Alert.AlertType.INFORMATION, "Export successful!").showAndWait();
            } catch (IOException e) {
                e.printStackTrace();
                new Alert(Alert.AlertType.ERROR, "Failed to export orders.").showAndWait();
            }
        }
    }

	
}
