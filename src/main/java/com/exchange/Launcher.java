package com.exchange;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;

public class Launcher {
    public static void main(String[] args) throws Exception {
        Server server = new Server(8080);

        WebAppContext context = new WebAppContext();
        context.setContextPath("/");
        context.setResourceBase("src/main/webapp");

        server.setHandler(context);
        server.start();

        System.out.println("ExChange запущен на http://localhost:8080");
        server.join();
    }
}