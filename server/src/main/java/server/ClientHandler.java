package server;

import constants.Command;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.concurrent.ExecutorService;

public class ClientHandler {
    private Server server;
    private   Socket socket;

    private DataInputStream in;
    private DataOutputStream out;
    private ExecutorService service;

    private boolean authenticated;
    private String nickname;
    private String login;


    public ClientHandler(Server server, Socket socket, ExecutorService service) {

        try{

            this.server = server;
            this.socket = socket;
            this.service=service;
            in = new DataInputStream(socket.getInputStream());
            out =new DataOutputStream(socket.getOutputStream());

            service.execute(() -> {
                try {
                    socket.setSoTimeout(12000);
                    //цикл аутентификации
                    while (true) {
                        String str = in.readUTF();

                        if(str.startsWith("/")) {
                            if (str.equals(Command.END)) {
                                sendMsg(Command.END);
                                break;
                            }

                            if (str.startsWith(Command.AUTH)){
                                String[] token =str.split(" ",3);
                                if (token.length<3){
                                    continue;
                                }
                                String newNick = server.getAuthService()
                                        .getNicknameByLoginAndPassword(token[1],token[2]);
                                login=token[1];
                                if (newNick != null) {
                                    if (!server.isLoginAuthenticated(login)) {
                                        nickname = newNick;
                                        sendMsg(Command.AUTH_OK+" "+ nickname);
                                        authenticated = true;
                                        server.subscribe(this);
                                        break;
                                    } else {
                                        sendMsg("Учетная запись уже используется.");

                                    }
                                } else {
                                    sendMsg("Логин / пароль не верны.");
                                }

                            }
                            if (str.startsWith(Command.REG)) {
                                String[] token = str.split(" ");
                                if (token.length < 4) {
                                    continue;
                                }
                                if(server.getAuthService().registration(token[1],token[2],token[3])){
                                    sendMsg(Command.REG_OK);
                                } else{
                                    sendMsg(Command.REG_FAILED);
                                }


                            }


                        }
                    }

                    //цикл работы
                    socket.setSoTimeout(0);
                    while (authenticated) {

                        String str = in.readUTF();
                        if (str.startsWith("/")) {
                            if (str.equals(Command.END)) {
                                sendMsg(Command.END);
                                break;
                            }
                            if (str.startsWith("/w")) {
                                String[] temp = str.split(" ", 3);
                                if (temp.length<3){
                                    continue;
                                }
                                server.whisperMsg(this,temp[1],temp[2]);

                            }
                            if(str.startsWith(Command.CHANGE_NAME)){
                                String[] temp = str.split(" ",2);
                                if (temp.length<2){
                                    continue;
                                }
                                if (server.userNameChange(nickname, temp[1])) {

                                    nickname = temp[1];
                                    server.broadcastClientList();
                                    sendMsg(Command.CHANGE_NAME_OK);
                                }else {
                                    sendMsg(Command.CHANGE_NAME_FALIED);
                                }

                            }
                        } else {
                            server.broadcastMsg(this, str);
                        }


                    }
                } catch (SocketTimeoutException e){
                    sendMsg(Command.END);

                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    server.unsubscribe(this);
                    System.out.println("Client disconnected");
                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });

        }catch (IOException e) {
            e.printStackTrace();
        }
    }



    public void sendMsg(String msg){
        try {
            out.writeUTF(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getNickname() {
        return nickname;
    }

    public String getLogin() {
        return login;
    }
}


