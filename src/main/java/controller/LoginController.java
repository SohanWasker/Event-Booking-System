package controller;

import dao.UserDAO;
import dao.impl.UserDAOImpl;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import util.Encryptor;
import model.User;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Optional;

/**
 * Handles user login and sign up actions.
 */
public class LoginController {
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label messageLabel;

    private UserDAO userDAO = new UserDAOImpl();

    
     // Called when Login button is pressed.
     
    @FXML
    public void onLogin(ActionEvent event) {
    	try {
            String username = usernameField.getText();
            String password = passwordField.getText();

            // ADMIN LOGIN CHECK
            if (username.equals("admin") && password.equals("Admin321")) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/AdminDashboardView.fxml"));
                Parent root = loader.load();

                Stage stage = new Stage();
                stage.setTitle("Admin Dashboard");
                stage.setScene(new Scene(root));
                stage.show();

                // Close login window
                ((Stage)((Button)event.getSource()).getScene().getWindow()).close();
                return;
            }
        	
            Optional<User> result = userDAO.findByUsername(usernameField.getText());
            if (result.isEmpty()) {
                messageLabel.setText("User not found");
                return;
            }
            User u = result.get();

            // Compare encrypted password
            if (!Encryptor.shiftEncrypt(passwordField.getText()).equals(u.getEncryptedPassword())) {
                messageLabel.setText("Invalid credentials");
                return;
            }

            // Loads the user dashboard
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/view/UserDashboardView.fxml")
            );
            Parent root = loader.load();

            // Pass the loggedin user to the dashboard
            UserDashboardController dashCtrl = loader.getController();
            dashCtrl.setUser(u);

            Stage dashStage = new Stage();
            dashStage.setTitle("The Super Event — Dashboard");
            dashStage.initModality(Modality.NONE);
            dashStage.setScene(new Scene(root));
            dashStage.show();

            // Closes login window
            ((Stage)((Button)event.getSource()).getScene().getWindow()).close();

        } catch (SQLException | IOException e) {
            e.printStackTrace();
            messageLabel.setText("Login failed: " + e.getMessage());
        }
    }

    
     // Called when Sign Up button is pressed.
     
    @FXML
    public void onSignup(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/view/SignupView.fxml")
            );
            Parent root = loader.load();

            Stage signupStage = new Stage();
            signupStage.setTitle("Sign Up");
            signupStage.initModality(Modality.APPLICATION_MODAL);
            signupStage.setScene(new Scene(root));
            signupStage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
            messageLabel.setText("Failed to open signup dialog.");
        }
    }
}
