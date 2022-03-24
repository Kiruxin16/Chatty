package client;

import constants.Command;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;


public class NewNameController {
    @FXML
    public TextField nameField;
    @FXML
    public TextArea textArea;

    private Controller controller;


    public void setController(Controller controller) {
        this.controller = controller;
    }

    public void result(String command){
        if(command.equals(Command.CHANGE_NAME_FALIED)){
            textArea.appendText("Никнейм занят\n");
        }else {
            nameField.clear();
            textArea.clear();
            controller.hideNameStage();
        }
    }


    @FXML
    public void changeName(ActionEvent actionEvent) {
        String name = nameField.getText().trim();
        controller.changingName(name);


    }
}
