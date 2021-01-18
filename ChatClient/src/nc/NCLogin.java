package nc;

import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import nc.exc.PacketCorruptionException;
import nc.exc.ConnectionClosed;
import nc.message.*;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class NCLogin {
    @FXML private TextField fieldEmail;
    @FXML private TextField fieldPassword;
    @FXML private ProgressBar progressBar;

    @FXML private Button buttonLogIn;
    @FXML private Button buttonSignUp;
    @FXML private Label labelStatus;

    @FXML private URL location;
    @FXML private ResourceBundle resources;

    public Task<Void> updaterIsConnected = new Task<Void>() {
        @Override
        protected Void call() throws Exception {
            final NCClientService.State[] lastState = new NCClientService.State[1];
            Scene scene = labelStatus.getScene();

            while (isShown()) {
                Thread.sleep(200);

                Platform.runLater(() -> {
                    if (!isShown())
                        return;

                    NCClientService s = NCClientApp.client;

                    switch (s.getState()) {
                        case NOT_CONNECTED:
                            _startConnectUI();
                            break;
                        case BOUND:
                            _startConnectUI();
                            break;
                        case CONNECTED:
                            if (lastState[0] != NCClientService.State.CONNECTED)
                                _endConnectUI();
                            break;
                        case AUTHENTICATED:
                            break;
                    }
                    lastState[0] = s.getState();
                });
            }

            return null;
        }
    };

    private void _startConnectUI() {
        fieldEmail.setDisable(true);
        fieldPassword.setDisable(true);
        buttonLogIn.setDisable(true);
        buttonSignUp.setDisable(true);

        progressBar.setProgress(-1);
        labelStatus.setText("Connecting");
    }

    private void _endConnectUI() {
        fieldEmail.setDisable(false);
        fieldPassword.setDisable(false);
        buttonLogIn.setDisable(false);
        buttonSignUp.setDisable(false);

        progressBar.setProgress(0);
        labelStatus.setText("Connected");
    }

    private void _startLoginUI() {
        fieldEmail.setDisable(true);
        fieldPassword.setDisable(true);
        buttonLogIn.setDisable(true);
        buttonSignUp.setDisable(true);

        progressBar.setProgress(-1);
        labelStatus.setText("Authenticating");
    }

    private void _endLoginUI() {
        fieldEmail.setDisable(false);
        fieldPassword.setDisable(false);
        buttonLogIn.setDisable(false);
        buttonSignUp.setDisable(false);

        progressBar.setProgress(0);
        labelStatus.setText("Failed");
    }


    public void connect() {
        _startConnectUI();
        Thread t = new Thread(updaterIsConnected);
        t.setDaemon(true);
        t.start();
    }

    private Service<Boolean> authenticationChecker = new Service<Boolean>() {
        @Override
        protected Task<Boolean> createTask() {
            return new Task<Boolean>() {
                @Override
                protected Boolean call() throws Exception {
                    for (int i = 0; i < 8; ++i)
                        if (NCClientApp.client.isAuthenticated())
                            return true;
                        else {
                            Thread.sleep(500);
                        }
                    return false;
                }
            };
        }
    };

    public boolean isShown() {
        return labelStatus.getScene().getWindow().isShowing();
    }

    public void logIn() {
        _startLoginUI();
        String email = fieldEmail.getText();
        String password = fieldPassword.getText();

        try {
            NCClientApp.client.send(new Authenticate(NCClientApp.client.sessionID(), email, password));
        } catch (PacketCorruptionException e) {
            _endLoginUI();
            labelStatus.setText("Email or Password too long");
            return;
        } catch (ConnectionClosed e) {
            _endLoginUI();
            return;
        }

        authenticationChecker.setOnSucceeded(e -> {
            if ((Boolean) e.getSource().getValue()) {
                onAuthenticationSuccess();
            } else {
                _endLoginUI();
                labelStatus.setText("Email/Password wrong.");
            }
        });
        authenticationChecker.reset();
        authenticationChecker.start();
    }

    public void signUp() {
        _startLoginUI();
        String email = fieldEmail.getText();
        String password = fieldPassword.getText();

        try {
            NCClientApp.client.send(new Register(NCClientApp.client.sessionID(), email, password));
        } catch (PacketCorruptionException e) {
            _endLoginUI();
            labelStatus.setText("Email or Password too long.");
            return;
        } catch (ConnectionClosed e) {
            _endLoginUI();
            return;
        }

        authenticationChecker.setOnSucceeded(e -> {
            if ((Boolean) e.getSource().getValue()) {
                onAuthenticationSuccess();
            } else {
                _endLoginUI();
                labelStatus.setText("Email is taken.");
            }
        });
        authenticationChecker.reset();
        authenticationChecker.start();
    }


    private void onAuthenticationSuccess() {
        if (!isShown())
            return;
        labelStatus.setText("Success");

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/nc/ncwindow.fxml"));

        Parent root = null;
        try {
            root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("NChat");
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            NCWindow window = loader.getController();
            stage.setOnShown(e -> window.onShow());
            stage.show();


            labelStatus.getScene().getWindow().hide();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }
}
