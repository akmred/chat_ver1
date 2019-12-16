package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;

public class Server {
    private Vector<ClientHandler> clients;
    private AuthService authService;

    public AuthService getAuthService() {
        return authService;
    }

    public Server() {
        clients = new Vector<>();
        authService = new SimpleAuthService();
        ServerSocket server = null;
        Socket socket = null;

        try {
            server = new ServerSocket(8181);
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

    public void broadcastMsg(String nick, String msg){
        for (ClientHandler c:clients ) {
            c.sendMsg( nick +" : "+  msg);
        }
    }

    public void privateMsg(ClientHandler sender, String receiver, String msg){
        String message = String.format("[ %s ] private [ %s ] : %s",
                sender.getNick(), receiver, msg);

        for (ClientHandler c:clients ) {
            if(c.getNick().equals(receiver)){
                c.sendMsg(message);
                sender.sendMsg(message);
                return;
            }
        }
        sender.sendMsg("Пользователь с ником: "+ receiver +" не найден");
    }

    public void subscribe(ClientHandler clientHandler){
        clients.add(clientHandler);
        broadcastClientlist();
    }

    public void unsubscribe(ClientHandler clientHandler){
        clients.remove(clientHandler);
        broadcastClientlist();
    }

    public boolean isLoginAuthorized(String login){
        for (ClientHandler c:clients ) {
            if (c.getLogin().equals(login)){
                return true;
            }
        }
        return false;
    }

    public void broadcastClientlist(){
        StringBuilder sb = new StringBuilder("/clientlist ");
        for (ClientHandler c:clients ) {
            sb.append(c.getNick()+" ");
        }

        String msg = sb.toString();
        for (ClientHandler c:clients ) {
            c.sendMsg(msg);
        }
    }
}
