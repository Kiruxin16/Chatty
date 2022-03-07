package server;

public interface AuthService {
    /**
     * Метод получения никнейма по логину и паролю
     * @return null если учетка не найдена
     * @return nickname если учетка найдена
     */
    String getNicknameByLoginAndPassword(String login, String password);

    boolean isNameExist(String name);

    /**
     *метод для регистрации учетной записи
     * @return true при успешнойфрегистрации
     * @return false если логин или никнейм заянты, и регистрация не получилась
     */
    boolean registration(String login,String password,String nickname);

}
