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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.sql.Time;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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
                                 @RequestParam(required = false, defaultValue = "Гость") String password) throws IOException {
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
        if(client==null){
            return loginForm();
        }
        String page= readResourceHtml("static/filemanager.html");
        page=page.replace("*ACCAUNT_FOR_REPLACE*",client.getLogin());
        String files="";
        for(int i=0;i<client.clientFiles.size();++i){
            files+=" <div class=\"file-card\">\n <form class=\"file-form\" method=\"get\" action=\"/files/downloads\" enctype=\"multipart/form-data\">"+
                    "<button class=\"file-download-btn\" type=\"submit\">"+
                    "<div class=\"file-name\">"+client.clientFiles.get(i).getName()+"</div>"+
                    "<div class=\"file-meta\">"+
                            client.clientFiles.get(i).getFiletypeinString()+ " · "+ String.valueOf( client.clientFiles.get(i).getStringSize())+" · "+ LocalDateTime.now() +
                    "</div>"+
                    "<input type=\"hidden\" name=\"filename\" value=\""+client.clientFiles.get(i).getName()+"\">"+
                    "</button>"+
                    "</form>"+
                    "<form class=\"delete-form\" method=\"post\" action=\"/files/any\">"+
                    "<input type=\"hidden\" name=\"filename\""+
            "value=\""+client.clientFiles.get(i).getName()+"\">"+
                    "<button class=\"delete-btn\" type=\"submit\">"+
                    "Прочее"+
                    "</button>"+
                    "</form>\n </div>\n";
        }
        page=page.replace("Загрузите ваши файлы",files);
        System.out.println("Страница files загружена пользователем: "+client.getLogin());
        return page;
    }
    @PostMapping("/upload")
    public RedirectView GetFile(@RequestParam("file")MultipartFile file){
        if(file.isEmpty()){
            RedirectView rv=new RedirectView("/filemanager");
        }
        System.out.println("Принят новый файл от пользователя "+client.getLogin()+" название файла и размер: "+file.getOriginalFilename()+" "+file.getSize());
        try{
            File dir =new File(client.getBaseFolder());
            File save=new File(dir,file.getOriginalFilename().replace(" ","_"));
            file.transferTo(save);
            ClientFile newfile=new ClientFile(save.getAbsoluteFile());
            client.clientFiles.add(newfile);
            database.Addfile(client,newfile);

            RedirectView rv=new RedirectView("/files");
            return rv;
        } catch (IOException e) {
            System.out.println(e.toString());
            RedirectView rv=new RedirectView("/settings");
            return rv;

        }
    }
    @GetMapping("/files/downloads")
    public void downloadFile(@RequestParam("filename") String filename,
                             HttpServletResponse response) {


        try {
            File file = new File(database.getFileWay(client,filename));

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
    @PostMapping ("/delete")
    public RedirectView deleteFile(@RequestParam("filename") String filename) {
        filename=new String(
                filename.getBytes(StandardCharsets.ISO_8859_1),
                StandardCharsets.UTF_8
        );
            File file = new File(database.getFileWay(client,filename));
            if(database.Deletefile(file.getName(),client)){
                if(file.delete()){
                    System.out.println("Пользователь "+client.getLogin()+" удалил файл "+filename);
                }else{
                    System.out.println("Пользователь "+client.getLogin()+"пытался удалить файл "+filename+ "но не получилось");
                    RedirectView rv=new RedirectView("/error");
                    return rv;
                }
            }else{
                    System.out.println("Пользователь "+client.getLogin()+"пытался удалить файл "+filename+" но не получилось");
                RedirectView rv=new RedirectView("/error");
                return rv;
            }

        RedirectView rv=new RedirectView("/files",true,false);
        return rv;

    }
    @PostMapping ("/files/any")
    public String anyFile(@RequestParam("filename") String filename) {
        filename=new String(
                filename.getBytes(StandardCharsets.ISO_8859_1),
                StandardCharsets.UTF_8
        );
        File file = new File(database.getFileWay(client,filename));
        ClientFile clientFile=new ClientFile(file);
        String page=readResourceHtml("static/other.html");
        page=page.replace("*ACCAUNT_FOR_REPLACE*",client.getLogin());
        page=page.replace("*FILENAME*",clientFile.getName());
        page=page.replace("*FILESIZE*",clientFile.getStringSize());
        page=page.replace("*FILETYPE*",String.valueOf(clientFile.getFiletype()));
        page=page.replace("*FILEOWNERS*", client.getLogin());
        return page;
    }

    @GetMapping ("/share")
    public String sharing(@RequestParam("filename") String filename){
        String page=readResourceHtml("static/share.html");
        page=page.replace("*ACCOUNT_FOR_REPLACE*", client.getLogin());
        page=page.replace("*FILENAME*",filename);
        List<Client> clints=database.GetAllClints();
        String clientsList="";
        for(int i=0;i<clints.size();++i){
            if(clints.get(i).getLogin().equals(client.getLogin())){continue;}
            clientsList+=" <label class=\"user-tile\">\n" +
                    "                    <input type=\"checkbox\" name=\"users\" value=\""+clints.get(i).getLogin()+"\">\n" +
                    "                    <div class=\"user-name\">"+clints.get(i).getLogin()+"</div>\n" +
                    "                </label>";
        }
        page=page.replace("Делиться не с кем",clientsList);
        return page;

    }
    @PostMapping("/share/in")
    public RedirectView share(
            @RequestParam("users") List<String> users,
            @RequestParam("filename") String filename
    ) {
        ClientFile clientFile = null;
        for(int i=0;i<client.clientFiles.size();++i){
            if(client.clientFiles.get(i).getName().equals(filename)){
                clientFile=client.clientFiles.get(i);
                break;
            }
        }

        if(clientFile==null){
            RedirectView rv=new RedirectView("/error");
            return rv;
        }

        System.out.println(clientFile.getName()+" "+clientFile.getFileway());
        for(int i=0;i<users.size();++i){
            Client tmp_client=new Client(users.get(i));
            database.getClientWithoutFiles(users.get(i),tmp_client);
            System.out.println(tmp_client.getId());
            System.out.println(tmp_client.getLogin()+" "+tmp_client.getId()+" "+tmp_client.getBaseFolder());
            database.Addfile(tmp_client,clientFile);
        }

        RedirectView rv=new RedirectView("/files");
        return rv;
    }
    @GetMapping("/settings")
    public String settings() throws SQLException {
        String page= readResourceHtml("static/settings.html");
        page=page.replace("*ACCAUNT_FOR_CHANGE*",client.getLogin());
        if(Database.BrowserIsOn()){
           page= page.replace("*H2BrowserEnabled*","checked");
        }else{page=page.replace("*H2BrowserEnabled*","unchecked");}
        return page;
    }
    @GetMapping("/error")
    public String errorPage(){
        return readResourceHtml("static/error.html");
    }
    @PostMapping("/settings/h2browser")
    public RedirectView h2browser(@RequestParam(value = "enabled",required = false) boolean h2) throws SQLException {
        if(h2){
            Database.startBrowser();
        }else {
            Database.stopBrowser();
        }
       RedirectView rv=new RedirectView("/settings");
        return rv;
    }
    @PostMapping("/settings/username")
    public RedirectView changeUsername(@RequestParam(value ="username") String username)  {
        try {
            database.RenameUser(client, username);
        } catch (Exception e) {
            RedirectView rv=new RedirectView("/error");
            return rv;
        }
        RedirectView rv=new RedirectView("/settings");
        return rv;
    }
    @GetMapping("/share/linkfile")
    public String Sharelink(@RequestParam(value="filename") String filename,@RequestParam(value="once_share",required = false) boolean once){
                String page=sharing(filename);
                String newWindow=
                        "\n" +
                        "<div class=\"modal-overlay\" id=\"modal\">\n" +
                        "\n" +
                        "    <div class=\"modal\">\n" +
                        "\n" +
                        "        <div class=\"modal-title\">Ссылка на файл</div>\n" +
                        "\n" +
                        "        <div class=\"link-box\" id=\"shareLink\">\n" +
                                database.Sharinglink(client,filename,once)+"\n" +
                        "        </div>\n" +
                        "\n" +
                        "        <button class=\"copy-btn\" type=\"button\" id=\"copyBtn\">\n" +
                        "            Копировать\n" +
                        "        </button>\n" +
                        "\n" +
                        "        <!-- теперь как плашка -->\n" +
                        "        <div class=\"copy-tile\" id=\"copyTile\">\n" +
                        "            Скопировано, для вставки нажмите Ctrl + V\n" +
                        "        </div>\n" +
                        "\n" +
                        "        <button class=\"modal-back-btn\" type=\"button\" id=\"closeModal\">\n" +
                        "            Назад\n" +
                        "        </button>\n" +
                        "\n" +
                        "    </div>\n" +
                        "\n" +
                        "</div>";
                page=page.replace("<!-- MODAL -->",newWindow);

                return page;

    }
    @GetMapping("/sharingfile/{id}")
    public String IdSharingLink(@PathVariable("id") int id){
        String page=readResourceHtml("static/other.html");
        if(database==null){
            database=new Database();
        }
        String buttons_to_delete=" " +
                "<form action=\"/share\" method=\"get\">\n" +
                "      <input type=\"hidden\" name=\"filename\" value= *FILENAME*>\n" +
                "      <button class=\"action-btn share-btn\" type=\"submit\">\n" +
                "        Поделиться\n" +
                "      </button>\n" +
                "    </form>\n" +
                "\n" +
                "    <form action=\"/delete\" method=\"post\">\n" +
                "    <input type=\"hidden\" name=\"filename\" value= *FILENAME*>\n" +
                "    <button class=\"action-btn delete-btn\" type=\"submit\">\n" +
                "      Удалить\n" +
                "    </button>\n" +
                "    </form>";
        ClientFile clientFile=database.GetFileShared(id);

        page=page.replace(buttons_to_delete,"");
        page=page.replace("*ACCAUNT_FOR_REPLACE*","");
        page=page.replace("*FILENAME*",clientFile.getName());
        page=page.replace("*FILESIZE*",clientFile.getStringSize());
        page=page.replace("*FILETYPE*",String.valueOf(clientFile.getFiletype()));
        page=page.replace("*FILEOWNERS*", "");
        page=page.replace("<form action=\"/files/downloads\" method=\"get\">","<form action=\"/sharingfile/"+id+"/download\" method=\"get\">");
        return page;
    }
    @GetMapping("/sharingfile/{id}/download")
    public void downloadFileFromLink(@PathVariable("id") int id,
                             HttpServletResponse response) {
        try {
            ClientFile clientFile=database.GetFileShared(id);
            File file = new File(clientFile.getFileway());

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
}

