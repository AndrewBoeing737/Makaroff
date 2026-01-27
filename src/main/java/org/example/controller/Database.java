package org.example.controller;
import org.example.baseclases.Client;
import org.example.baseclases.ClientFile;
import org.h2.tools.Server;
import org.springframework.web.servlet.view.RedirectView;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Database {
    private static Server webServer;

    public static void startBrowser() throws SQLException {
        // Запустить веб-консоль на порту 8082
        if (webServer != null){
            return;
        }
        webServer = Server.createWebServer("-web", "-webAllowOthers", "-webPort", "8085").start();
        System.out.println("H2 Console доступна: http://localhost:8085");
    }

    private static final String URL =
            "jdbc:h2:file:./data/appdb;AUTO_SERVER=TRUE";
    private static final String USER = "sa";
    private static final String PASS = "";

    public static void Init() {

        try (Connection c = DriverManager.getConnection(URL, USER, PASS);
             Statement st = c.createStatement()) {

            st.execute("CREATE TABLE IF NOT EXISTS users (id IDENTITY PRIMARY KEY,login VARCHAR(100) UNIQUE NOT NULL,password VARCHAR(100) NOT NULL);");
           // st.execute("CREATE TABLE IF NOT EXISTS files (id IDENTITY PRIMARY KEY,name VARCHAR(100) NOT NULL,password VARCHAR(100) NOT NULL);");

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
    public void MakeClient(Client client) {
        StringBuilder sb = new StringBuilder();

        try (Connection c = DriverManager.getConnection(URL, USER, PASS);
             Statement st = c.createStatement();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT id,folder FROM users WHERE login=?")) {

            ps.setString(1, client.getLogin());

            ResultSet rs = ps.executeQuery();
            rs.next();
            client.setId(rs.getInt("id"));
            client.setBaseFolder(rs.getString("folder"));
            } catch (SQLException ex) {
            System.out.println(ex.toString());
        }



    }
    public void Addfile(Client client, ClientFile clientFile) {


        try (Connection c = DriverManager.getConnection(URL, USER, PASS);
             Statement st = c.createStatement();
             PreparedStatement ps = c.prepareStatement(
                     "INSERT INTO files (FILE_NAME,FILE_EXTENSION,FILE_PATH,FILE_SIZE,FILE_TYPE,OWNER_ID,UPDATED_AT) VALUES(?,?,?,?,?,?,CURRENT_TIMESTAMP)")) {

            ps.setString(1, clientFile.getName());
            ps.setString(2, clientFile.getExtension());
            ps.setString(3, clientFile.getFileway());
            ps.setLong(4, clientFile.getSize());
            ps.setString(5, clientFile.getFiletypeinString());
            ps.setLong(6,client.getId());
            int rs = ps.executeUpdate();


        } catch (SQLException ex) {
            System.out.println(ex.toString());
        }

    }
    public List<ClientFile> GetFilesFromUser(Client client) {

        List<ClientFile> clientFiles=new ArrayList<>();
        try (Connection c = DriverManager.getConnection(URL, USER, PASS);
             Statement st = c.createStatement();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT FILE_PATH FROM files WHERE OWNER_ID = ?")) {

            ps.setLong(1, client.getId());
            ResultSet rs = ps.executeQuery();
            while(rs.next()){
                String path=rs.getString("FILE_PATH");
                clientFiles.add(new ClientFile(path));
            }
            return clientFiles;
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            return clientFiles;
        }

    }
}
