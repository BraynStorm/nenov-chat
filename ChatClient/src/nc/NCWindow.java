package nc;

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

    public void onSendAction() {
        friendList.getItems().add(new NCFriend(friendList.getItems().size()));
    }

    public void chatLineOnKeyReleased() {

    }

}
