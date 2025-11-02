package client.view;

import client.controller.ClientCtr;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Stage;
import shared.dto.ObjectWrapper;

import java.io.File;
import java.io.IOException;


public class ResultFrm  {

    private ClientCtr mySocket = ClientCtr.getInstance();
    private Stage stage = mySocket.getStage();

    public ResultFrm() {
    }

    public void openScene() {
        // Khởi tạo âm thanh trước
//        initializeBackgroundMusic();

        // Sau đó xử lý UI trong Platform.runLater
        Platform.runLater(() -> {
            try {
                mySocket.sendData(new ObjectWrapper(ObjectWrapper.GET_RESULT));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public void receivedDataProcessing(ObjectWrapper data) {
        Platform.runLater(() -> {
            Scene scene = mySocket.getMainScene();

            switch (data.getPerformative()) {
                case ObjectWrapper.SERVER_SEND_RESULT:

                    String[] resultAndUserNameEnemy = ((String) data.getData()).split("\\|\\|");

                    String result = resultAndUserNameEnemy[0];
                    String usernameEnemy = resultAndUserNameEnemy[1];

                    if (result.equals("loss")) {
                        FXMLLoader loss = new FXMLLoader(getClass().getResource("/Fxml/Client/Lose.fxml"));
                        try {
                            String clickButtonFile = new File("src/main/resources/Sounds/lose.mp3").toURI().toString();
                            Media clickButton = new Media(clickButtonFile);
                            MediaPlayer clickButtonPlayer = new MediaPlayer(clickButton);
                            clickButtonPlayer.setVolume(0.8);
                            clickButtonPlayer.play();

                            Scene lossScene = new Scene(loss.load());
                            mySocket.setResultScene(lossScene);
                            stage.setScene(lossScene);
                            stage.setTitle("Result");
                            stage.show();

                            //set point and flag
                            ImageView flagLose = (ImageView) loss.getNamespace().get("flagRankResultLose");
                            Label lblPointLose = (Label) loss.getNamespace().get("lblPointLose");;
                            int point = mySocket.getPoints();
                            lblPointLose.setText(String.valueOf(point));

                            if (point < 20) flagLose.setImage(new Image(getClass().getResource("/Images/flagIntern.png").toExternalForm()));
                            else if (point < 40) flagLose.setImage(new Image(getClass().getResource("/Images/flagMaster.png").toExternalForm()));
                            else if (point < 60) flagLose.setImage(new Image(getClass().getResource("/Images/flagGrandmaster.png").toExternalForm()));
                            else flagLose.setImage(new Image(getClass().getResource("/Images/flagChallenger.png").toExternalForm()));

                            Button btnLoseGo = (Button) loss.getNamespace().get("btnPlayAgainLose");
                            btnLoseGo.setOnAction(e -> {
                                clicBackMain();
                                mySocket.sendData(new ObjectWrapper(ObjectWrapper.BACK_TO_MAIN_FORM));
                                mySocket.getMainFrm().openScene();
                                clickButtonPlayer.stop();
                            });
                            stage.setScene(lossScene);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    } else {
                        FXMLLoader win = new FXMLLoader(getClass().getResource("/Fxml/Client/Win.fxml"));
                        try {
                            String clickButtonFile = new File("src/main/resources/Sounds/win.mp3").toURI().toString();
                            Media clickButton = new Media(clickButtonFile);
                            MediaPlayer clickButtonPlayer = new MediaPlayer(clickButton);
                            clickButtonPlayer.setVolume(0.8);
                            clickButtonPlayer.play();

                            Scene winScene  = new Scene(win.load());
                            mySocket.setResultScene(winScene);
                            stage.setScene(winScene);
                            stage.setTitle("Result");
                            stage.show();

                            //set point and flag
                            ImageView flagWin = (ImageView) win.getNamespace().get("flagRankResultWin");
                            Label lblPointWin = (Label) win.getNamespace().get("lblPointWin");
                            int point = mySocket.getPoints();


                            if(result.equals("win")) {
                                point = point + 1;
                                mySocket.setPoints(point);
                            }
                            else {
                                Label lblPointChange = (Label) win.getNamespace().get("lblPointChange");
                                lblPointChange.setText("+0 POINT");
                            }
                            lblPointWin.setText(String.valueOf(point));

                            if (point < 20) flagWin.setImage(new Image(getClass().getResource("/Images/flagIntern.png").toExternalForm()));
                            else if (point < 40) flagWin.setImage(new Image(getClass().getResource("/Images/flagMaster.png").toExternalForm()));
                            else if (point < 60) flagWin.setImage(new Image(getClass().getResource("/Images/flagGrandmaster.png").toExternalForm()));
                            else flagWin.setImage(new Image(getClass().getResource("/Images/flagChallenger.png").toExternalForm()));

                            Button btnWinGo = (Button) win.getNamespace().get("btnPlayAgainWin");
                            btnWinGo.setOnAction(e -> {
                                clicBackMain();
                                mySocket.sendData(new ObjectWrapper(ObjectWrapper.BACK_TO_MAIN_FORM));
                                mySocket.getMainFrm().openScene();
                                clickButtonPlayer.stop();
                            });
                            stage.setScene(winScene);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
            }
        });

    }


    public void clicBackMain () {
        String clickButtonFile = new File("src/main/resources/Sounds/buttonBackMain.mp3").toURI().toString();
        Media clickButton = new Media(clickButtonFile);
        MediaPlayer clickButtonPlayer = new MediaPlayer(clickButton);
        clickButtonPlayer.setVolume(0.8);
        clickButtonPlayer.play();
    }
}
