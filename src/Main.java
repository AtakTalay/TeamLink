import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.transform.Scale;
import javafx.stage.Screen;
import javafx.stage.Stage;
<<<<<<< Updated upstream
=======
import javafx.stage.StageStyle;

import java.awt.*;

public class Main extends Application {
//aasd
    @Override
    public void start(Stage primaryStage) throws Exception{
<<<<<<< Updated upstream
        Parent root = FXMLLoader.load(getClass().getResource("views/MainTemplate2.fxml"));
=======
<<<<<<< Updated upstream
        Parent root = FXMLLoader.load(getClass().getResource("views/MainTemplate.fxml"));
        primaryStage.setTitle("Hello World");
=======
        Parent root = FXMLLoader.load(getClass().getResource("views/DemoScene.fxml"));
>>>>>>> Stashed changes
        primaryStage.setTitle("TeamLink");
>>>>>>> Stashed changes
        primaryStage.setScene(new Scene(root));
        primaryStage.setMaximized(true);
        primaryStage.setMinHeight(700);
        primaryStage.setMinWidth(1100);
        primaryStage.show();
        Rectangle2D screenBounds = Screen.getPrimary().getBounds();
        System.out.println(screenBounds);
    }


    public static void main(String[] args) {
        launch(args);
    }
}
