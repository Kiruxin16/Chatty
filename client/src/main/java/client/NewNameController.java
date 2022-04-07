package client;

import constants.Command;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;


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
            textArea.appendText("Никнейм успешно изменен\n");
            controller.closeNameWindow();

        }
    }


    @FXML
    public void changeName(ActionEvent actionEvent) {
        String name = nameField.getText().trim();
        controller.changingName(name);


    }
}
