package org.example.controller;

import org.example.baseclases.Client;
import org.example.baseclases.ClientFile;
import org.h2.tools.Server;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
            client=new Client(login);
            database.MakeClient(client);
            client.clientFiles=database.GetFilesFromUser(client);
            System.out.println("Успешно зарегестрировался пользоваель: "+client.getId()+" "+client.getLogin()+" "+client.getBaseFolder());
            redirectView.setUrl("/files");
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
        String files="";
        for(int i=0;i<client.clientFiles.size();++i){
            files+="<form class=\"file-form\" method=\"get\" action=\"/files/downloads\" enctype=\"multipart/form-data\">"+
                    "<button class=\"file-card\">"+
                    "<div class=\"file-name\">"+client.clientFiles.get(i).getName()+"</div>"+
                    "<div class=\"file-meta\">"+
                            client.clientFiles.get(i).getFiletypeinString()+ "·"+ String.valueOf( client.clientFiles.get(i).getSize())+"·"+" 10.02.2026 22:41"+
                    "</div>"+
                    "<input type=\"hidden\" name=\"filename\" value=\""+client.clientFiles.get(i).getName()+"\">"+
                    "</button>"+
                    "</form>";
        }
        page=page.replace("Загрузите ваши файлы",files);
        return page;
    }
    @PostMapping("/files")
    public RedirectView GetFile(@RequestParam("file")MultipartFile file){
        if(file.isEmpty()){
            RedirectView rv=new RedirectView("/filemanager");
        }
        System.out.println("Принят новый файл от пользователя "+client.getLogin()+" название файла и размер: "+file.getOriginalFilename()+" "+file.getSize());
        try{
            File dir =new File(client.getBaseFolder());
            File save=new File(dir,file.getOriginalFilename());
            file.transferTo(save);
            ClientFile newfile=new ClientFile(save.getAbsoluteFile());
            client.clientFiles.add(newfile);
            database.Addfile(client,newfile);

            RedirectView rv=new RedirectView("/files");
            return rv;
        } catch (IOException e) {
            System.out.println(e.toString());
            RedirectView rv=new RedirectView("/files");
            return rv;

        }
    }
    @GetMapping("/files/downloads")
    public void downloadFile(@RequestParam("filename") String filename,
                             HttpServletResponse response) {
        try {
            File file = new File(client.getBaseFolder(), filename);

            if (!file.exists() || !file.isFile()) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            String contentType = Files.probeContentType(file.toPath());
            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            response.setContentType(contentType);
            response.setContentLengthLong(file.length());

            response.setHeader(HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=\"" + file.getName() + "\"");

            // Отправляем файл
            try (FileInputStream fis = new FileInputStream(file);
                 OutputStream os = response.getOutputStream()) {

                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = fis.read(buffer)) != -1) {
                    os.write(buffer, 0, bytesRead);
                }
                os.flush();
            }

        } catch (IOException e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
    @GetMapping("/files/delete")
    public void deleteFile(@RequestParam("filename") String filename) {
            File file = new File(client.getBaseFolder(), filename);
            if(database.Deletefile(file.getName(),client)){
                if(file.delete()){
                    System.out.println("Пользователь "+client.getLogin()+" удалил файл "+filename);
                }else{
                    System.out.println("Пользователь "+client.getLogin()+"пытался удалить файл "+filename+ "но не получилось");
                }
            }else{
                    System.out.println("Пользователь "+client.getLogin()+"пытался удалить файл "+filename+" но не получилось");
            }

    }
}

