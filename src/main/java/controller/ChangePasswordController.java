package controller;

import dao.UserDAO;
import dao.impl.UserDAOImpl;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import model.User;
import util.Encryptor;

import java.sql.SQLException;

public class ChangePasswordController {
    @FXML private PasswordField oldPasswordField;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;

    private final UserDAO userDAO = new UserDAOImpl();
    private User currentUser;

    public void setUser(User user) {
        this.currentUser = user;
    }

    @FXML
    public void onSubmit() {
        String oldPass = oldPasswordField.getText();
        String newPass = newPasswordField.getText();
        String confirm = confirmPasswordField.getText();

        if (!Encryptor.shiftEncrypt(oldPass).equals(currentUser.getEncryptedPassword())) {
            showAlert("Incorrect current password.");
            return;
        }

        if (!newPass.equals(confirm)) {
            showAlert("New passwords do not match.");
            return;
        }

        try {
            currentUser.setEncryptedPassword(Encryptor.shiftEncrypt(newPass));
            userDAO.updatePassword(currentUser.getId(), currentUser.getEncryptedPassword());
            showAlert("Password updated successfully.");
            close();
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Failed to update password.");
        }
    }

    @FXML
    public void onCancel() {
        close();
    }

    private void showAlert(String msg) {
        new Alert(Alert.AlertType.INFORMATION, msg).showAndWait();
    }

    private void close() {
        Stage stage = (Stage) oldPasswordField.getScene().getWindow();
        stage.close();
    }
}
