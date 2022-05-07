package com.example.client_chat;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.net.URL;
import java.util.ResourceBundle;

public class HelloController {
    private boolean isAuthorized;

    @FXML
    TextArea textArea;
    @FXML
    TextField textField;
    @FXML
    Button button;

    @FXML
    TextField loginField;
    @FXML
    PasswordField passwordField;
    @FXML
    Button enter;

    @FXML
    HBox upperPanel;
    @FXML
    HBox bottomPanel;

    @FXML
    ListView clientList;

    @FXML
    Button createAccount;
    @FXML
    TextField loginCreate;
    @FXML
    PasswordField passwordCreate;
    @FXML
    TextField nickCreate;


    Socket socket;
    DataInputStream in;
    DataOutputStream out;

    String IP_ADDRESS = "localhost";
    int PORT = 8189;

    @FXML
    public void keyListener(KeyEvent keyEvent) {
        if (keyEvent.getCode().getCode() == 10) {
            sendMessage();
        }
    }

    public void setActive(Boolean isAuthorized) {
        this.isAuthorized = isAuthorized;

        if (!isAuthorized) {
            upperPanel.setVisible(true);
            upperPanel.setManaged(true);
            bottomPanel.setVisible(false);
            bottomPanel.setManaged(false);
        } else {
            upperPanel.setVisible(false);
            upperPanel.setManaged(false);
            bottomPanel.setVisible(true);
            bottomPanel.setManaged(true);
            clientList.setVisible(true);
            clientList.setManaged(true);
            createAccount.setVisible(false);
            createAccount.setManaged(false);
            loginCreate.setVisible(false);
            loginCreate.setManaged(false);
            passwordCreate.setVisible(false);
            passwordCreate.setManaged(false);
            nickCreate.setVisible(false);
            nickCreate.setManaged(false);
        }
    }

    @FXML
    public void sendMessage() {
        try {
            out.writeUTF(textField.getText());
            textField.clear();
            textField.requestFocus();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void connect() {

        try {
            socket = new Socket(IP_ADDRESS, PORT);
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        while (true) {
                            try {
                                String str = in.readUTF();
                                if (str.startsWith("/authOK")) {
                                    setActive(true);
                                    textArea.clear();
                                    textArea.appendText(str + "\n");
                                    break;
                                } else {
                                    textArea.appendText(str + "\n");
                                }
                            } catch (SocketException e) {
                                System.out.println("Server don't callback");
                                break;
                            }


                        }

                        while (true) {
                            try {
                                String str = in.readUTF();

                                if (str.startsWith("/")) {
                                    if (str.startsWith("/show")) {
                                        String[] nicknames = str.split(" ");

                                        Platform.runLater(new Runnable() {
                                            @Override
                                            public void run() {
                                                clientList.getItems().clear();
                                                for (int i = 1; i < nicknames.length; i++) {
                                                    clientList.getItems().add(nicknames[i]);
                                                }
                                            }
                                        });
                                    }
                                    if (str.equals("/end")) {
                                        break;
                                    }
                                } else {
                                    textArea.appendText(str + "\n");
                                }

                            } catch (SocketException e) {
                                System.out.println("Server don't callback");
                                break;
                            }

                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            socket.close();
                            in.close();
                            out.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                }

            }).start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void auth() {
        if (socket == null || socket.isClosed()) {
            connect();
        }
        try {
            if (loginField.getText().isBlank() || passwordField.getText().isBlank()) {
                textArea.appendText("Input login/Password\n");
                return;
            }

            out.writeUTF("/auth " + loginField.getText() + " " + passwordField.getText());
            loginField.clear();
            passwordField.clear();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @FXML
    public void registration() {

        try {
            out.writeUTF("/reg " + loginCreate.getText() + " " + passwordCreate.getText());
            loginCreate.clear();
            passwordCreate.clear();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}