package server;

import java.sql.*;


public class DBHandler {
    private Connection connection;
    private Statement statement;
    private PreparedStatement psInsert;

    public DBHandler() {

        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void connect() throws Exception{

        connection=DriverManager.getConnection("jdbc:sqlite:users.db");
        statement=connection.createStatement();
    }

    public boolean isUserExist(String name) throws SQLException{

        ResultSet rs=statement.executeQuery("SELECT FROM users WHERE login="+name+";");
        if(rs.next()){
            return true;
        }
        return false;
    }


    public void changeName(String oldName, String newName)throws SQLException{
        psInsert = connection.prepareStatement("UPDATE users SET nickname =? WHERE nickname=?;");
        psInsert.setString(1,newName);
        psInsert.setString(2,oldName);
        psInsert.executeUpdate();
    }

    public String getNickname(String login, String password) throws SQLException{
        psInsert = connection.prepareStatement("SELECT nickname FROM users WHERE login=? AND password=?;");
        psInsert.setString(1,login);
        psInsert.setString(2,password);
        String result =null;
        ResultSet rs=psInsert.executeQuery();
        if (rs.next()){
            result=rs.getString(1);
        }
        return result;
    }

    public void addUser(String login, String password, String nickname) throws SQLException{
        psInsert = connection.prepareStatement("INSERT INTO users (login,password,nickname) VALUES (?,?,?)");
        psInsert.setString(1,login);
        psInsert.setString(2,password);
        psInsert.setString(3,nickname);
        psInsert.executeUpdate();

    }



    public  void disconnect(){
        try {

            statement.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        try {
            connection.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

    }


    


}
