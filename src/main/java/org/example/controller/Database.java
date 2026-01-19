package org.example.controller;

import java.sql.*;

public class Database {

    private static final String URL =
            "jdbc:h2:file:./data/appdb;AUTO_SERVER=TRUE";
    private static final String USER = "sa";
    private static final String PASS = "";
    public static void Init() {

        // инициализация БД один раз
        try (Connection c = DriverManager.getConnection(URL, USER, PASS);
             Statement st = c.createStatement()) {

            st.execute("CREATE TABLE IF NOT EXISTS users (id IDENTITY PRIMARY KEY,login VARCHAR(100),password VARCHAR(100))");

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }
    /* ===============================
       Добавить пользователя
       =============================== */
    public static String addUser(String login, String password) {
        try (Connection c = DriverManager.getConnection(URL, USER, PASS);
             PreparedStatement ps = c.prepareStatement(
                     "INSERT INTO users(login, password) VALUES (?, ?)")) {

            ps.setString(1, login);
            ps.setString(2, password);
            ps.executeUpdate();

            return "OK: пользователь добавлен";

        } catch (SQLException e) {
            return "ERROR: " + e.getMessage();
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
    public static String checkLogin(String login, String password) {
        try (Connection c = DriverManager.getConnection(URL, USER, PASS);
             PreparedStatement ps = c.prepareStatement(
                     "SELECT COUNT(*) FROM users WHERE login=? AND password=?")) {

            ps.setString(1, login);
            ps.setString(2, password);

            ResultSet rs = ps.executeQuery();
            rs.next();

            return rs.getInt(1) == 1
                    ? "OK"
                    : "FAIL";

        } catch (SQLException e) {
            return "ERROR: " + e.getMessage();
        }
    }
}
