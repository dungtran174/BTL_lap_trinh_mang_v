package server.controller;

import server.network.ServerListening;
import server.network.ServerProcessing;
import server.view.ServerMainFrm;
import shared.dto.IPAddress;
import shared.dto.ObjectWrapper;
import shared.model.Player;

import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.ArrayList;

public class ServerCtr {

    private ServerMainFrm view;
    private ServerSocket myServer;
    private ServerListening myListening;
    private ArrayList<ServerProcessing> myProcess;
//    private IPAddress myAddress = new IPAddress("localhost", 8888);
    private IPAddress myAddress = new IPAddress("26.161.164.36", 8888);

    public ServerCtr(ServerMainFrm view) {
        myProcess = new ArrayList<ServerProcessing>();
        this.view = view;
        openServer();
    }

    public ServerCtr(ServerMainFrm view, int serverPort) {
        myProcess = new ArrayList<ServerProcessing>();
        this.view = view;
        myAddress.setPort(serverPort);
        openServer();
    }

    private void openServer() {
        try {
            // myServer = new ServerSocket(myAddress.getPort());
            myServer = new ServerSocket(myAddress.getPort(), 50, InetAddress.getByName(myAddress.getHost()));
            myListening = new ServerListening(this);
            myListening.start();
            // myAddress.setHost(InetAddress.getLocalHost().getHostAddress());
            view.showServerInfor(myAddress);
            System.out.println("server started!");
            view.showMessage("TCP server is running at the port " + myAddress.getPort() + "...");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stopServer() {
        try {
            for (ServerProcessing sp : myProcess) {
                sp.stopProcessing();
            }
            myListening.stopListening();
            myServer.close();
            view.showMessage("TCP server is stopped!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void addServerProcessing(ServerProcessing sp) {
        myProcess.add(sp);
        view.showMessage("Number of client connecting to the server: " + myProcess.size());
        try {
            publicClientNumber();
        } catch (Exception e) {
            System.err.println("Error in publicClientNumber after adding client: " + e.getMessage());
        }
    }

    public void removeServerProcessing(ServerProcessing sp) {
        myProcess.remove(sp);
        view.showMessage("Number of client connecting to the server: " + myProcess.size());
        try {
            publicClientNumber();
        } catch (Exception e) {
            System.err.println("Error in publicClientNumber after removing client: " + e.getMessage());
        }
    }

    public ServerMainFrm getView() {
        return view;
    }

    public ServerSocket getMyServer() {
        return myServer;
    }

    public ArrayList<ServerProcessing> getMyProcess() {
        return myProcess;
    }

    public void publicClientNumber() {
        ObjectWrapper data = new ObjectWrapper(ObjectWrapper.SERVER_INFORM_CLIENT_NUMBER, myProcess.size());
        for (ServerProcessing sp : myProcess) {
            try {
                if (sp != null && sp.isIsOnline()) {
                    sp.sendData(data);
                }
            } catch (Exception e) {
                System.err.println("Error sending client number to " + (sp != null ? sp.getUsername() : "unknown") + ": " + e.getMessage());
                // Client may have disconnected, continue with next client
            }
        }
    }

    public void sendWaitingList() {
        ArrayList<Player> listUsername = new ArrayList<>();
        System.out.println("myProcess: " + myProcess.size());
        for (ServerProcessing sp : myProcess) {
            if(sp != null && sp.isIsOnline()){
                Player player = sp.getPlayer();
                if (player != null) {
                    if(sp.isInGame()) player.setStatus("In game");
                    else player.setStatus("Online");
                    listUsername.add(player);
                }
            }
        }
        System.out.println("listUsername: " + listUsername.size());

        System.out.println("Server send waiting list:");
        System.out.println(listUsername);
        ObjectWrapper data = new ObjectWrapper(ObjectWrapper.SERVER_INFORM_CLIENT_WAITING, listUsername);
        System.out.println(data);
        
        // Send waiting list to all online clients
        for (ServerProcessing sp : myProcess) {
            try {
                if (sp != null && sp.isIsOnline()) {
                    sp.sendData(data);
                    Player player = sp.getPlayer();
                    if (player != null) {
                        System.out.println("Send to: " + player.getUsername());
                    } else {
                        System.out.println("Send to: " + sp.getUsername() + " (player object is null)");
                    }
                }
            } catch (Exception e) {
                System.err.println("Error sending waiting list to " + (sp != null ? (sp.getPlayer() != null ? sp.getPlayer().getUsername() : sp.getUsername()) : "unknown") + ": " + e.getMessage());
                // Client may have disconnected, continue with next client
            }
        }
    }

}
