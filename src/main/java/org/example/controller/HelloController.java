package org.example.controller;

import org.example.baseclases.Client;
import org.h2.tools.Server;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;

@RestController
public class HelloController {
    Database database;
    Client client;
    private String readResourceHtml(String resourcePath) {
        InputStream is = getClass().getClassLoader().getResourceAsStream(resourcePath);
        if (is == null) {
            throw new RuntimeException("Файл не найден: " + resourcePath);
        }

        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append("\n");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return sb.toString();
    }

    // Главная страница
    @GetMapping("/")
    public String root() throws SQLException {
        database=new Database();

        return readResourceHtml("static/hello.html");
    }

    // Страница логина
    @GetMapping("/login")
    public String loginForm() {
        return readResourceHtml("static/login.html");
    }

    // Обработка логина
    @PostMapping("/login")
    public RedirectView loginSubmit(@RequestParam(required = false, defaultValue = "Гость") String login,
                              @RequestParam(required = false, defaultValue = "Гость") String password) {
        System.out.println("Login: " + login + "\tPassword: " + password);
        database=new Database();
        RedirectView redirectView = new RedirectView();

        if(database.checkLogin(login,password)){
            redirectView.setUrl("/files");

            client=new Client(login);
            return redirectView;
        }else{
            redirectView.setUrl("/register");
            return redirectView;
        }


    }

    // Страница регистрации
    @GetMapping("/register")
    public String registerForm() {
        return readResourceHtml("static/register.html");
    }

    // Обработка регистрации
    @PostMapping("/register")
    public RedirectView registerSubmit(@RequestParam(required = false, defaultValue = "Гость") String login,
                                 @RequestParam(required = false, defaultValue = "Гость") String password) {
        System.out.println("Register login: " + login + "\tPassword: " + password);
        RedirectView redirectView = new RedirectView();
        if(database.addUser(login,password)){
            redirectView.setUrl("/login");
            return redirectView;
        }else{
            redirectView.setUrl("/register");
            return redirectView;
        }

    }
    @GetMapping("/files")
    public String filesForm() {
        String page= readResourceHtml("static/filemanager.html");
        page=page.replace("*ACCAUNT_FOR_REPLACE*",client.getLogin());

        return page;
    }
}
