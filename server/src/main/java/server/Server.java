package server;

import constants.Command;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class Server {
    private static final Logger logger = Logger.getLogger(server.Server.class.getName());
    public static ServerSocket server;
    public static Socket socket;
    public static final int PORT = 8189;
    public static ExecutorService service=Executors.newCachedThreadPool();

    private List<ClientHandler> clients;
    private AuthService authService;

    private static LogManager logManager;
    static {
        logManager = LogManager.getLogManager();
    }



    public Server() {

        try {
            logManager.readConfiguration(new FileInputStream("logging.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        clients = new CopyOnWriteArrayList<>();
        authService = new DBAuthService();

        try {
            Server.server = new ServerSocket(PORT);
            logger.log(Level.INFO,"server started");


            while (true) {
                socket = server.accept();
                logger.log(Level.INFO,"Client connected");
                new ClientHandler(this,socket,service);
           }




        } catch (IOException e) {
            logger.log(Level.SEVERE,"Ошибка сервера",e);
        }finally {
            for(Handler h:logger.getHandlers()){
                h.close();
            }
            service.shutdown();
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                server.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void broadcastMsg(ClientHandler sender, String msg){
        String message = String.format("[%s]: %s",sender.getNickname(),msg);

        for (ClientHandler c: clients) {
            c.sendMsg(message);

        }
    }

    public void whisperMsg(ClientHandler sender, String recipient, String msg) {
        boolean isOnline=false;
        if (authService.isNameExist(recipient)) {
            for (ClientHandler c : clients) {
                if (c.getNickname().equals(recipient)) {
                    String message = String.format("[%s] to: %s %s", sender.getNickname(), recipient, msg);
                    sender.sendMsg(message);
                    message = String.format("from %s: %s", sender.getNickname(), msg);
                    c.sendMsg(message);
                    isOnline=true;
                }

            }
            if(!isOnline){
                sender.sendMsg("Пользователь не в сети");
            }
        } else sender.sendMsg("Пользователь не найден");
    }

    public void broadcastClientList(){
        StringBuilder sb = new StringBuilder(Command.CLIENT_LIST);

        for(ClientHandler c: clients){
            sb.append(" ").append(c.getNickname());
        }
        String msg =sb.toString();

        for(ClientHandler c: clients) {
            c.sendMsg(msg);
        }




    }


    public boolean userNameChange(String oldName, String newName) {

        return authService.changeName(oldName, newName);

    }

    public boolean isLoginAuthenticated(String login){
        for (ClientHandler c:clients){
            if(c.getLogin().equals(login)){
                return true;
            }
        }
        return false;
    }




    public void subscribe(ClientHandler clientHandler){
        clients.add(clientHandler);
        broadcastClientList();
    };


    public void unsubscribe(ClientHandler clientHandler){
        clients.remove(clientHandler);
        broadcastClientList();
    };

    public AuthService getAuthService() {
        return authService;
    }
}
