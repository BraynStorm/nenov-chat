package nc;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import nc.message.ClientAddFriend;
import nc.message.ClientRemoveFriend;
import nc.message.ClientSentDirectMessage;
import nc.message.NCMessage;

import java.util.List;

public class NCWindow {
    @FXML
    private Button send;
    @FXML
    private ListView friendList;
    @FXML
    private TextField chatLine;
    @FXML
    private TextArea chatBox;

    private Task<Void> timer = new Task<Void>() {
        @Override
        protected Void call() throws Exception {
            while (true) {
                NCClientService client = NCClientApp.client;
                Thread.sleep(500);

                Platform.runLater(() -> {
                    System.out.println("update");
                    // UPDATE friend list
                    List<NCFriend> friends = client.getFriendList();
                    if(!friends.isEmpty()){
                        System.out.println("Not empty");
                        friendList.getItems().clear();
                        for(NCFriend friend : friends){
                            friendList.getItems().add(friend);
                        }
                    }
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
        String chatLineText = chatLine.getText();

        // check for command
        // extract

        if (chatLineText.contains(" ")) {
            String cmd = chatLineText.substring(0, chatLineText.indexOf(" "));
            String frName = chatLineText.substring(chatLineText.lastIndexOf(" ") + 1);

            // add friend cmd
            if (cmd.toLowerCase().equals("//add")) {
                try {
                    NCClientApp.client.send(new ClientAddFriend(frName));
                } catch (Exception e) {
                }
            }
            // remove friend
            else if (cmd.toLowerCase().equals("//rmv")) {
                try {
                    NCClientApp.client.send(new ClientRemoveFriend(frName));
                } catch (Exception e) {
                }
            }
        } else {
            try {
                long id = -1;
                for (NCFriend friend : NCClientApp.client.getFriendList()) {
                    if (friend.name.equals(friendList.getSelectionModel().getSelectedItem())) {
                        id = friend.id;
                        break;
                    }
                }
                NCClientApp.client.send(new ClientSentDirectMessage(id, chatLineText));
            } catch (Exception e) {
            }
        }
    }

    public void chatLineOnKeyReleased() {

    }

}
