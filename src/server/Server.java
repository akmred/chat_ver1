package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;
import java.lang.NullPointerException;

public class Server {
    private Vector<ClientHandler> clients;
    private AuthService authService;

    public AuthService getAuthService() {
        return authService;
    }

    public ClientHandler getClients(String name) {

        for (ClientHandler c:clients ) {
            if (c.getNick() == name){
                return c;
            }

        }
        return null;
    }

    public Server() {
        clients = new Vector<>();
        authService = new SimpleAuthService();
        ServerSocket server = null;
        Socket socket = null;

        try {
            server = new ServerSocket(8186);
            System.out.println("Сервер запущен");

            while (true){
                socket = server.accept();
                System.out.println("Клиент подключился");
                new ClientHandler(this,socket);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                server.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    public void broadcastMsg(String msg){
        for (ClientHandler c:clients ) {
            c.sendMsg(msg);
        }
    }

    public void privateMsg(String msg, String nick){

        for (ClientHandler c:clients ) {
            if (c.getNick().equals(nick))
                c.sendMsg(msg);
        }

    }

    public void subscribe(ClientHandler clientHandler){
        clients.add(clientHandler);
    }

    public void unsubscribe(ClientHandler clientHandler){
        clients.remove(clientHandler);
    }
}
