package nc;

import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import nc.exc.PacketCorruptionException;
import nc.exc.ConnectionClosed;
import nc.message.*;
import nc.net.NCConnectionContainer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URL;
import java.util.Queue;
import java.util.ResourceBundle;

public class NCLogin {
    @FXML private TextField fieldEmail;
    @FXML private TextField fieldPassword;
    @FXML private ProgressBar progressBar;

    @FXML private Button buttonLogIn;
    @FXML private Label labelStatus;

    @FXML private URL location;
    @FXML private ResourceBundle resources;

    private NCConnection connection;

    private void _startConnectUI() {
        fieldEmail.setDisable(true);
        fieldPassword.setDisable(true);
        buttonLogIn.setDisable(true);

        progressBar.setProgress(-1);
        labelStatus.setText("Connecting");
    }

    private void _endConnectUI() {
        fieldEmail.setDisable(false);
        fieldPassword.setDisable(false);
        buttonLogIn.setDisable(false);

        progressBar.setProgress(0);
        labelStatus.setText("Connected");
    }

    private void _startLoginUI() {
        fieldEmail.setDisable(true);
        fieldPassword.setDisable(true);
        buttonLogIn.setDisable(true);

        progressBar.setProgress(-1);
        labelStatus.setText("Authenticating");
    }

    private void _endLoginUI() {
        fieldEmail.setDisable(false);
        fieldPassword.setDisable(false);
        buttonLogIn.setDisable(false);

        progressBar.setProgress(0);
        labelStatus.setText("Failed");
    }

    private Service<NCConnection> connectionService;

    public void connect() {
        _startConnectUI();

        connectionService = new Service<NCConnection>() {
            final InetSocketAddress address = new InetSocketAddress("213.91.183.197", 5511);

            @Override
            protected Task<NCConnection> createTask() {
                return new Task<NCConnection>() {
                    @Override
                    public NCConnection call() throws ConnectionClosed {
                        return NCConnectionContainer.connect(address);
                    }
                };
            }

        };

        connectionService.setOnSucceeded(e -> {
            connection = (NCConnection) e.getSource().getValue();
            _endConnectUI();
        });
        connectionService.setOnFailed(e -> {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Failed to connect to NChat server. Retry?", ButtonType.YES, ButtonType.NO);
            alert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.YES) {
                    connectionService.restart();
                } else {
                    Platform.exit();
                }
            });
        });

        connectionService.start();
    }

    public void logIn() {
        _startLoginUI();
        String email = fieldEmail.getText();
        String password = fieldPassword.getText();

        try {
            connection.sendPacket(new ClientAuthenticate(connection.getSessionID(), email, password));
        } catch (PacketCorruptionException e) {
            labelStatus.setText("Email or Password too long.");
            _endLoginUI();
            return;
        } catch (ConnectionClosed e) {
            _endLoginUI();
            labelStatus.setText("Disconnected.");
            connect();
            return;
        }

        Task<Void> authenticateTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                long start = System.currentTimeMillis();

                connection.processReadPackets();

                final Queue<NCMessage> readQueue = connection.getReadQueue();
                while (connection.clientID == 0) {
                    synchronized (readQueue) {
                        if (!readQueue.isEmpty()) {
                            NCMessage message = readQueue.peek();
                            System.out.println(message.type().name());
                            switch (message.type()) {
                                case PING:
                                    break;
                                case CONNECT_SUCCESSFUL:
                                    connection.sessionID = ((ConnectSuccessful) message).sessionID;
                                    System.out.println("Connection successful. SESSION_ID=" + connection.sessionID);
                                    break;
                                case CLIENT_JOIN_ROOM:
                                    break;
                                case CLIENT_SEND_DIRECT_MESSAGE:
                                    break;

                                case CLIENT_AUTHENTICATE:
                                    break;
                                case CLIENT_AUTHENTICATION_STATUS:
                                    connection.clientID = ((ClientAuthenticationStatus) message).clientID;
                                    System.out.println("CLIENT_ID=" + connection.sessionID);
                                    break;
                            }
                            readQueue.remove(); // pop

                        }
                        long now = System.currentTimeMillis();
                        if (now - start > 4000)
                            break;
                        Thread.sleep(250);
                    }
                }

                if (connection.clientID == 0)
                    throw new IOException();
                else
                    return null;
            }
        };

        authenticateTask.setOnSucceeded(e -> {
            labelStatus.setText("Success");
        });
        authenticateTask.setOnFailed(e -> _endLoginUI());
        new Thread(authenticateTask).start();
    }
}
