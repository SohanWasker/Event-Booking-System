package controller;

import dao.UserDAO;
import dao.impl.UserDAOImpl;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import util.Encryptor;
import model.User;

/**
 * Controller for user registration.
 */
public class SignupController {
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private TextField preferredNameField;
    @FXML private Label messageLabel;

    private UserDAO userDAO = new UserDAOImpl();

    @FXML
    public void onRegister(ActionEvent event) {
        try {
            String user = usernameField.getText();
            String pass = passwordField.getText();
            String name = preferredNameField.getText();
            if(user.isBlank() || pass.isBlank() || name.isBlank()){
                messageLabel.setText("All fields required");
                return;
            }
            User u = new User(0, user, pass, name, false);
            userDAO.add(u);
            messageLabel.setText("Registration successful");
        } catch(Exception e) {
            messageLabel.setText("Error: " + e.getMessage());
        }
    }
}