package org.example.baseclases;

public class Client {
    private String login;
    Client(String _login){
        login=_login;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }
}
