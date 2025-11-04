package client.view;

import client.controller.ClientCtr;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Pair;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.animation.FadeTransition;
import javafx.util.Duration;
import shared.dto.ObjectWrapper;
import javafx.application.Platform;
import shared.dto.PlayerHistory;
import shared.model.Match;
import shared.model.Player;
import java.time.format.DateTimeFormatter;
import java.util.*;


public class MainFrm {

    private ClientCtr mySocket = ClientCtr.getInstance();
    private Stage stage = mySocket.getStage();

    public MainFrm() {
    }

    public void openScene() {
        // Sau đó xử lý UI trong Platform.runLater
        Platform.runLater(() -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/Fxml/Client/Main.fxml"));
                Scene scene = new Scene(loader.load());
                mySocket.setMainScene(scene);
                stage.setScene(scene);
                stage.setTitle("Main");
                stage.show();

                //set username main lblUserName01
                Label lblUserName = (Label) loader.getNamespace().get("usernameMain");
                lblUserName.setText(mySocket.getUsername());

                // Lay button Home, History, Ranking va stackPane
                Button btnHome = (Button)  loader.getNamespace().get("btnHome");
                Button btnHistory = (Button)  loader.getNamespace().get("btnHistory");
                Button btnRanked = (Button)  loader.getNamespace().get("btnRanked");
                StackPane stackPane = (StackPane) loader.getNamespace().get("stackPane");

                FXMLLoader loaderHome = new FXMLLoader(getClass().getResource("/Fxml/Client/Home.fxml"));
                FXMLLoader loaderHistory = new FXMLLoader(getClass().getResource("/Fxml/Client/History.fxml"));
                FXMLLoader loaderRanked = new FXMLLoader(getClass().getResource("/Fxml/Client/Ranked.fxml"));

                AnchorPane home = loaderHome.load();
                AnchorPane history = loaderHistory.load();
                AnchorPane ranked = loaderRanked.load();

                stackPane.getChildren().addAll(home, history, ranked);

                home.setVisible(true);
                history.setVisible(false);
                ranked.setVisible(false);

                // Set point - rank UI Home
                Label lblPoints = (Label) loaderHome.getNamespace().get("lblPoints");
                ImageView flagRankHome = (ImageView) loaderHome.getNamespace().get("flagRankHome");
                lblPoints.setText(String.valueOf(mySocket.getPoints()));
                int point = mySocket.getPoints();
                if (point < 20) flagRankHome.setImage(new Image(getClass().getResource("/Images/flagIntern.png").toExternalForm()));
                else if (point < 40) flagRankHome.setImage(new Image(getClass().getResource("/Images/flagMaster.png").toExternalForm()));
                else if (point < 60) flagRankHome.setImage(new Image(getClass().getResource("/Images/flagGrandmaster.png").toExternalForm()));
                else flagRankHome.setImage(new Image(getClass().getResource("/Images/flagChallenger.png").toExternalForm()));


                btnHome.setStyle("-fx-text-fill: rgba(234, 251, 2, 0.7);" +
                        "-fx-background-color: linear-gradient(from 0% 10% to 0% 100%, rgba(248,198,187,0), rgba(156,153,141,0.3));");
                btnHistory.setStyle("-fx-text-fill: rgba(217, 223, 165, 0.7);");
                btnRanked.setStyle("-fx-text-fill: rgba(217, 223, 165, 0.7);");

                // Xu ly su kien click vao button Home
                btnHome.setOnAction(event -> {
                    try {
                        home.setVisible(true);
                        history.setVisible(false);
                        ranked.setVisible(false);

                        btnHome.setStyle("-fx-text-fill: rgba(234, 251, 2, 0.7);" +
                                "-fx-background-color: linear-gradient(from 0% 10% to 0% 100%, rgba(248,198,187,0), rgba(156,153,141,0.3));");
                        btnHistory.setStyle("-fx-text-fill: rgba(217, 223, 165, 0.7);" +
                                "-fx-background-color: transparent;");
                        btnRanked.setStyle("-fx-text-fill: rgba(217, 223, 165, 0.7);" +
                                "-fx-background-color: transparent;");

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });

                // Xu ly su kien click vao button History
                btnHistory.setOnAction(event -> {
                    try {
                        home.setVisible(false);
                        history.setVisible(true);
                        ranked.setVisible(false);

                        btnHome.setStyle("-fx-text-fill: rgba(217, 223, 165, 0.7); " +
                                "-fx-background-color: transparent;");
                        btnHistory.setStyle("-fx-text-fill: rgba(234, 251, 2, 0.7);" +
                                "-fx-background-color: linear-gradient(from 0% 10% to 0% 100%, rgba(248,198,187,0), rgba(156,153,141,0.3));");
                        btnRanked.setStyle("-fx-text-fill: rgba(217, 223, 165, 0.7);" +
                                "-fx-background-color: transparent;");

                        mySocket.sendData(new ObjectWrapper(ObjectWrapper.GET_HISTORY));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
                // Xu ly su kien click vao button Ranking

                btnRanked.setOnAction(event -> {
                    try {
                        home.setVisible(false);
                        history.setVisible(false);
                        ranked.setVisible(true);

                        btnHome.setStyle("-fx-text-fill: rgba(217, 223, 165, 0.7);" +
                                "-fx-background-color: transparent;");
                        btnHistory.setStyle("-fx-text-fill: rgba(217, 223, 165, 0.7);" +
                                "-fx-background-color: transparent;");
                        btnRanked.setStyle("-fx-text-fill: rgba(234, 251, 2, 0.7);" +
                                "-fx-background-color: linear-gradient(from 0% 10% to 0% 100%, rgba(248,198,187,0), rgba(156,153,141,0.3));");

                        mySocket.sendData(new ObjectWrapper(ObjectWrapper.GET_RANKING));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });

                //client send request to server to get all user
                mySocket.sendData(new ObjectWrapper(ObjectWrapper.GET_ALL_USER, null));

                //button logout
                Button btnLogout = (Button) loader.getNamespace().get("btnLogout");
                btnLogout.setOnAction(event -> {
                    mySocket.sendData(new ObjectWrapper(ObjectWrapper.EXIT_MAIN_FORM, null));
                    mySocket.setMainScene(null);
                    mySocket.setLoginScreen(null);

                    LoginFrm loginFrm = new LoginFrm();
                    mySocket.setLoginFrm(loginFrm);
                    mySocket.getLoginFrm().openScene();
                });

                //button refresh
                Button btnRefresh = (Button) loader.getNamespace().get("btnRefresh");
                btnRefresh.setOnAction(event -> {
                    mySocket.sendData(new ObjectWrapper(ObjectWrapper.GET_ALL_USER, null));
                    System.out.println("Refresh");
                });


            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public void receivedDataProcessing(ObjectWrapper data) {
        Platform.runLater(() -> {
            Scene scene = mySocket.getMainScene();

            switch (data.getPerformative()) {
                case ObjectWrapper.SERVER_SEND_ALL_USER:

                    if (scene == null) {
                        mySocket.getMainFrm().openScene();
                        break;
                    }

                    ArrayList<Player> listUser = (ArrayList<Player>) data.getData();
                    listUser.sort((o1, o2) -> o1.getUsername().compareTo(o2.getUsername()));


                    int numberUser = listUser.size();
                    System.out.println("numberUser: " + numberUser);

                    // cap nhat giao dien hien thi danh sach user
                    VBox screnListUser = (VBox) scene.lookup("#listUerVbox");
                    if(screnListUser == null) {
                        System.out.println("screnListUser is null");
                    } else {
                        System.out.println("screnListUser is not null");
                    }
                    screnListUser.setPrefHeight(41 * (numberUser + 1));
                    screnListUser.getChildren().clear();

                    for (Player player : listUser) {
                        if (player.getUsername().equals(mySocket.getUsername())) {
                            continue;
                        }

                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Fxml/Client/ItemUser.fxml"));
                        try {
                            HBox itemUser = loader.load();
                            Label lblUserName = (Label) loader.getNamespace().get("usernameItem");
                            Label lblStatus = (Label) loader.getNamespace().get("statusItem");
                            Circle circleStatus = (Circle) loader.getNamespace().get("circleStatus");
                            Button btnInvite = (Button) itemUser.lookup("#buttonInvite");

                            lblUserName.setText(player.getUsername());
                            lblStatus.setText("Offline");
                            circleStatus.setStyle("-fx-stroke: #262825");
                            lblStatus.setStyle("-fx-text-fill: #9E9E9E; -fx-font-weight: 700; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.8), 2, 0, 0, 1);");
                            btnInvite.setVisible(false);

                            screnListUser.getChildren().add(itemUser);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    mySocket.sendData(new ObjectWrapper(ObjectWrapper.UPDATE_WAITING_LIST_REQUEST, null));
                    break;

                case ObjectWrapper.SERVER_INFORM_CLIENT_WAITING:
                    // cap nhat status cua user
                    ArrayList<Player> listUserWaiting = (ArrayList<Player>) data.getData();
                    ArrayList<HBox> listUserItem = new ArrayList<>();
                    
                    // Check if scene is null
                    if (scene == null) {
                        System.err.println("MainFrm: Scene is null, cannot update waiting list");
                        break;
                    }
                    
                    VBox screnListUserWaiting = (VBox) scene.lookup("#listUerVbox");


                    HashMap<String, String> mapUserStatus = new HashMap<>();
                    ArrayList<String> listUserStatus = new ArrayList<>();

                    // dua cac user co o giao dien vao trong hashmap
                    for (int i = 0; i < screnListUserWaiting.getChildren().size(); i++) {
                        // lay ra item user
                        HBox itemUser = (HBox) screnListUserWaiting.getChildren().get(i);
                        AnchorPane anchorPane = (AnchorPane) itemUser.getChildren().getFirst();
                        Label lblUserName = (Label) anchorPane.lookup("#usernameItem");

                        // dua vao trong map va list
                        listUserStatus.add(lblUserName.getText());
                        mapUserStatus.put(lblUserName.getText(), "Offline");

                    }


                    // Cap nhat lai trang thai cua cua user
                    // Kiem tra xem co user moi nao khong (truong hop co user nao do moi tao tai khoan nen khong biet)
                    for (Player player : listUserWaiting) {
                        if (!mapUserStatus.containsKey(player.getUsername()) && !player.getUsername().equals(mySocket.getUsername()))
                            listUserStatus.add(player.getUsername());

                        if(player.getStatus().equals("Online")) mapUserStatus.put(player.getUsername(), "Online");
                        else mapUserStatus.put(player.getUsername(), "In Game");
                    }

                    // sort user theo status, name
                    listUserStatus.sort((o1, o2) -> {
                        String status01 = mapUserStatus.get(o1);
                        String status02 = mapUserStatus.get(o2);
                        if (status01.equals(status02)) return o1.compareTo(o2);
                        if(status01.equals("Online")) return -1;
                        if(status01.equals("In Game") && status02.equals("Offline")) return -1;
                        return 1;
                    });

                    // cap nhat lai giao dien
                    screnListUserWaiting.getChildren().clear();
                    for (String namePlayer : listUserStatus){
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Fxml/Client/ItemUser.fxml"));
                        try {
                            HBox itemUser = loader.load();
                            Label lblUserName = (Label) loader.getNamespace().get("usernameItem");
                            Label lblStatus = (Label) loader.getNamespace().get("statusItem");
                            Circle circleStatus = (Circle) loader.getNamespace().get("circleStatus");
                            Button btnInvite = (Button) itemUser.lookup("#buttonInvite");

                            lblUserName.setText(namePlayer);
                            lblStatus.setText(mapUserStatus.get(namePlayer));

                            btnInvite.setOnAction(event -> {
                                mySocket.sendData(new ObjectWrapper(ObjectWrapper.SEND_PLAY_REQUEST, namePlayer));
                            });


                            //check exit user in receive play request
                            HashMap<String, Pair<Label, Circle>> mapstatusInvite = new HashMap<>();
                            HashMap<String, HBox> mapHboxInvite = new HashMap<>();

                            VBox receivePlayRequest = (VBox) scene.lookup("#RECEIVE_PLAY_REQUEST");
                            for (int i = 0; i < receivePlayRequest.getChildren().size(); i++) {
                                HBox itemUserRequest = (HBox) receivePlayRequest.getChildren().get(i);
                                AnchorPane anchorPane = (AnchorPane) itemUserRequest.getChildren().getFirst();
                                Label lblUserNameInvite = (Label) anchorPane.lookup("#userNameInvite");
                                Label lblStatusInvite = (Label) anchorPane.lookup("#userInvitePlayGame");
                                Circle circleStatusInvite = (Circle) anchorPane.lookup("#circleStatusInvite");
                                mapstatusInvite.put(lblUserNameInvite.getText(), new Pair<>(lblStatusInvite, circleStatusInvite));
                                mapHboxInvite.put(lblUserNameInvite.getText(), itemUserRequest);
                            }

                            if(mapUserStatus.get(namePlayer).equals("Online")) {
                                circleStatus.setStyle("-fx-stroke: #00ff00");
                                lblStatus.setStyle("-fx-text-fill: #4CAF50; -fx-font-weight: 700; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.8), 2, 0, 0, 1);");
                                btnInvite.setVisible(true);
                                if(mapstatusInvite.containsKey(namePlayer)) {
                                    Pair<Label, Circle> pair = mapstatusInvite.get(namePlayer);
                                    pair.getKey().setText("Invite to play game");
                                    pair.getKey().setStyle("-fx-text-fill: rgba(251, 57, 57, 0.7)");
                                    pair.getValue().setStyle("-fx-stroke: #00ff00");

                                    HBox itemUserRequest = mapHboxInvite.get(namePlayer);
                                    Button btnAccept = (Button) itemUserRequest.lookup("#btnAccept");
                                    btnAccept.setVisible(true);

                                }

                            } else if(mapUserStatus.get(namePlayer).equals("In Game")) {
                                circleStatus.setStyle("-fx-stroke: #584ee6");
                                lblStatus.setStyle("-fx-text-fill: #6C5CE7; -fx-font-weight: 700; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.8), 2, 0, 0, 1);");
                                btnInvite.setVisible(false);

                                if(mapHboxInvite.containsKey(namePlayer)) {
                                    receivePlayRequest.getChildren().remove(mapHboxInvite.get(namePlayer));
                                    System.out.println("remove: " + namePlayer);
                                }

//                                if(mapstatusInvite.containsKey(namePlayer)) {
//                                    Pair<Label, Circle> pair = mapstatusInvite.get(namePlayer);
//                                    pair.getKey().setText("Cannot join this queue");
//                                    pair.getKey().setStyle("-fx-text-fill:#827ce8");
//                                    pair.getValue().setStyle("-fx-stroke: #584ee6");
//
//                                    HBox itemUserRequest = mapHboxInvite.get(namePlayer);
//                                    Button btnAccept = (Button) itemUserRequest.lookup("#btnAccept");
//                                    btnAccept.setVisible(false);
//                                }
                            }
                            else {
                                circleStatus.setStyle("-fx-stroke: rgba(101,104,100,0.76)");
                                lblStatus.setStyle("-fx-text-fill: #9E9E9E; -fx-font-weight: 700; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.8), 2, 0, 0, 1);");
                                btnInvite.setVisible(false);
                                if(mapHboxInvite.containsKey(namePlayer)) {
                                    receivePlayRequest.getChildren().remove(mapHboxInvite.get(namePlayer));
                                    System.out.println("remove: " + namePlayer);
                                }
                            }

                            screnListUserWaiting.getChildren().add(itemUser);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    break;
                case ObjectWrapper.RECEIVE_PLAY_REQUEST:
                    String username = (String) data.getData();

                    VBox receivePlayRequest = (VBox) scene.lookup("#RECEIVE_PLAY_REQUEST");

                    boolean isExist = false;

                    ArrayList<HBox> listUserItemReceive = new ArrayList<>();
                    for (int i = 0; i < receivePlayRequest.getChildren().size(); i++) {
                        HBox itemUser = (HBox) receivePlayRequest.getChildren().get(i);
                        AnchorPane anchorPane = (AnchorPane) itemUser.getChildren().getFirst();
                        Label lblUserName = (Label) anchorPane.lookup("#userNameInvite");
                        if(lblUserName.getText().equals(username)) {
                            isExist = true;
                            break;
                        }
                    }

                    if (isExist) {
                        break;
                    }

                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/Fxml/Client/ItemHome.fxml"));
                    try {
                        HBox itemUser = loader.load();
                        Label lblUserNameInvite = (Label) itemUser.lookup("#userNameInvite");
                        lblUserNameInvite.setText(username);
                        receivePlayRequest.getChildren().add(itemUser);

                        // Xu ly su kien click vao button Accept: chap nhan loi moi choi
                        Button btnAccept = (Button) itemUser.lookup("#btnAccept");
                        btnAccept.setOnAction(event -> {
                            mySocket.sendData(new ObjectWrapper(ObjectWrapper.ACCEPTED_PLAY_REQUEST));
                            receivePlayRequest.getChildren().remove(itemUser);
                        });

                        // Xu ly su kien click vao button Reject: tu choi loi moi choi
                        Button btnReject = (Button) itemUser.lookup("#btnReject");
                        btnReject.setOnAction(event -> {
                            mySocket.sendData(new ObjectWrapper(ObjectWrapper.REJECTED_PLAY_REQUEST));
                            receivePlayRequest.getChildren().remove(itemUser);
                        });

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    break;

                case ObjectWrapper.SERVER_SEND_HISTORY:
                    PlayerHistory playerHistory = (PlayerHistory) data.getData();
                    List<Match> listMatch = playerHistory.getListMatch();

                    VBox vboxHistory = (VBox) scene.lookup("#VboxHistory");
                    vboxHistory.getChildren().clear();

                    // Tạo formatter theo định dạng dd/MM/yyyy HH:mm:ss
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

                    listMatch.sort((o1, o2) -> o2.getTimestamp().compareTo(o1.getTimestamp()));

                    int index = 1;
                    for(Match match : listMatch) {
                        FXMLLoader itemHistoryLoad = new FXMLLoader(getClass().getResource("/Fxml/Client/ItemHistory.fxml"));
                        try {
                            VBox vBox = itemHistoryLoad.load();
                            HBox itemHistory = (HBox) vBox.getChildren().getFirst();
                            Label lblSTT = (Label) itemHistory.getChildren().getFirst();
                            Label lblOpponent = (Label) itemHistory.getChildren().get(1);
                            Label lblTime = (Label) itemHistory.getChildren().get(2);
                            Label lblResult = (Label) itemHistory.getChildren().get(3);
                            Label lblPointChange = (Label) itemHistory.getChildren().get(4);

                            lblSTT.setText(String.valueOf(index));
                            lblOpponent.setText(match.getUser2Username());
                            lblTime.setText(String.valueOf(match.getTimestamp().format(formatter)));
                            lblResult.setText(match.getResultUser1());
                            lblPointChange.setText(String.valueOf(match.getPointsChangeUser1()));

                            if(match.getResultUser1().equals("win")) {
                                lblResult.setStyle("-fx-text-fill: #6ee494");
                            } else if(match.getResultUser1().equals("afk")) {
                                lblResult.setStyle("-fx-text-fill: rgb(251, 51, 51)");
                            }

                            index++;
                            vboxHistory.getChildren().add(itemHistory);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    break;


                case ObjectWrapper.SERVER_SEND_RANKING:
                    List<PlayerHistory> leaderboard = (List<PlayerHistory>) data.getData();

                    HashMap<String, String> mapRanking = new HashMap<>();
                    HashMap<String, PlayerHistory> mapPlayerRanking = new HashMap<>();

                    ArrayList<PlayerHistory> listPlayerRankINTERN = new ArrayList<>();
                    ArrayList<PlayerHistory> listPlayerRankMASTER = new ArrayList<>();
                    ArrayList<PlayerHistory> listPlayerRankGRANDMASTER = new ArrayList<>();
                    ArrayList<PlayerHistory> listPlayerRankCHALLENGER = new ArrayList<>();

                    for(PlayerHistory playerRank : leaderboard) {
                        mapPlayerRanking.put(playerRank.getUsername(), playerRank);
                        String rank = "";
                        int point = playerRank.getPoints();
                        if (point < 20) {
                            rank = "INTERN";
                            listPlayerRankINTERN.add(playerRank);
                        } else if (point < 40) {
                            rank = "MASTER";
                            listPlayerRankMASTER.add(playerRank);
                        } else if (point < 60) {
                            rank = "GRANDMASTER";
                            listPlayerRankGRANDMASTER.add(playerRank);
                        } else {
                            rank = "CHALLENGER";
                            listPlayerRankCHALLENGER.add(playerRank);
                        }
                        mapRanking.put(playerRank.getUsername(), rank);
                    }

                    ChoiceBox choiceBox = (ChoiceBox) scene.lookup("#choiceBoxRank");
                    if (choiceBox.getItems().isEmpty()) {
                        choiceBox.getItems().addAll("INTERN", "MASTER", "GRANDMASTER", "CHALLENGER");
                    }

                    if(mapRanking.get(mySocket.getUsername()) == null) {
                        System.out.println("mapRanking.get(mySocket.getUsername()) is null");
                    }
                    choiceBox.setValue(mapRanking.get(mySocket.getUsername()));

                    VBox vboxRanked = (VBox) scene.lookup("#vboxRanked");
                    ImageView imgRank = (ImageView) scene.lookup("#flagRank");

                    switch (mapRanking.get(mySocket.getUsername())) {
                        case "INTERN":
                            setUIrank(vboxRanked, listPlayerRankINTERN, "INTERN", imgRank);
                            break;
                        case "MASTER":
                            setUIrank(vboxRanked, listPlayerRankMASTER, "MASTER", imgRank);
                            break;
                        case "GRANDMASTER":
                            setUIrank(vboxRanked, listPlayerRankGRANDMASTER, "GRANDMASTER", imgRank);
                            break;
                        case "CHALLENGER":
                            setUIrank(vboxRanked, listPlayerRankCHALLENGER, "CHALLENGER", imgRank);
                            break;
                    }

                    choiceBox.setOnAction(event -> {
                        String rank = (String) choiceBox.getValue();
                        vboxRanked.getChildren().clear();
                        switch (rank) {
                            case "INTERN":
                                setUIrank(vboxRanked, listPlayerRankINTERN, rank, imgRank);
                                break;
                            case "MASTER":
                                setUIrank(vboxRanked, listPlayerRankMASTER, rank, imgRank);
                                break;
                            case "GRANDMASTER":
                                setUIrank(vboxRanked, listPlayerRankGRANDMASTER, rank, imgRank);
                                break;
                            case "CHALLENGER":
                                setUIrank(vboxRanked, listPlayerRankCHALLENGER, rank, imgRank);
                                break;
                        }
                    });

                    break;

                case ObjectWrapper.SERVER_SET_GAME_READY:
                    boolean isInviter = (Boolean) data.getData();
                    if (mySocket.getWaitingFrm() == null) {
                        WaitingFrm waitingFrm = new WaitingFrm(isInviter);
                        mySocket.setWaitingFrm(waitingFrm);
                    } else {
                        mySocket.getWaitingFrm().setIsInviter(isInviter);
                    }
                    mySocket.getWaitingFrm().openScene();
                    break;

            }
        });

    }


    public void setUIrank(VBox vboxRanked, ArrayList<PlayerHistory> listPlayer, String rank, ImageView imgRank) {
        Platform.runLater(() -> {

            vboxRanked.getChildren().clear();
            switch (rank) {
                case "INTERN":
                    String path = getClass().getResource("/Images/Intern.png").toExternalForm();
                    imgRank.setImage(new Image(path));
                    break;
                case "MASTER":
                    String path1 = getClass().getResource("/Images/Master.png").toExternalForm();
                    imgRank.setImage(new Image(path1));
                    break;
                case "GRANDMASTER":
                    String path2 = getClass().getResource("/Images/Grandmaster.png").toExternalForm();
                    imgRank.setImage(new Image(path2));
                    break;
                case "CHALLENGER":
                    String path3 = getClass().getResource("/Images/Challenger.png").toExternalForm();
                    imgRank.setImage(new Image(path3));
                    break;
            }

            int i = 1;
            for(PlayerHistory playerRank : listPlayer) {
                FXMLLoader itemRankedLoad = new FXMLLoader(getClass().getResource("/Fxml/Client/ItemRank.fxml"));
                try {
                    HBox itemRanked = itemRankedLoad.load();
                    Label lblStt = (Label) itemRanked.getChildren().getFirst();
                    Label lblPlayers = (Label) itemRanked.getChildren().get(1);
                    Label lblWins = (Label) itemRanked.getChildren().get(2);
                    Label lblLosses = (Label) itemRanked.getChildren().get(3);
                    Label lblDraws = (Label) itemRanked.getChildren().get(4);
                    Label lblAFKs = (Label) itemRanked.getChildren().get(5);
                    Label lplPoints = (Label) itemRanked.getChildren().get(6);

                    lblStt.setText(String.valueOf(i));
                    lblPlayers.setText(playerRank.getUsername());
                    lblWins.setText(String.valueOf(playerRank.getTotalWins()));
                    lblLosses.setText(String.valueOf(playerRank.getTotalLosses()));
                    lblDraws.setText(String.valueOf(playerRank.getTotalDraw()));
                    lblAFKs.setText(String.valueOf(playerRank.getTotalAfk()));
                    lplPoints.setText(String.valueOf(playerRank.getPoints()));
                    i++;

                    if(playerRank.getUsername().equals(mySocket.getUsername())) {
                        itemRanked.setStyle("-fx-background-color: rgba(85,103,223,0.4);");
                    }

                    vboxRanked.getChildren().add(itemRanked);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void openSceneWithNotification(String leftPlayerUsername) {
        openScene(); // Mở scene như bình thường
        
        // Hiển thị thông báo sau khi scene đã load
        Platform.runLater(() -> {
            try {
                // Đợi một chút để scene load xong
                Timeline delay = new Timeline(new KeyFrame(Duration.millis(300), e -> {
                    showNotification("Opponent " + leftPlayerUsername + " has left the game");
                }));
                delay.play();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }
    
    private void showNotification(String message) {
        Platform.runLater(() -> {
            try {
                // Tạo notification popup
                Stage notificationStage = new Stage();
                notificationStage.initStyle(StageStyle.UNDECORATED);
                notificationStage.initModality(Modality.NONE);
                
                VBox content = new VBox(10);
                content.setStyle("-fx-background-color: rgba(244, 67, 54, 0.95); -fx-background-radius: 10px; -fx-padding: 15px 20px; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 10, 0, 0, 3);");
                
                Label messageLabel = new Label(message);
                messageLabel.setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold; -fx-wrap-text: true;");
                messageLabel.setMaxWidth(300);
                
                content.getChildren().add(messageLabel);
                
                javafx.scene.Scene notificationScene = new javafx.scene.Scene(content);
                notificationScene.setFill(Color.TRANSPARENT);
                notificationStage.setScene(notificationScene);
                
                // Đặt vị trí ở góc trên bên phải
                notificationStage.setX(stage.getX() + stage.getWidth() - 350);
                notificationStage.setY(stage.getY() + 50);
                
                notificationStage.show();
                
                // Fade out và đóng sau 3 giây
                FadeTransition fadeOut = new FadeTransition(Duration.seconds(0.5), content);
                fadeOut.setFromValue(1.0);
                fadeOut.setToValue(0.0);
                
                Timeline closeTimer = new Timeline(new KeyFrame(Duration.seconds(3), e -> {
                    fadeOut.play();
                    fadeOut.setOnFinished(event -> notificationStage.close());
                }));
                closeTimer.play();
                
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }


}
