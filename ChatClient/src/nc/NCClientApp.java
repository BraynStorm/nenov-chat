package nc;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

public class NCClientApp extends Application {
    public static NCClientService client;

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/nc/nclogin.fxml"));

        client = new NCClientService();

        Parent root = loader.load();
        primaryStage.setTitle("NChat Login");
        primaryStage.setScene(new Scene(root));
        primaryStage.setResizable(false);

        NCLogin loginController = loader.getController();
        primaryStage.setOnShown(e -> loginController.connect());

        var tickThread = new Thread(() -> {
            while (true) {
                client.networkTick();
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ignored) {
                }
            }
        });
        tickThread.setDaemon(true);
        tickThread.start();

        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

