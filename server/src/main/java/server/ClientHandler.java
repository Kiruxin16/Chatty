package server;

import constants.Command;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientHandler {
    private Server server;
    private   Socket socket;

    private DataInputStream in;
    private DataOutputStream out;

    private boolean authenticated;
    private String nickname;
    private String login;

    public ClientHandler(Server server, Socket socket) {

        try{

            this.server = server;
            this.socket = socket;
            in = new DataInputStream(socket.getInputStream());
            out =new DataOutputStream(socket.getOutputStream());


            new Thread(() -> {
                try {
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
                                        sendMsg(Command.AUTH_OK+" " + nickname);
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
                        } else {
                            server.broadcastMsg(this, str);
                        }


                    }
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
            }).start();

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


