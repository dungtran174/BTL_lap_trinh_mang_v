package client.controller;

import client.network.ClientListening;
import client.view.*;
import javafx.scene.Scene;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Screen;
import javafx.stage.Stage;
import shared.dto.IPAddress;
import shared.dto.ObjectWrapper;
import shared.model.Player;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import javafx.scene.media.MediaPlayer;

public class ClientCtr {

    private static ClientCtr instance;

    private Socket mySocket;
    private ClientListening myListening;
    private volatile boolean isConnected = false;
    private IPAddress serverAddress = new IPAddress("26.161.164.36", 8888); // Thay bằng IP Radmin VPN của máy Server

    private ObjectOutputStream oos;

    private String username;
    private Stage stage;
    private int points;
    private MediaPlayer backgroundMusicPlayer;

    private ConnectFrm connectFrm;
    private LoginFrm loginFrm;

    private MainFrm mainFrm;
    private SetShipFrm setShipFrm;
    private WaitingFrm waitingFrm;
    private ImageQuizFrm imageQuizFrm;
    private PlayFrm playFrm;
    private ResultFrm resultFrm;
    private HistoryFrm historyFrm;
    private RankingFrm rankingFrm;
    private RegisterFrm registerFrm;

    private Scene loginScreen;
    private Scene mainScene;
    private Scene setShipScene;
    private Scene waitingScene;
    private Scene imageQuizScene;
    private Scene playScene;
    private Scene resultScene;
    private Scene historyScene;
    private Scene rankingScene;
    private Scene registerScene;


    public ClientCtr (){}

    public ClientCtr(ConnectFrm connectFrm) throws IOException {
        this.connectFrm = connectFrm;
    }

    public ClientCtr(ConnectFrm connectFrm, IPAddress serverAddr) throws IOException {
        this.connectFrm = connectFrm;
        this.serverAddress = serverAddr;
    }

    // Method static để lấy instance duy nhất của class
    public static ClientCtr getInstance() {
        // Nếu chưa có instance thì tạo mới
        if(instance == null) {
            instance = new ClientCtr();
        }
        // Trả về instance đã có
        return instance;
    }

    public boolean openConnection() {
        try {
            mySocket = new Socket(serverAddress.getHost(), serverAddress.getPort());
            this.oos = new ObjectOutputStream(mySocket.getOutputStream());
            myListening = new ClientListening(instance);
            myListening.start();
            isConnected = true;
            connectFrm.showMessage("Connected to the server at host: " + serverAddress.getHost() + ", port: " + serverAddress.getPort());
        } catch (Exception e) {
            connectFrm.showMessage("Error when connecting to the server!");
            return false;
        }
        return true;
    }

    public boolean sendData(Object obj) {
        try {
            oos.writeObject(obj);
            ObjectWrapper check = (ObjectWrapper) obj;
            Player test = (Player) check.getData();
            System.out.println(test.getUsername() + " " + test.getPassword());
            oos.flush();
        } catch (Exception e) {
            connectFrm.showMessage("Error when sending data to the server!");
            return false;
        }
        return true;
    }

    public boolean closeConnection() {
        if (!isConnected) {
            return true;
        }
        try {
            isConnected = false;
            if (myListening != null) {
                myListening.stopListening();
            }
            if (mySocket != null) {
                mySocket.close();
            }
            connectFrm.showMessage("Disconnected from the server!");
            return true;
        } catch (Exception e) {
            connectFrm.showMessage("Error when disconnecting from the server!");
            return false;
        }
    }

    public void setInstance(ClientCtr clientCtr) {instance = clientCtr;}

    public ConnectFrm getConnectFrm() {
        return connectFrm;
    }

    public Socket getMySocket() {
        return mySocket;
    }

    public LoginFrm getLoginFrm() {
        return loginFrm;
    }

    public RegisterFrm getRegisterFrm() {
        return registerFrm;
    }

    public void setRegisterFrm(RegisterFrm registerFrm) {
        this.registerFrm = registerFrm;
    }

    public void setLoginFrm(LoginFrm loginFrm) {
        this.loginFrm = loginFrm;
    }

    public MainFrm getMainFrm() {
        return mainFrm;
    }

    public void setMainFrm(MainFrm mainFrm) {
        this.mainFrm = mainFrm;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public void setStage(Stage stage) {this.stage = stage;}

    public Stage getStage() {return stage;}

    public HistoryFrm getHistoryFrm() {
        return historyFrm;
    }

    public void setHistoryFrm(HistoryFrm historyFrm) {
        this.historyFrm = historyFrm;
    }

    public RankingFrm getRankingFrm() {
        return rankingFrm;
    }

    public void setRankingFrm(RankingFrm rankingFrm) {
        this.rankingFrm = rankingFrm;
    }

    public SetShipFrm getSetShipFrm() {
        return setShipFrm;
    }

    public void setSetShipFrm(SetShipFrm setShipFrm) {
        this.setShipFrm = setShipFrm;
    }

    public PlayFrm getPlayFrm() {
        return playFrm;
    }

    public void setPlayFrm(PlayFrm playFrm) {
        this.playFrm = playFrm;
    }

    public ResultFrm getResultFrm() {
        return resultFrm;
    }

    public void setResultFrm(ResultFrm resultFrm) {
        this.resultFrm = resultFrm;
    }

    public Scene getLoginScreen() {return loginScreen;}

    public void setLoginScreen(Scene loginScreen) {this.loginScreen = loginScreen;}

    public Scene getMainScene() {return mainScene;}

    public void setMainScene(Scene mainScene) {this.mainScene = mainScene;}

    public Scene getSetShipScene() {return setShipScene;}

    public void setSetShipScene(Scene setShipScene) {this.setShipScene = setShipScene;}

    public WaitingFrm getWaitingFrm() {return waitingFrm;}

    public void setWaitingFrm(WaitingFrm waitingFrm) {this.waitingFrm = waitingFrm;}

    public Scene getWaitingScene() {return waitingScene;}

    public void setWaitingScene(Scene waitingScene) {this.waitingScene = waitingScene;}

    public ImageQuizFrm getImageQuizFrm() {return imageQuizFrm;}

    public void setImageQuizFrm(ImageQuizFrm imageQuizFrm) {this.imageQuizFrm = imageQuizFrm;}

    public Scene getImageQuizScene() {return imageQuizScene;}

    public void setImageQuizScene(Scene imageQuizScene) {this.imageQuizScene = imageQuizScene;}

    public Scene getPlayScene() {return playScene;}

    public void setPlayScene(Scene playScene) {this.playScene = playScene;}

    public Scene getResultScene() {return resultScene;}

    public void setResultScene(Scene resultScene) {this.resultScene = resultScene;}

    public Scene getHistoryScene() {return historyScene;}

    public void setHistoryScene(Scene historyScene) {this.historyScene = historyScene;}

    public Scene getRankingScene() {return rankingScene;}

    public void setRankingScene(Scene rankingScene) {this.rankingScene = rankingScene;}

    public Scene getRegisterScene() {return registerScene;}

    public void setRegisterScene(Scene registerScene) {this.registerScene = registerScene;}

    public MediaPlayer getBackgroundMusicPlayer() {
        return backgroundMusicPlayer;
    }

    public void setBackgroundMusicPlayer(MediaPlayer backgroundMusicPlayer) {
        this.backgroundMusicPlayer = backgroundMusicPlayer;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public int getPoints() {
        return points;
    }
}
