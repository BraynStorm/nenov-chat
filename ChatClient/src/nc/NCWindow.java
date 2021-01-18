package nc;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyIntegerProperty;
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
import java.awt.event.MouseEvent;

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
                    // UPDATE friend list
                    List<NCFriend> friends = client.getFriendList();
                    String name="";
                    if(!friends.isEmpty()){
                        if(friendList.getSelectionModel().selectedIndexProperty().intValue() != -1 )
                            name = friendList.getSelectionModel().getSelectedItem().toString();

                        friendList.getItems().clear();
                        for(NCFriend friend : friends){
                            friendList.getItems().add(friend);
                            if(friend.name.equals(name)) {
                                //friendList.getSelectionModel().select(name);
                                friendList.setSelectionModel(friendList.getSelectionModel().select(name));
                                System.out.println("Selecting " + name);
                            }
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
        if (friendList.getSelectionModel().getSelectedItem() == null)
            chatLine.setVisible(false);
    }

    public void onSendAction() {
        if (!chatLine.isVisible()) return;
        friendList.getItems().add(new NCFriend(friendList.getItems().size()));
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
                NCFriend friend = (NCFriend) friendList.getSelectionModel().getSelectedItem();
                if (friend != null)
                    NCClientApp.client.send(new ClientSentDirectMessage(friend.id, chatLineText));
            } catch (Exception e) {
            }
        }

        chatLine.clear();
    }

    public void chatLineOnKeyReleased() {
    }


    public void handleMouseClick(javafx.scene.input.MouseEvent mouseEvent) {
        chatLine.setVisible(true);
    }
}
