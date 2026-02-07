package org.example.baseclases;

import java.util.List;
import java.util.ArrayList;

public class Client {
    private String login;
    private String baseFolder;
    private int id;
    public List<ClientFile> clientFiles;
    public Client(String _login){
        login=_login;
        clientFiles=new ArrayList<>();
    }
    public Client(Client client){
        login=client.login;
        clientFiles=new ArrayList<>();
        for (int i=0;i<client.clientFiles.size();++i){
            clientFiles.add(client.clientFiles.get(i));
        }
    }
    public Client deepCopy(Client client){
        Client client1=new Client(client.getLogin());
        client1.clientFiles=new ArrayList<>();
        for (int i=0;i<client.clientFiles.size();++i){
            client1.clientFiles.add(client.clientFiles.get(i));
        }
        return client1;
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
    public void DeleteFile(String clientFile){
        for(int i=0;i<clientFiles.size();++i){
            if(clientFiles.get(i).getName().equals(clientFile)){
                clientFiles.remove(i);
                break;
            }
        }

    }

    public String getBaseFolder() {
        return baseFolder;
    }

    public void setBaseFolder(String baseFolder) {
        this.baseFolder = baseFolder;
    }
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
