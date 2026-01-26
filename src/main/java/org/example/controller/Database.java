package org.example.controller;
import org.h2.tools.Server;
import java.sql.*;

public class Database {
    private static Server webServer;

    public  void startBrowser() throws SQLException {
        // Запустить веб-консоль на порту 8082
        webServer = Server.createWebServer("-web", "-webAllowOthers", "-webPort", "8085").start();
        System.out.println("H2 Console доступна: http://localhost:8085");
    }

    public  void stopBrowser() {
        if (webServer != null) {
            webServer.stop();
        }
    }

    private static final String URL =
            "jdbc:h2:file:./data/appdb;AUTO_SERVER=TRUE";
    private static final String USER = "sa";
    private static final String PASS = "";

    public static void Init() {

        try (Connection c = DriverManager.getConnection(URL, USER, PASS);
             Statement st = c.createStatement()) {

            st.execute("CREATE TABLE IF NOT EXISTS users (id IDENTITY PRIMARY KEY,login VARCHAR(100) UNIQUE NOT NULL,password VARCHAR(100) NOT NULL);");

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }
    /* ===============================
       Добавить пользователя
       =============================== */
    public boolean addUser(String login, String password) {
        try (Connection c = DriverManager.getConnection(URL, USER, PASS);
             PreparedStatement ps = c.prepareStatement(
                     "INSERT INTO users(login, password) VALUES (?, ?)")) {

            ps.setString(1, login);
            ps.setString(2, password);
            ps.executeUpdate();

            return true;

        } catch (SQLException e) {
            System.out.println( "ERROR: " + e.getMessage());
            return false;
        }
    }

    /* ===============================
       Получить всех пользователей
       =============================== */
    public static String getUsersAsString() {
        StringBuilder sb = new StringBuilder();

        try (Connection c = DriverManager.getConnection(URL, USER, PASS);
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(
                     "SELECT id, login FROM users")) {

            while (rs.next()) {
                sb.append("ID=")
                        .append(rs.getLong("id"))
                        .append(" LOGIN=")
                        .append(rs.getString("login"))
                        .append("\n");
            }

        } catch (SQLException e) {
            return "ERROR: " + e.getMessage();
        }

        return sb.toString();
    }

    /* ===============================
       Проверка логина
       =============================== */
    public boolean checkLogin(String login, String password) {
        try (Connection c = DriverManager.getConnection(URL, USER, PASS);
             PreparedStatement ps = c.prepareStatement(
                     "SELECT COUNT(*) FROM users WHERE login=? AND password=?")) {

            ps.setString(1, login);
            ps.setString(2, password);

            ResultSet rs = ps.executeQuery();
            rs.next();

            return rs.getInt(1) == 1;


        } catch (SQLException e) {
            System.out.println("ERROR: " + e.getMessage());
            return false;
        }
    }
}
