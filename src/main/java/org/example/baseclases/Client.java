package org.example.baseclases;

import java.util.List;
import java.util.ArrayList;

public class Client {
    private String login;
    private String baseFolder;
    public List<ClientFile> clientFiles;
    public Client(String _login){
        login=_login;
        clientFiles=new ArrayList<>();
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }
    public void AddFile(String fileway){
        ClientFile clientFile=new ClientFile(fileway);
        clientFiles.add(clientFile);
    }
    public void AddFiles(List<String> fileway){
        for(int i=0;i<fileway.size();i++){
            ClientFile clientFile=new ClientFile(fileway.get(i));
            clientFiles.add(clientFile);
        }
    }

    public String getBaseFolder() {
        return baseFolder;
    }

    public void setBaseFolder(String baseFolder) {
        this.baseFolder = baseFolder;
    }
}
