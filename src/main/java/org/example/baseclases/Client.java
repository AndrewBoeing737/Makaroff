package org.example.baseclases;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

public class Client {
    private String login;
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

}
