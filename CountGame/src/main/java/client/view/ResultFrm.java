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
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import shared.dto.ObjectWrapper;

import java.io.IOException;


public class ResultFrm  {

    private ClientCtr mySocket = ClientCtr.getInstance();
    private Stage stage = mySocket.getStage();
    private boolean waitingForPlayAgainResponse = false;
    private Dialog<ButtonType> currentPlayAgainDialog = null; // Reference to current dialog để đóng khi cần
    private Stage currentNotificationStage = null; // Reference to current notification để đóng khi cần
    private boolean hasDeclinedPlayAgain = false; // Flag to track if this client declined

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
                    // Reset declined flag when server asks again
                    hasDeclinedPlayAgain = false;
                    Platform.runLater(() -> {
                        showPlayAgainDialog();
                    });
                    break;
                    
                case ObjectWrapper.SERVER_START_NEW_GAME:
                    // Both players agreed, start new game
                    // Close dialog and all notifications immediately
                    Platform.runLater(() -> {
                        if (currentPlayAgainDialog != null) {
                            try {
                                currentPlayAgainDialog.close();
                            } catch (Exception e) {
                                // Ignore
                            }
                            currentPlayAgainDialog = null;
                        }
                        if (currentNotificationStage != null) {
                            try {
                                currentNotificationStage.close();
                            } catch (Exception e) {
                                // Ignore
                            }
                            currentNotificationStage = null;
                        }
                        waitingForPlayAgainResponse = false;
                    });
                    // The game will start automatically from server when SERVER_SEND_ROUND_DATA is received
                    break;
                    
                case ObjectWrapper.SERVER_OPPONENT_WAITING_RESPONSE:
                    // Opponent is waiting for your response - show notification
                    Platform.runLater(() -> {
                        // Close any existing notification first
                        if (currentNotificationStage != null) {
                            try {
                                currentNotificationStage.close();
                            } catch (Exception e) {
                                // Ignore
                            }
                            currentNotificationStage = null;
                        }
                        showNotification("Opponent is waiting for your response...");
                        // Dialog remains open so user can still choose
                    });
                    break;
                    
