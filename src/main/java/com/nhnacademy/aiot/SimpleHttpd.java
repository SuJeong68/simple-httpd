package com.nhnacademy.aiot;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class SimpleHttpd {
    public static final String DOCUMENT_ROOT = System.getProperty("user.dir");
    private int port = 80;

    public void setPort(int port) {
        this.port = port;
    }

    public void apply() {
        try (ServerSocket server = new ServerSocket(port)) {
            System.out.printf("Serving HTTP on 127.0.0.1 port %s (http://127.0.0.1:%s/) ...%s", port, port, "\r\n");

            Socket socket;
            while ((socket = server.accept()) != null) {
                ServiceHandler handler = new ServiceHandler(socket);
                handler.start();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        SimpleHttpd simpleHttpd = new SimpleHttpd();

        if (args.length > 0) {
            simpleHttpd.setPort(Integer.parseInt(args[0]));
        }

        simpleHttpd.apply();
    }
}
