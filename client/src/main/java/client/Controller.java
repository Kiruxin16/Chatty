package client;

import constants.Command;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ResourceBundle;

public class Controller implements Initializable {

    @FXML
    public TextField textField;
    @FXML
    public TextArea textArea;
    @FXML
    public TextField loginField;
    @FXML
    public PasswordField passwordField;
    @FXML
    public HBox authPanel;
    @FXML
    public HBox textPanel;
    @FXML
    public ListView<String> clientList;
    @FXML
    public VBox leftPanel;


    private Socket socket;
    private static final int PORT =8189;
    private static final String ADDRESS = "localhost";

    private DataInputStream in;
    private DataOutputStream out;

    private boolean isWindowClosed;

    private boolean authenticated;
    private String nickname;
    private Stage stage;
    private Stage regStage;
    private Stage nameStage;
    private RegController regController;
    private NewNameController newNameController;

    public void setAuthenticated(boolean authenticated) {
        this.authenticated = authenticated;
        authPanel.setVisible(!authenticated);
        authPanel.setManaged(!authenticated);
        textPanel.setVisible(authenticated);
        textPanel.setManaged(authenticated);
        leftPanel.setVisible(authenticated);
        leftPanel.setManaged(authenticated);

        if(!authenticated){
            nickname = "";
        }

        textArea.clear();
        setTitle(nickname);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        Platform.runLater(()->{
            stage=(Stage) textField.getScene().getWindow();
            stage.setOnCloseRequest(event -> {
                System.out.println("bye");
                if(socket !=null&& !socket.isClosed()){
                    try {
                        out.writeUTF(Command.END);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        });
        setAuthenticated(false);

    }

    private void connect(){


        try{
            socket=new Socket(ADDRESS,PORT);
            in = new DataInputStream(socket.getInputStream());
            out =new DataOutputStream(socket.getOutputStream());


            new Thread(() -> {

                try {
                    //цикл аутентификации
                    while (true) {
                        String str = in.readUTF();

                        if (str.startsWith("/")) {
                            if (str.equals(Command.END)) {
                                break;
                            }

                            if (str.startsWith(Command.AUTH_OK)) {
                                nickname = str.split(" ")[1];
                                setAuthenticated(true);
                                break;

                            }
                            if(str.equals(Command.REG_OK)||str.equals(Command.REG_FAILED)){
                                regController.result(str);

                            }
                        } else {
                            textArea.appendText(str + "\n");

                        }
                    }

                    //цикл работы
                    while (authenticated) {
                        String str = in.readUTF();
                        if (str.startsWith("/")) {

                            if (str.equals(Command.END)) {
                                break;
                            }
                            if (str.startsWith(Command.CLIENT_LIST)) {
                                String[] token =str.split(" ");
                                Platform.runLater(()->{
                                    clientList.getItems().clear();
                                    for (int i = 1; i <token.length ; i++) {
                                        clientList.getItems().add(token[i]);
                                    }

                                });

                            }
                            if(str.equals(Command.CHANGE_NAME_FALIED) || str.equals(Command.CHANGE_NAME_OK)){
                                newNameController.result(str);
                            }
                        }else {
                            textArea.appendText(str + "\n");
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    setAuthenticated(false);
                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            }).start();

        } catch (Exception e) {
            e.printStackTrace();

        }


    }


    @FXML
    public void sendMsg(ActionEvent actionEvent) {

        try {
            out.writeUTF(textField.getText());
            textField.clear();
            textField.requestFocus();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    @FXML
    public void tryToAuth(ActionEvent actionEvent) {
        if (socket== null|| socket.isClosed()){
            connect();
        }

        String msg = String.format("%s %s %s",Command.AUTH, loginField.getText().trim(),
                passwordField.getText().trim());
        passwordField.clear();

        try {
            out.writeUTF(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    private void setTitle(String nickname){
        String title;
        if(nickname.equals("")){
            title = "Chatty";
        } else{
            title=String.format("Chatty:: %s",nickname);
        }
        Platform.runLater(()->{
            stage.setTitle(title);
        });
    }
    @FXML
    public void clientlListMouseAction(MouseEvent mouseEvent) {
        String receiver=(clientList.getSelectionModel().getSelectedItem());
        textField.setText(String.format("/w %s ",receiver));
    }


    private void createRegStage(){

        try{
            FXMLLoader  fxmlLoader = new FXMLLoader(getClass().getResource("/reg.fxml"));
            Parent root =fxmlLoader.load();

            regStage=new Stage();
            regStage.setTitle("Chatty registration");
            regStage.setScene(new Scene(root, 600, 500));

            regController=fxmlLoader.getController();
            regController.setController(this);


            regStage.initStyle(StageStyle.UTILITY);
            regStage.initModality(Modality.APPLICATION_MODAL);

        }catch (IOException e){
            e.printStackTrace();

        }

    }



    private void createNameStage(){

        try{
            FXMLLoader  fxmlLoader = new FXMLLoader(getClass().getResource("/newName.fxml"));
            Parent root =fxmlLoader.load();

            nameStage=new Stage();
            nameStage.setTitle("New name");
            nameStage.setScene(new Scene(root, 400, 200));

            newNameController=fxmlLoader.getController();
            newNameController.setController(this);


            nameStage.initStyle(StageStyle.UTILITY);
            nameStage.initModality(Modality.APPLICATION_MODAL);

        }catch (IOException e){
            e.printStackTrace();

        }

    }

    public void closeNameWindow(){
        Platform.runLater(() -> nameStage.close());

    }


    public void tryToReg(ActionEvent actionEvent) {
        if(regStage==null){
            createRegStage();
        }
        regStage.show();
    }

    public void registration(String login, String password,String nickname){
        String msg = String.format("%s %s %s %s",Command.REG, login,password,nickname);

        if (socket== null|| socket.isClosed()){
            connect();
        }
        try {
            out.writeUTF(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void changingName(String nickname){
        String msg = String.format("%s %s",Command.CHANGE_NAME,nickname);

        if (socket== null|| socket.isClosed()){
            connect();
        }
        try {
            out.writeUTF(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void setNewName(ActionEvent actionEvent) {
        if(nameStage==null){
            createNameStage();
        }
        nameStage.show();
    }
}