                case ObjectWrapper.SERVER_OPPONENT_DECLINED_PLAY_AGAIN:
                    // Opponent declined - only handle if this client hasn't declined
                    // If this client already declined, ignore this message (don't show notification)
                    if (hasDeclinedPlayAgain) {
                        System.out.println("Client: Received SERVER_OPPONENT_DECLINED_PLAY_AGAIN but this client already declined, ignoring");
                        break;
                    }
                    // Opponent declined - close dialog immediately and show notification
                    Platform.runLater(() -> {
                        if (currentPlayAgainDialog != null) {
                            try {
                                currentPlayAgainDialog.close();
                            } catch (Exception e) {
                                // Ignore
                            }
                            currentPlayAgainDialog = null;
                        }
                        waitingForPlayAgainResponse = false;
                        // Close any existing notification first
                        if (currentNotificationStage != null) {
                            try {
                                currentNotificationStage.close();
                            } catch (Exception e) {
                                // Ignore
                            }
                            currentNotificationStage = null;
                        }
                        showNotification("Opponent has declined to play again");
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
                            else if(result.equals("draw")) {
                                // Hòa: cộng 0.5 điểm (điểm sẽ được cập nhật từ server)
                                // Không cập nhật điểm ở client vì điểm được lưu dưới dạng int
                                // Điểm sẽ được cập nhật khi client refresh hoặc đăng nhập lại
                                lblPointChange.setText("+0.5 POINT");
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
            currentPlayAgainDialog = new Dialog<>();
            currentPlayAgainDialog.initOwner(stage); // Căn giữa theo stage
            currentPlayAgainDialog.initModality(Modality.APPLICATION_MODAL);
            currentPlayAgainDialog.initStyle(StageStyle.TRANSPARENT);
            currentPlayAgainDialog.setTitle("Play Again");
            
            Dialog<ButtonType> playAgainDialog = currentPlayAgainDialog; // Local reference
            
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
                    hasDeclinedPlayAgain = false; // Reset flag
                    mySocket.sendData(new ObjectWrapper(ObjectWrapper.CLIENT_PLAY_AGAIN_RESPONSE, true));
                } else {
                    // User declined (No hoặc click X) - close dialog immediately and send response
                    hasDeclinedPlayAgain = true; // Mark that this client declined
                    mySocket.sendData(new ObjectWrapper(ObjectWrapper.CLIENT_PLAY_AGAIN_RESPONSE, false));
                    // Close dialog immediately (already closing from showAndWait, but ensure it's closed)
                    if (currentPlayAgainDialog != null) {
                        try {
                            currentPlayAgainDialog.close();
                        } catch (Exception e) {
                            // Ignore
                        }
                        currentPlayAgainDialog = null;
                    }
                    // No notification for the person who declined - they won't process SERVER_OPPONENT_DECLINED_PLAY_AGAIN
                }
            });
        });
    }
    
    private void showNotification(String message) {
        Platform.runLater(() -> {
            try {
                // Tạo notification popup
                Stage notificationStage = new Stage();
                currentNotificationStage = notificationStage; // Lưu reference để đóng sau
                notificationStage.initStyle(StageStyle.UNDECORATED);
                notificationStage.initModality(Modality.NONE);
                notificationStage.initOwner(stage); // Căn giữa theo stage
                
                VBox content = new VBox(10);
                content.setStyle("-fx-background-color: rgba(33, 150, 243, 0.95); -fx-background-radius: 10px; -fx-padding: 15px 20px; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 10, 0, 0, 3); -fx-background-insets: 0;");
                
                // Tạo HBox chứa nút X ở góc trên bên phải
                HBox headerBox = new HBox();
                headerBox.setAlignment(javafx.geometry.Pos.TOP_RIGHT);
                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);
                
                // Nút X để đóng notification
                Button closeButton = new Button("✕");
                closeButton.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-font-size: 18px; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 0px 5px;");
                closeButton.setOnAction(e -> {
                    notificationStage.close();
                    if (currentNotificationStage == notificationStage) {
                        currentNotificationStage = null;
                    }
                });
                
                headerBox.getChildren().addAll(spacer, closeButton);
                
                Label messageLabel = new Label(message);
                messageLabel.setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold; -fx-wrap-text: true; -fx-background-color: transparent;");
                messageLabel.setMaxWidth(300);
                
                VBox contentBox = new VBox(5);
                contentBox.getChildren().addAll(headerBox, messageLabel);
                
                content.getChildren().add(contentBox);
                
                javafx.scene.Scene notificationScene = new javafx.scene.Scene(content);
                notificationScene.setFill(Color.TRANSPARENT);
                notificationStage.setScene(notificationScene);
                
                // Clip VBox để bo góc tròn
                Rectangle clip = new Rectangle();
                clip.setArcWidth(20);
                clip.setArcHeight(20);
                content.layoutBoundsProperty().addListener((observable, oldValue, newValue) -> {
                    if (newValue.getWidth() > 0 && newValue.getHeight() > 0) {
                        clip.setWidth(newValue.getWidth());
                        clip.setHeight(newValue.getHeight());
                    }
                });
                Platform.runLater(() -> {
                    double width = content.getBoundsInLocal().getWidth();
                    double height = content.getBoundsInLocal().getHeight();
                    if (width > 0 && height > 0) {
                        clip.setWidth(width);
                        clip.setHeight(height);
                    } else {
                        clip.setWidth(notificationScene.getWidth());
                        clip.setHeight(notificationScene.getHeight());
                    }
                    content.setClip(clip);
                });
                
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
                    fadeOut.setOnFinished(event -> {
                        notificationStage.close();
                        if (currentNotificationStage == notificationStage) {
                            currentNotificationStage = null;
                        }
                    });
                }));
                closeTimer.play();
                
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
    
}
