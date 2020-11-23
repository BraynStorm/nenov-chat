package nc;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
    private NCConnection connection;

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/nc/nclogin.fxml"));

        Parent root = loader.load();
        primaryStage.setTitle("NC Login");
        primaryStage.setScene(new Scene(root));
        primaryStage.setResizable(false);

        NCLogin loginController = loader.getController();

        primaryStage.setOnShowing(e -> loginController.connect());

        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

