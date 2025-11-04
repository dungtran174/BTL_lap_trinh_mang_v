package client.view;

import client.controller.ClientCtr;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import shared.dto.ObjectWrapper;

import java.io.IOException;


public class ResultFrm  {

    private ClientCtr mySocket = ClientCtr.getInstance();
    private Stage stage = mySocket.getStage();
    private boolean waitingForPlayAgainResponse = false;

    public ResultFrm() {
    }

    public void openScene() {
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
                case ObjectWrapper.SERVER_ASK_PLAY_AGAIN:
                    // Server asking if want to play again - show dialog automatically
                    Platform.runLater(() -> {
                        showPlayAgainDialog();
                    });
                    break;
                    
                case ObjectWrapper.SERVER_START_NEW_GAME:
                    // Both players agreed, start new game
                    // The game will start automatically from server when SERVER_SEND_ROUND_DATA is received
                    break;
                    
                case ObjectWrapper.SERVER_PLAY_AGAIN_DECLINED:
                    // One player declined - stay on result screen
                    Platform.runLater(() -> {
                        waitingForPlayAgainResponse = false;
                        // Stay on result screen, no notification needed
                    });
                    break;
                    
                case ObjectWrapper.SERVER_SEND_RESULT:

                    String[] resultAndUserNameEnemy = ((String) data.getData()).split("\\|\\|");

                    String result = resultAndUserNameEnemy[0];
                    String usernameEnemy = resultAndUserNameEnemy[1];

                    if (result.equals("loss")) {
                        FXMLLoader loss = new FXMLLoader(getClass().getResource("/Fxml/Client/Lose.fxml"));
                        try {
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
                                // Quay về trang home/main
                                mySocket.sendData(new ObjectWrapper(ObjectWrapper.BACK_TO_MAIN_FORM));
                                mySocket.getMainFrm().openScene();
                            });
                            stage.setScene(lossScene);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    } else {
                        FXMLLoader win = new FXMLLoader(getClass().getResource("/Fxml/Client/Win.fxml"));
                        try {
                            Scene winScene  = new Scene(win.load());
                            mySocket.setResultScene(winScene);
                            stage.setScene(winScene);
                            stage.setTitle("Result");
                            stage.show();

                            //set point and flag
                            ImageView flagWin = (ImageView) win.getNamespace().get("flagRankResultWin");
                            Label lblPointWin = (Label) win.getNamespace().get("lblPointWin");
                            Label lblPointChange = (Label) win.getNamespace().get("lblPointChange");
                            int point = mySocket.getPoints();

                            // Kiểm tra nếu đây là trường hợp đối thủ rời trận (vẫn cộng 1 điểm)
                            if(result.equals("win")) {
                                // Thắng (bình thường hoặc do đối thủ rời trận): cộng 1 điểm
                                point = point + 1;
                                mySocket.setPoints(point);
                                lblPointChange.setText("+1 POINT");
                            }
                            else {
                                // Không thắng: không cộng điểm
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
                                // Quay về trang home/main
                                mySocket.sendData(new ObjectWrapper(ObjectWrapper.BACK_TO_MAIN_FORM));
                                mySocket.getMainFrm().openScene();
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
        // Sound removed
    }
    
    private void showPlayAgainDialog() {
        if (waitingForPlayAgainResponse) {
            return; // Prevent multiple dialogs
        }
        
        waitingForPlayAgainResponse = true;
        
        Platform.runLater(() -> {
            // Create custom dialog
            Dialog<ButtonType> playAgainDialog = new Dialog<>();
            playAgainDialog.initOwner(stage); // Căn giữa theo stage
            playAgainDialog.initModality(Modality.APPLICATION_MODAL);
            playAgainDialog.initStyle(StageStyle.TRANSPARENT);
            playAgainDialog.setTitle("Play Again");
            
            // Tạo container với nút X
            StackPane container = new StackPane();
            container.setStyle("-fx-background-color: transparent;");
            
            // Content
            VBox content = new VBox(15);
            content.setStyle("-fx-background-color: white; -fx-background-radius: 20px; -fx-padding: 30px; -fx-alignment: center; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 15, 0, 0, 5);");
            
            // Tạo HBox chứa nút X ở góc trên bên phải
            HBox headerBox = new HBox();
            headerBox.setAlignment(javafx.geometry.Pos.TOP_RIGHT);
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            
            // Buttons trước để có thể dùng trong closeButton
            ButtonType yesButtonType = new ButtonType("Yes", javafx.scene.control.ButtonBar.ButtonData.OK_DONE);
            ButtonType noButtonType = new ButtonType("No", javafx.scene.control.ButtonBar.ButtonData.CANCEL_CLOSE);
            
            // Nút X để đóng dialog (coi như No)
            Button closeButton = new Button("✕");
            closeButton.setStyle("-fx-background-color: transparent; -fx-text-fill: #999; -fx-font-size: 20px; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 5px 10px;");
            closeButton.setOnAction(e -> {
                playAgainDialog.setResult(noButtonType);
                playAgainDialog.close();
            });
            
            headerBox.getChildren().addAll(spacer, closeButton);
            
            Label titleLabel = new Label("Play Again?");
            titleLabel.setStyle("-fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: #4CAF50; -fx-font-family: 'Be Vietnam Pro';");
            
            Label messageLabel = new Label("Do you want to play another round with this opponent?");
            messageLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #333; -fx-wrap-text: true; -fx-text-alignment: center; -fx-font-family: 'Be Vietnam Pro';");
            messageLabel.setMaxWidth(400);
            
            VBox contentBox = new VBox(10);
            contentBox.setAlignment(javafx.geometry.Pos.CENTER);
            contentBox.getChildren().addAll(headerBox, titleLabel, messageLabel);
            
            content.getChildren().add(contentBox);
            container.getChildren().add(content);
            
            playAgainDialog.getDialogPane().setContent(container);
            playAgainDialog.getDialogPane().setStyle("-fx-background-color: transparent; -fx-background-radius: 20px;");
            
            playAgainDialog.getDialogPane().getButtonTypes().addAll(yesButtonType, noButtonType);
            
            // Style buttons
            Button yesButton = (Button) playAgainDialog.getDialogPane().lookupButton(yesButtonType);
            yesButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 10px; -fx-padding: 10px 25px; -fx-cursor: hand; -fx-font-family: 'Be Vietnam Pro';");
            
            Button noButton = (Button) playAgainDialog.getDialogPane().lookupButton(noButtonType);
            noButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 10px; -fx-padding: 10px 25px; -fx-cursor: hand; -fx-font-family: 'Be Vietnam Pro';");
            
            // Handle result - No timeout, wait indefinitely for user response
            playAgainDialog.showAndWait().ifPresent(response -> {
                waitingForPlayAgainResponse = false;
                if (response == yesButtonType) {
                    // User wants to play again
                    mySocket.sendData(new ObjectWrapper(ObjectWrapper.CLIENT_PLAY_AGAIN_RESPONSE, true));
                } else {
                    // User declined (No hoặc click X) - stay on result screen
                    mySocket.sendData(new ObjectWrapper(ObjectWrapper.CLIENT_PLAY_AGAIN_RESPONSE, false));
                }
            });
        });
    }
    
}
