package client;

import constants.Command;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.util.ResourceBundle;

public class RegController  {


    @FXML
    TextField loginField;
    @FXML
    PasswordField passwordField;
    @FXML
    TextField nicknameField;
    @FXML
    TextArea textArea;

    private Controller controller;

    @FXML
    void tryToReg(ActionEvent actionEvent) {
        String login =loginField.getText().trim();
        String password=passwordField.getText().trim();
        String nickname=nicknameField.getText().trim();

        controller.registration(login,password,nickname);

    }

    public void setController(Controller controller) {
        this.controller = controller;
    }

    public void result(String command){
        if(command.equals(Command.REG_OK)){
            textArea.appendText("Регистрация прошла успешно\n");
        }else{
            textArea.appendText("Логин или никнэйм заняты\n");
        }
    }
}
