package controller;

import dao.CartDAO;
import dao.EventDAO;
import dao.impl.CartDAOImpl;
import dao.impl.EventDAOImpl;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.SpinnerValueFactory.IntegerSpinnerValueFactory;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.CartItem;
import model.Event;
import model.User;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/**
 * Controller for the user dashboard. Displays events and allows adding to cart.
 */
public class UserDashboardController {
    @FXML private Label welcomeLabel;
    @FXML private TableView<Event> eventTable;
    @FXML private TableColumn<Event, String> titleCol;
    @FXML private TableColumn<Event, String> venueCol;
    @FXML private TableColumn<Event, String> dayCol;
    @FXML private TableColumn<Event, Double> priceCol;
    @FXML private TableColumn<Event, Integer> soldCol;
    @FXML private TableColumn<Event, Integer> capacityCol;

    @FXML private Spinner<Integer> quantitySpinner;
    @FXML private Button addToCartButton;
    @FXML private Button viewCartButton;

    private EventDAO eventDAO = new EventDAOImpl();
    private CartDAO cartDAO   = new CartDAOImpl();
    private User currentUser;

    // Called by LoginController to set the current user
    public void setUser(User user) {
        this.currentUser = user;
        welcomeLabel.setText("Welcome, " + user.getPreferredName() + "!");
    }

    @FXML
    public void initialize() {
        // Table column setup
        titleCol.   setCellValueFactory(new PropertyValueFactory<>("title"));
        venueCol.   setCellValueFactory(new PropertyValueFactory<>("venue"));
        dayCol.     setCellValueFactory(new PropertyValueFactory<>("day"));
        priceCol.   setCellValueFactory(new PropertyValueFactory<>("price"));
        soldCol.    setCellValueFactory(new PropertyValueFactory<>("sold"));
        capacityCol.setCellValueFactory(new PropertyValueFactory<>("capacity"));

        // Load events
        List<Event> allEvents = eventDAO.findAll();
        List<Event> enabledEvents = allEvents.stream()
            .filter(e -> !e.isDisabled())
            .toList();
        eventTable.setItems(FXCollections.observableArrayList(enabledEvents));


        // Spinner allows any positive number
        quantitySpinner.setValueFactory(
            new IntegerSpinnerValueFactory(1, Integer.MAX_VALUE, 1)
        );
    }

    // Called when Add to Cart button is pressed
    @FXML
    public void onAddToCart() {
        Event sel = eventTable.getSelectionModel().getSelectedItem();
        if (sel == null) {
            new Alert(Alert.AlertType.WARNING, "Please select an event first.")
                .showAndWait();
            return;
        }

        int requested = quantitySpinner.getValue();
        try {
            //  displays how much is already in cart for this event
            int inCart = cartDAO.findByUser(currentUser.getId()).stream()
                .filter(ci -> ci.getEvent().getId() == sel.getId())
                .mapToInt(CartItem::getQuantity)
                .sum();

            // Available seats
            int available = sel.getCapacity() - sel.getSold() - inCart;
            if (requested > available) {
                new Alert(Alert.AlertType.WARNING,
                    String.format("Not enough seats! You asked %d but only %d available.",
                                  requested, available))
                  .showAndWait();
                return;
            }

            // Persist new total in cart
            CartItem ci = new CartItem(currentUser.getId(), sel, inCart + requested);
            cartDAO.addOrUpdate(ci);

            new Alert(Alert.AlertType.INFORMATION,
                requested + " tickets added to cart.").showAndWait();

        } catch (SQLException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Failed to update cart.").showAndWait();
        }
    }

    // Opens the cart window
    @FXML
    public void onViewCart() {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/view/CartView.fxml")
            );
            Parent root = loader.load();

            CartController cartCtrl = loader.getController();
            cartCtrl.setUser(currentUser);

            Stage cartStage = new Stage();
            cartStage.setTitle("Your Cart");
            cartStage.initModality(Modality.APPLICATION_MODAL);
            cartStage.setScene(new Scene(root));
            cartStage.showAndWait();
            refreshEventTable();

        } catch (IOException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Unable to open cart.").showAndWait();
        }
    }
    
    @FXML
    public void onViewOrders() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/OrderHistoryView.fxml"));
            Parent root = loader.load();

            OrderHistoryController ctrl = loader.getController();
            ctrl.setUser(currentUser);  

            Stage stage = new Stage();
            stage.setTitle("Order History");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Failed to open Order History.").showAndWait();
        }
    }

    @FXML
    public void onChangePassword() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/ChangePasswordView.fxml"));
            Parent root = loader.load();

            ChangePasswordController controller = loader.getController();
            controller.setUser(currentUser);

            Stage stage = new Stage();
            stage.setTitle("Change Password");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Failed to open Change Password window.").showAndWait();
        }
    }

    @FXML
    public void logout() {
        try {
            // Load the login view
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/LoginView.fxml"));
            Parent root = loader.load();

            Stage loginStage = new Stage();
            loginStage.setTitle("Login");
            loginStage.setScene(new Scene(root));
            loginStage.show();

            // Closes the current dashboard window
            Stage currentStage = (Stage) eventTable.getScene().getWindow();
            currentStage.close();

        } catch (IOException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Failed to log out.").showAndWait();
        }
    }

    private void refreshEventTable() {
    	List<Event> updatedEvents = eventDAO.findAll().stream()
    		    .filter(e -> !e.isDisabled())
    		    .toList();
    		eventTable.setItems(FXCollections.observableArrayList(updatedEvents));
    		eventTable.refresh();

    }

}
