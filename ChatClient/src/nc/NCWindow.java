package nc;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
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
                Thread.sleep(250);

                Platform.runLater(() -> {
                    // UPDATE friend list
                    List<NCFriend> friends = client.getFriendList();
                    NCFriend selected = (NCFriend) friendList.getSelectionModel().getSelectedItem();

                    friendList.getItems().clear();
                    friendList.getItems().addAll(friends);
                    if (selected != null)
                        friendList.getSelectionModel().select(selected);
                });

                if (friendList.getSelectionModel() != null) {
                    NCFriend friend = (NCFriend) friendList.getSelectionModel().getSelectedItem();
                    if (friend != null) {
                        chatBox.clear();
                        for (NCChatMessage s : friend.messages) {
                            NCChatMessage.Direct msg = (NCChatMessage.Direct) s;
                            String senderName = NCClientApp.client.getName(msg.getSender());
                            String content = msg.getContent();

                            if (senderName != null && content != null)
                                chatBox.setText(chatBox.getText() + NCClientApp.client.getName(msg.getSender()) + " -> " + msg.getContent() + "\n");
                            else {
                                ;
                            }
                        }
                    }
                }
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
        String chatLineText = chatLine.getText();

        // check for command
        // extract

        if (chatLineText.startsWith("//") && chatLineText.contains(" ")) {
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
        }
        try {
            NCFriend friend = (NCFriend) friendList.getSelectionModel().getSelectedItem();
            if (friend != null)
                NCClientApp.client.send(new ClientSentDirectMessage(NCClientApp.client.clientID(), friend.id, chatLineText));
        } catch (Exception e) {
        }
        chatLine.clear();
    }

    public void chatLineOnKeyReleased() {
    }


    public void handleMouseClick(javafx.scene.input.MouseEvent mouseEvent) {
        chatLine.setVisible(true);
    }

    public void onKeyPressedEvent(KeyEvent keyEvent) {
        if (keyEvent.getCode().equals(KeyCode.ENTER))
            onSendAction();
    }
}
