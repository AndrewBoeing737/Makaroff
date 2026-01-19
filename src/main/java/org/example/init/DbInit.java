package org.example.init;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class DbInit {

    private final JdbcTemplate jdbc;

    public DbInit(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @PostConstruct
    public void init() {
        jdbc.execute("CREATE TABLE IF NOT EXISTS users (id IDENTITY PRIMARY KEY,login VARCHAR(100) UNIQUE NOT NULL,password VARCHAR(100) NOT NULL)");
    }
}
