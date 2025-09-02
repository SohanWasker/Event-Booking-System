package controller;

import dao.EventDAO;
import dao.impl.EventDAOImpl;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.Event;

import java.io.IOException;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Controller for admin event management.
 */
public class AdminDashboardController {
    @FXML private TableView<EventDisplay> eventTable;
    @FXML private TableColumn<EventDisplay, String> titleCol;
    @FXML private TableColumn<EventDisplay, String> scheduleCol;
    @FXML private TableColumn<EventDisplay, Boolean> disabledCol;
    @FXML private Button disableButton;
    @FXML private Button enableButton;
    @FXML private Button addButton;
    @FXML private Button deleteButton;
    @FXML private Button modifyButton;

    private EventDAO eventDAO = new EventDAOImpl();
    private ObservableList<EventDisplay> groupedEvents;

    public static class EventDisplay {
        private final String title;
        private final String schedule;
        private final boolean disabled;
        private final List<Integer> eventIds; // store all ids for enable/disable

        public EventDisplay(String title, String schedule, boolean disabled, List<Integer> eventIds) {
            this.title = title;
            this.schedule = schedule;
            this.disabled = disabled;
            this.eventIds = eventIds;
        }

        public String getTitle() { return title; }
        public String getSchedule() { return schedule; }
        public boolean isDisabled() { return disabled; }
        public List<Integer> getEventIds() { return eventIds; }  // All the event IDs under the same title
    }

    @FXML
    public void initialize() {
        refreshTable();                   // Setting what each table column should display
        titleCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTitle()));
        scheduleCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getSchedule()));
        disabledCol.setCellValueFactory(data -> new SimpleBooleanProperty(data.getValue().isDisabled()).asObject());
    }

    private void refreshTable() {
        List<Event> allEvents = eventDAO.findAll();
        Map<String, List<Event>> grouped = allEvents.stream().collect(Collectors.groupingBy(Event::getTitle));

        groupedEvents = FXCollections.observableArrayList();

        for (String title : grouped.keySet()) {
            List<Event> events = grouped.get(title);
            String schedule = events.stream()
                .map(e -> e.getVenue() + " – " + e.getDay())
                .collect(Collectors.joining(", "));
            boolean disabled = events.stream().allMatch(Event::isDisabled);
            List<Integer> ids = events.stream().map(Event::getId).toList();
            groupedEvents.add(new EventDisplay(title, schedule, disabled, ids));
        }

        eventTable.setItems(groupedEvents);
    }

    @FXML
    public void onDisable(ActionEvent event) throws SQLException {       // Disables events
        EventDisplay selected = eventTable.getSelectionModel().getSelectedItem();
        for (int id : selected.getEventIds()) {
            eventDAO.setDisabled(id, true);
        }
        refreshTable();
    }

    @FXML
    public void onEnable(ActionEvent event) throws SQLException {           // Enables events
        EventDisplay selected = eventTable.getSelectionModel().getSelectedItem();
        for (int id : selected.getEventIds()) {
            eventDAO.setDisabled(id, false);
        }
        refreshTable();
    }

    @FXML
    public void onAdd(ActionEvent event) {         // adds new event
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Add Event");
        dialog.setHeaderText("Enter event details as: Title,Venue,Day,Price,Capacity");
        dialog.setContentText("Details:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(input -> {
            try {
                String[] parts = input.split(",");
                if (parts.length != 5) throw new IllegalArgumentException();

                String title = parts[0].trim();
                String venue = parts[1].trim();
                String day = parts[2].trim();
                double price = Double.parseDouble(parts[3].trim());
                int capacity = Integer.parseInt(parts[4].trim());

                boolean duplicate = eventDAO.findAll().stream().anyMatch(e ->
                    e.getTitle().equalsIgnoreCase(title) &&
                    e.getVenue().equalsIgnoreCase(venue) &&
                    e.getDay().equalsIgnoreCase(day)
                );
                if (duplicate) {
                    new Alert(AlertType.WARNING, "Duplicate event exists.").showAndWait();
                    return;
                }

                eventDAO.add(new Event(0, title, venue, day, price, 0, capacity, false));
                refreshTable();
            } catch (Exception e) {
                new Alert(AlertType.ERROR, "Invalid input format.").showAndWait();
            }
        });
    }

    @FXML
    public void onDelete(ActionEvent event) throws SQLException {       // Deletes events
        EventDisplay selected = eventTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        Alert confirm = new Alert(AlertType.CONFIRMATION, "Are you sure you want to delete all schedules for this event?");
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                try {
                    for (int id : selected.getEventIds()) {
                        eventDAO.delete(id);
                    }
                    refreshTable();
                } catch (SQLException e) {
                    e.printStackTrace();
                    new Alert(AlertType.ERROR, "Deletion failed.").showAndWait();
                }
            }
        });
    }

    @FXML
    public void onModify(ActionEvent event) {   // modifies the venue, day, date and price
        EventDisplay selected = eventTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Modify Event");
        dialog.setHeaderText("Enter new details as: Venue,Day,Price,Capacity\n(This applies to all schedules under this title)");
        dialog.setContentText("Details:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(input -> {
            try {
                String[] parts = input.split(",");
                if (parts.length != 4) throw new IllegalArgumentException();

                String newVenue = parts[0].trim();
                String newDay = parts[1].trim();
                double newPrice = Double.parseDouble(parts[2].trim());
                int newCapacity = Integer.parseInt(parts[3].trim());

                for (int id : selected.getEventIds()) {    // Updates each event individually
                    Event e = eventDAO.findById(id);
                    if (e == null) continue;

                    boolean duplicate = eventDAO.findAll().stream().anyMatch(ev ->
                        ev.getTitle().equalsIgnoreCase(e.getTitle()) &&
                        ev.getVenue().equalsIgnoreCase(newVenue) &&
                        ev.getDay().equalsIgnoreCase(newDay) &&
                        ev.getId() != id
                    );
                    if (duplicate) {
                        new Alert(AlertType.WARNING, "Duplicate with another event exists.").showAndWait();
                        return;
                    }

                    e.setVenue(newVenue);   // applies updates
                    e.setDay(newDay);
                    e.setPrice(newPrice);
                    e.setCapacity(newCapacity);
                    eventDAO.update(e);
                }
                refreshTable();
            } catch (Exception e) {
                e.printStackTrace();
                new Alert(AlertType.ERROR, "Invalid format or failed to update.").showAndWait();
            }
        });
    }
    
    @FXML
    public void logout() {
        try {
            // Load the login view after logout
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/LoginView.fxml"));
            Parent root = loader.load();

            Stage loginStage = new Stage();
            loginStage.setTitle("Login");
            loginStage.setScene(new Scene(root));
            loginStage.show();

            // Close the current dashboard window
            Stage currentStage = (Stage) eventTable.getScene().getWindow();
            currentStage.close();

        } catch (IOException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Failed to log out.").showAndWait();
        }
    }

    
    @FXML
    public void onViewAllOrders() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/OrderHistoryView.fxml"));
            Parent root = loader.load();

            OrderHistoryController ctrl = loader.getController();
            ctrl.loadAllOrders(); 

            Stage stage = new Stage();
            stage.setTitle("All Orders");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Failed to open All Orders view.").showAndWait();
        }
    }


}
