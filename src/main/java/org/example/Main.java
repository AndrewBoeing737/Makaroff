package org.example;

//import com.sun.security.ntlm.Server;
import org.apache.catalina.Context;
import org.apache.catalina.startup.Tomcat;
import org.example.config.WebConfig;
import org.example.controller.Database;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

import java.io.File;
import java.sql.*;

public class Main {


  private static final String URL =
            "jdbc:h2:file:./data/appdb;AUTO_SERVER=TRUE";
    private static final String USER = "sa";
    private static final String PASS = "";
    public static void main(String[] args) throws Exception {
        Database.Init();
        Database.startBrowser();

        System.out.println(Database.getUsersAsString());
        Tomcat tomcat = new Tomcat();
        tomcat.setPort(8080);

        // 🔴 КРИТИЧЕСКИ ВАЖНО
        tomcat.getConnector(); // ← БЕЗ ЭТОГО ПОРТ НЕ ОТКРОЕТСЯ

        File baseDir = new File(System.getProperty("java.io.tmpdir"));
        Context context = tomcat.addContext("", baseDir.getAbsolutePath());

        AnnotationConfigWebApplicationContext appContext =
                new AnnotationConfigWebApplicationContext();
        appContext.register(WebConfig.class);

        DispatcherServlet dispatcherServlet =
                new DispatcherServlet(appContext);

        Tomcat.addServlet(context, "dispatcher", dispatcherServlet);
        context.addServletMappingDecoded("/", "dispatcher");

        tomcat.start();
        tomcat.getServer().await();
    }
}
