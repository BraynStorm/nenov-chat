package nc;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class NCWindow {
    @FXML private Button send;
    @FXML private ListView friendList;
    @FXML private TextField chatLine;
    @FXML private TextArea chatBox;

    private Task<Void> timer = new Task<Void>() {
        @Override
        protected Void call() throws Exception {
            while (true) {
                NCClientService client = NCClientApp.client;
                Thread.sleep(500);

                Platform.runLater(() -> {
                    // UPDATE friend list
                });
            }
        }
    };

    public void onShow() {
        Thread t = new Thread(timer);
        t.setDaemon(true);
        t.start();
    }

    public void onSendAction() {
        friendList.getItems().add(new NCFriend(friendList.getItems().size()));
    }

    public void chatLineOnKeyReleased() {

    }

}
