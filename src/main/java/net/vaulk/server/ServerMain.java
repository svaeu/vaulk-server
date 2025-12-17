package net.vaulk.server;

import java.io.IOException;

public class ServerMain {
    public static void main(String[] args) {
        try {
            VaulkEdgeServer.getInstance().init().start();
        } catch (InterruptedException | IOException e) {
            throw new RuntimeException(e);
        }
    }
}
