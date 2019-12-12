package client;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.util.ResourceBundle;

public class Controller implements Initializable {

    @FXML
    public TextArea textArea;
    @FXML
    public TextField textField;
    @FXML
    public HBox authPanel;
    @FXML
    public HBox msgPanel;
    @FXML
    public TextField loginField;
    @FXML
    public PasswordField passwordField;

    Socket socket;
    DataInputStream in;
    DataOutputStream out;

    final String IP_ADRESS = "localhost";
    final int PORT = 8186;

    private boolean isAuthenticated;
    private String nickname;

    public void setAuthenticated(boolean authenticated) {
        isAuthenticated = authenticated;
        authPanel.setVisible(!isAuthenticated);
        authPanel.setManaged(!isAuthenticated);
        msgPanel.setVisible(isAuthenticated);
        msgPanel.setManaged(isAuthenticated);
        if (!isAuthenticated) {
            nickname = "";
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setAuthenticated(false);
    }

    void setTitle(String title){
        Platform.runLater(()->{
            ((Stage)textField.getScene().getWindow()).setTitle(title);
        });
    }

    void connect() {
        try {
            socket = new Socket(IP_ADRESS, PORT);
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());

            new Thread(() -> {
                try {
                    //цикл авторизации
                    while (true) {
                        String str = in.readUTF();
                        if (str.startsWith("/authok")) {
                            setAuthenticated(true);
                            nickname= str.split(" ")[1];
                            break;
                        }
                        textArea.appendText(str + "\n");
                    }

                    setTitle("Chat : "+nickname);
                    // цикл работы
                    while (true) {
                        String str = in.readUTF();
                        if (str.equals("/end")) {
                            setAuthenticated(false);
                            break;
                        }
                        textArea.appendText(str + "\n");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    setTitle("Chat ");
                    textArea.clear();
                }
            }).start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMSG() {
        try {
            out.writeUTF(textField.getText());
            textField.clear();
            textField.requestFocus();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void tryToAuth(ActionEvent actionEvent) {
        if (socket == null || socket.isClosed()) {
            connect();
        }

        try {
            out.writeUTF("/auth " + loginField.getText() + " " + passwordField.getText());
            loginField.clear();
            passwordField.clear();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
