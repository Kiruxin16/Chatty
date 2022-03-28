package server;

import java.sql.SQLException;

public class DBAuthService implements AuthService {

    private DBHandler dbHandler;


    public DBAuthService() {
        dbHandler = new DBHandler();

    }

    @Override
    public String getNicknameByLoginAndPassword(String login, String password) {
        String nickname=null; //мне показалось, что, если ввести строковую переменную код будет более очевиден
        try {
            dbHandler.connect();
            nickname = dbHandler.getNickname(login,password);
        }catch (Exception e){
            e.printStackTrace();
        }
        finally {
            dbHandler.disconnect();
            return nickname;

        }


    }

    @Override
    public boolean changeName(String oldName,String newName) {
        try {
            dbHandler.connect();
            dbHandler.changeName(oldName,newName);
            return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }finally {
            System.out.println("it works!!");
            dbHandler.disconnect();
        }
    }

    @Override
    public boolean isNameExist(String name) {
        try {
            dbHandler.connect();
            return dbHandler.isUserExist(name);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }finally {
            dbHandler.disconnect();
        }

    }

    @Override
    public boolean registration(String login, String password, String nickname) {
        try {
            dbHandler.connect();
            dbHandler.addUser(login,password,nickname);
            return true;
        }catch (Exception e){
            return false;
        }finally {
            System.out.println("its alive!!");
            dbHandler.disconnect();
        }

    }
}
