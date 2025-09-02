package app;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import util.Database;

public class Main extends Application{
    @Override
    public void start(Stage primaryStage) throws Exception {
        // Setting up SQLite tables 
        Database.init();   
        System.out.print("FXML : "+getClass().getResource("/view/LoginView.fxml"));
        Parent root = FXMLLoader.load(getClass().getResource("/view/LoginView.fxml"));
        primaryStage.setTitle("The Super Event Booking");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }

    public static void main(String[] 
    		args) {	
        launch(args);
    }
}