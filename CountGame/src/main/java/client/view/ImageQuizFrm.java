package client.view;

import client.controller.ClientCtr;
import javafx.application.Platform;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
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
import javafx.util.Duration;
import shared.dto.ObjectWrapper;

import java.io.ByteArrayInputStream;

public class ImageQuizFrm {
    
    private ClientCtr mySocket = ClientCtr.getInstance();
    private Stage stage = mySocket.getStage();
    private Timeline countdownTimeline;
    private boolean answerSubmitted = false;
    private double currentMyScore = 0.0;
    private double currentEnemyScore = 0.0;
    private boolean isPlayer1 = false; // Track if current player is player1
    
    public void openScene() {
        Platform.runLater(() -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/Fxml/Client/ImageQuiz.fxml"));
                Scene scene = new Scene(loader.load());
                mySocket.setImageQuizScene(scene);
                mySocket.setImageQuizFrm(this);
                
                stage.setScene(scene);
                stage.setTitle("Image Quiz Game");
                stage.show();
                
                // Set button action handlers
                Button btnSubmit = (Button) scene.lookup("#btnSubmit");
                if (btnSubmit != null) {
                    btnSubmit.setOnAction(event -> submitAnswer());
                }
                
                Button btnLeave = (Button) scene.lookup("#btnLeave");
                if (btnLeave != null) {
                    btnLeave.setOnAction(event -> handleLeaveGame());
                }
                
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
    
    private void handleLeaveGame() {
        Platform.runLater(() -> {
            // Tạo custom dialog đẹp hơn với bo góc
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("Xác nhận rời trận");
            dialog.initOwner(stage); // Căn giữa theo stage
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.initStyle(StageStyle.TRANSPARENT);
            
            // Tạo container với nút X
            StackPane container = new StackPane();
            container.setStyle("-fx-background-color: transparent;");
            
            // Tạo nội dung dialog với bo góc đẹp
            VBox content = new VBox(20);
            content.setStyle("-fx-padding: 30px; -fx-alignment: center; -fx-background-color: #ffffff; -fx-background-radius: 20px; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 15, 0, 0, 5);");
            
            // Tạo HBox chứa nút X ở góc trên bên phải
            HBox headerBox = new HBox();
            headerBox.setAlignment(javafx.geometry.Pos.TOP_RIGHT);
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            
            // Tạo buttons trước để có thể dùng trong closeButton
            ButtonType confirmButtonType = new ButtonType("Confirm", javafx.scene.control.ButtonBar.ButtonData.OK_DONE);
            ButtonType cancelButtonType = new ButtonType("Cancel", javafx.scene.control.ButtonBar.ButtonData.CANCEL_CLOSE);
            
            // Nút X để đóng dialog (coi như Cancel)
            Button closeButton = new Button("✕");
            closeButton.setStyle("-fx-background-color: transparent; -fx-text-fill: #999; -fx-font-size: 20px; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 5px 10px;");
            closeButton.setOnAction(e -> {
                dialog.setResult(cancelButtonType);
                dialog.close();
            });
            
            headerBox.getChildren().addAll(spacer, closeButton);
            
            // Sử dụng ảnh err.png làm icon
            ImageView iconView = new ImageView();
            try {
                Image errImage = new Image(getClass().getResource("/Images/err.png").toExternalForm());
                iconView.setImage(errImage);
                iconView.setFitHeight(60);
                iconView.setFitWidth(60);
                iconView.setPreserveRatio(true);
            } catch (Exception e) {
                // Fallback nếu không load được ảnh
                e.printStackTrace();
            }
            
            Label titleLabel = new Label("Leave Game");
            titleLabel.setStyle("-fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: #f44336; -fx-font-family: 'Be Vietnam Pro';");

            Label messageLabel = new Label("If you leave, you will lose 1 point.\nAre you sure you want to leave?");
            messageLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #333; -fx-wrap-text: true; -fx-text-alignment: center; -fx-font-family: 'Be Vietnam Pro';");
            messageLabel.setMaxWidth(400);
            
            VBox contentBox = new VBox(10);
            contentBox.setAlignment(javafx.geometry.Pos.CENTER);
            contentBox.getChildren().addAll(headerBox, iconView, titleLabel, messageLabel);
            
            content.getChildren().add(contentBox);
            container.getChildren().add(content);
            
            dialog.getDialogPane().setContent(container);
            dialog.getDialogPane().setStyle("-fx-background-color: transparent; -fx-background-radius: 20px;");
            
            dialog.getDialogPane().getButtonTypes().addAll(confirmButtonType, cancelButtonType);
            
            // Style cho buttons với bo góc
            Button confirmButton = (Button) dialog.getDialogPane().lookupButton(confirmButtonType);
            confirmButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 10px; -fx-padding: 10px 25px; -fx-cursor: hand; -fx-font-family: 'Be Vietnam Pro';");
            
            Button cancelButton = (Button) dialog.getDialogPane().lookupButton(cancelButtonType);
            cancelButton.setStyle("-fx-background-color: #757575; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 10px; -fx-padding: 10px 25px; -fx-cursor: hand; -fx-font-family: 'Be Vietnam Pro';");

            // Xử lý kết quả
            dialog.showAndWait().ifPresent(response -> {
                if (response == confirmButtonType) {
                    // Send leave game request to server
                    mySocket.sendData(new ObjectWrapper(ObjectWrapper.CLIENT_LEAVE_GAME, null));
                    
                    // Quay về màn hình home ngay lập tức
                    if (countdownTimeline != null) {
                        countdownTimeline.stop();
                    }
                    MainFrm mainFrm = new MainFrm();
                    mainFrm.openScene();
                }
                // Nếu response là cancelButtonType (khi click X hoặc Cancel), không làm gì cả
            });
        });
    }
    
    public void displayRound(int roundNumber, byte[] imageBytes, String question) {
        Platform.runLater(() -> {
            try {
                Scene scene = mySocket.getImageQuizScene();
                if (scene == null) {
                    return;
                }
                
                // Reset state
                answerSubmitted = false;
                
                // Display round number
                Label lblRound = (Label) scene.lookup("#lblRound");
                if (lblRound != null) {
                    lblRound.setText("Round " + roundNumber + " / 3");
                }
                
                // Display question and show it
                Label lblQuestion = (Label) scene.lookup("#lblQuestion");
                if (lblQuestion != null) {
                    lblQuestion.setText(question);
                    lblQuestion.setVisible(true);
                }
                
                // Display image
                ImageView imgView = (ImageView) scene.lookup("#imgView");
                if (imgView != null && imageBytes != null) {
                    ByteArrayInputStream bis = new ByteArrayInputStream(imageBytes);
                    Image image = new Image(bis);
                    imgView.setImage(image);
                }
                
                // Clear answer field and show it
                TextField txtAnswer = (TextField) scene.lookup("#txtAnswer");
                if (txtAnswer != null) {
                    txtAnswer.setText("");
                    txtAnswer.setDisable(false);
                    txtAnswer.setVisible(true);
                }
                
                // Enable submit button and show it
                Button btnSubmit = (Button) scene.lookup("#btnSubmit");
                if (btnSubmit != null) {
                    btnSubmit.setDisable(false);
                    btnSubmit.setVisible(true);
                }
                
                // Hide result label
                Label lblResult = (Label) scene.lookup("#lblResult");
                if (lblResult != null) {
                    lblResult.setVisible(false);
                }
                
                // Update score display
                updateScoreDisplay();
                
                // Start countdown (15 seconds)
                startCountdown(15);
                
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
    
    private void startCountdown(int seconds) {
        if (countdownTimeline != null) {
            countdownTimeline.stop();
        }
        
        Label lblTimer = (Label) mySocket.getImageQuizScene().lookup("#lblTimer");
        if (lblTimer == null) {
            return;
        }
        
        // Hiển thị giá trị ban đầu
        lblTimer.setText(String.valueOf(seconds));
        
        countdownTimeline = new Timeline();
        
        // Tạo KeyFrame cho mỗi giây: 0, 1, 2, ..., seconds
        for (int i = 0; i <= seconds; i++) {
            final int displayValue = seconds - i;
            countdownTimeline.getKeyFrames().add(new KeyFrame(Duration.seconds(i), event -> {
                if (lblTimer != null) {
                    lblTimer.setText(String.valueOf(displayValue));
                }
                
                // Khi đếm đến 0, disable input
                if (displayValue == 0) {
                    countdownTimeline.stop();
                    // Time is up, disable input
                    TextField txtAnswer = (TextField) mySocket.getImageQuizScene().lookup("#txtAnswer");
                    Button btnSubmit = (Button) mySocket.getImageQuizScene().lookup("#btnSubmit");
                    if (txtAnswer != null) {
                        txtAnswer.setDisable(true);
                    }
                    if (btnSubmit != null) {
                        btnSubmit.setDisable(true);
                    }
                }
            }));
        }
        
        countdownTimeline.setCycleCount(1); // Chỉ chạy 1 lần
        countdownTimeline.play();
    }
    
    public void submitAnswer() {
        if (answerSubmitted) {
            return;
        }
        
        Platform.runLater(() -> {
            try {
                Scene scene = mySocket.getImageQuizScene();
                if (scene == null) {
                    return;
                }
                
                TextField txtAnswer = (TextField) scene.lookup("#txtAnswer");
                if (txtAnswer != null) {
                    String answerText = txtAnswer.getText().trim();
                    try {
                        int answer = Integer.parseInt(answerText);
                        mySocket.sendData(new ObjectWrapper(ObjectWrapper.SUBMIT_ANSWER, answer));
                        answerSubmitted = true;
                        
                        // Disable input
                        txtAnswer.setDisable(true);
                        Button btnSubmit = (Button) scene.lookup("#btnSubmit");
                        if (btnSubmit != null) {
                            btnSubmit.setDisable(true);
                        }
                        
                        if (countdownTimeline != null) {
                            countdownTimeline.stop();
                        }
                    } catch (NumberFormatException e) {
                        // Invalid input, ignore
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
    
    public void receivedDataProcessing(ObjectWrapper data) {
        Platform.runLater(() -> {
            try {
                Scene scene = mySocket.getImageQuizScene();
                if (scene == null) {
                    return;
                }
                
                switch (data.getPerformative()) {
                    case ObjectWrapper.SERVER_SEND_ROUND_DATA:
                        System.out.println("ImageQuizFrm: Processing SERVER_SEND_ROUND_DATA");
                        Object[] roundData = (Object[]) data.getData();
                        if (roundData != null && roundData.length >= 3) {
                            int roundNumber = (Integer) roundData[0];
                            byte[] imageBytes = (byte[]) roundData[1];
                            String question = (String) roundData[2];
                            System.out.println("ImageQuizFrm: Round " + roundNumber + ", Question: " + question + ", Image bytes: " + (imageBytes != null ? imageBytes.length : "null"));
                            
                            // Check if result label is currently visible - if so, wait 0.5 seconds before showing next round
                            Label lblResult = (Label) scene.lookup("#lblResult");
                            if (lblResult != null && lblResult.isVisible()) {
                                // Result is visible, delay 0.5 seconds before showing next round
                                Timeline delayTimeline = new Timeline(new KeyFrame(Duration.millis(500), e -> {
                                    displayRound(roundNumber, imageBytes, question);
                                }));
                                delayTimeline.play();
                            } else {
                                // No result visible, display round immediately
                                displayRound(roundNumber, imageBytes, question);
                            }
                        } else {
                            System.err.println("ImageQuizFrm: Invalid round data received");
                        }
                        break;
                        
                    case ObjectWrapper.SERVER_DISABLE_INPUT:
                        // Disable input immediately when someone submits answer
                        TextField txtAnswer = (TextField) scene.lookup("#txtAnswer");
                        Button btnSubmit = (Button) scene.lookup("#btnSubmit");
                        if (txtAnswer != null) {
                            txtAnswer.setDisable(true);
                        }
                        if (btnSubmit != null) {
                            btnSubmit.setDisable(true);
                        }
                        // Stop countdown
                        if (countdownTimeline != null) {
                            countdownTimeline.stop();
                        }
                        break;
                        
                    case ObjectWrapper.SERVER_SEND_ROUND_RESULT:
                        Object[] resultData = (Object[]) data.getData();
                        displayRoundResult(resultData);
                        break;
                        
                    case ObjectWrapper.SERVER_END_IMAGE_QUIZ_GAME:
                        Object[] endData = (Object[]) data.getData();
                        double finalPlayer1Score = (Double) endData[0];
                        double finalPlayer2Score = (Double) endData[1];
                        String winner = (String) endData[2];
                        String loser = (String) endData[3];
                        
                        // Stop countdown
                        if (countdownTimeline != null) {
                            countdownTimeline.stop();
                        }
                        
                        // Determine result for current player
                        String myResult;
                        if (winner.equals(mySocket.getUsername())) {
                            myResult = "win";
                        } else if (loser.equals(mySocket.getUsername())) {
                            myResult = "loss";
                        } else {
                            myResult = "draw";
                        }
                        
                        // Delay 0.5 giây để người chơi có thời gian xem đáp án ở round cuối
                        // trước khi chuyển sang result screen
                        Timeline delayTimeline = new Timeline(new KeyFrame(Duration.seconds(0.5), e -> {
                            // Go to result screen
                            ResultFrm resultFrm = new ResultFrm();
                            mySocket.setResultFrm(resultFrm);
                            resultFrm.openScene();
                        }));
                        delayTimeline.setCycleCount(1);
                        delayTimeline.play();
                        break;
                        
                    case ObjectWrapper.SERVER_PLAYER_LEFT_GAME:
                        // Người rời trận: return to main screen (không có thông báo)
                        if (countdownTimeline != null) {
                            countdownTimeline.stop();
                        }
                        // Clear ImageQuizFrm reference để có thể tạo mới khi chơi ván sau
                        mySocket.setImageQuizFrm(null);
                        mySocket.setImageQuizScene(null);
                        MainFrm mainFrm = new MainFrm();
                        mainFrm.openScene();
                        break;
                        
                    case ObjectWrapper.SERVER_OPPONENT_LEFT_SHOW_RESULT:
                        // Đối thủ rời trận: hiển thị dialog và chuyển về Result
                        if (countdownTimeline != null) {
                            countdownTimeline.stop();
                        }
                        
                        // Clear ImageQuizFrm reference để có thể tạo mới khi chơi ván sau
                        mySocket.setImageQuizFrm(null);
                        mySocket.setImageQuizScene(null);
                        
                        // Get result data: "win||" + username của người rời trận
                        String opponentLeftData = (String) data.getData();
                        String[] resultAndLeftUsername = opponentLeftData.split("\\|\\|");
                        String result = resultAndLeftUsername[0]; // "win"
                        String leftPlayerUsername = resultAndLeftUsername[1];
                        
                        // Hiển thị dialog thông báo
                        showOpponentLeftDialog(leftPlayerUsername, result);
                        break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
    
    private void displayRoundResult(Object[] resultData) {
        Platform.runLater(() -> {
            try {
                Scene scene = mySocket.getImageQuizScene();
                if (scene == null) {
                    System.err.println("ImageQuizFrm: Scene is null in displayRoundResult");
                    return;
                }
                
                int roundNumber = (Integer) resultData[0];
                int correctAnswer = (Integer) resultData[1];
                boolean player1Correct = (Boolean) resultData[2];
                boolean player2Correct = (Boolean) resultData[3];
                double player1Score = (Double) resultData[4];
                double player2Score = (Double) resultData[5];
                boolean gameFinished = (Boolean) resultData[8];
                String player1Username = resultData.length > 10 ? (String) resultData[10] : null;
                String player2Username = resultData.length > 11 ? (String) resultData[11] : null;
                
                System.out.println("ImageQuizFrm: displayRoundResult - Round " + roundNumber + ", Correct answer: " + correctAnswer);
                
                // Determine if current player is player1 or player2 (only set once)
                if (player1Username != null && player2Username != null) {
                    String myUsername = mySocket.getUsername();
                    if (myUsername.equals(player1Username)) {
                        isPlayer1 = true;
                    } else if (myUsername.equals(player2Username)) {
                        isPlayer1 = false;
                    }
                } else {
                    // Fallback: determine based on score pattern (if scores are same, might be first round)
                    // This is a backup in case usernames are not sent
                    if (roundNumber == 1) {
                        // Assume player1 initially, will be corrected when usernames are available
                        isPlayer1 = true;
                    }
                }
                
                // Update scores based on player role
                if (isPlayer1) {
                    currentMyScore = player1Score;
                    currentEnemyScore = player2Score;
                } else {
                    currentMyScore = player2Score;
                    currentEnemyScore = player1Score;
                }
                
                // Update score display
                updateScoreDisplay();
                
                // Stop countdown
                if (countdownTimeline != null) {
                    countdownTimeline.stop();
                }
                
                // Hide question to avoid overlap
                Label lblQuestion = (Label) scene.lookup("#lblQuestion");
                if (lblQuestion != null) {
                    lblQuestion.setVisible(false);
                }
                
                // Hide input fields
                TextField txtAnswer = (TextField) scene.lookup("#txtAnswer");
                Button btnSubmit = (Button) scene.lookup("#btnSubmit");
                if (txtAnswer != null) {
                    txtAnswer.setVisible(false);
                }
                if (btnSubmit != null) {
                    btnSubmit.setVisible(false);
                }
                
                // Show correct answer (no "Round X finished!" message)
                Label lblResult = (Label) scene.lookup("#lblResult");
                if (lblResult != null) {
                    String resultText = "✓ Correct answer: " + correctAnswer;
                    if (gameFinished) {
                        resultText += "\n\nGame finished!\n";
                        resultText += "Your score: " + String.format("%.1f", currentMyScore) + "\n";
                        resultText += "Opponent score: " + String.format("%.1f", currentEnemyScore);
                    }
                    System.out.println("ImageQuizFrm: Setting result text: " + resultText);
                    lblResult.setText(resultText);
                    lblResult.setVisible(true);
                    lblResult.setStyle("-fx-text-fill: white; -fx-background-color: rgba(0, 0, 0, 0.85); -fx-background-radius: 10px; -fx-padding: 15px; -fx-font-size: 16px; -fx-font-family: 'Be Vietnam Pro';");
                    System.out.println("ImageQuizFrm: Result label is now visible: " + lblResult.isVisible());
                } else {
                    System.err.println("ImageQuizFrm: lblResult is null, cannot display round result!");
                }
                
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
    
    private void showOpponentLeftDialog(String leftPlayerUsername, String result) {
        Platform.runLater(() -> {
            // Tạo custom dialog thông báo
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("Kết thúc trận đấu");
            dialog.initOwner(stage); // Căn giữa theo stage
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.initStyle(StageStyle.TRANSPARENT);
            
            // Tạo container với nút X
            StackPane container = new StackPane();
            container.setStyle("-fx-background-color: transparent;");
            
            // Tạo nội dung dialog
            VBox content = new VBox(20);
            content.setStyle("-fx-padding: 30px; -fx-alignment: center; -fx-background-color: #ffffff; -fx-background-radius: 20px; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 15, 0, 0, 5);");
            
            // Tạo HBox chứa nút X ở góc trên bên phải
            HBox headerBox = new HBox();
            headerBox.setAlignment(javafx.geometry.Pos.TOP_RIGHT);
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            
            // Nút X để đóng dialog (coi như CANCEL - không chuyển về Result screen)
            Button closeButton = new Button("✕");
            closeButton.setStyle("-fx-background-color: transparent; -fx-text-fill: #999; -fx-font-size: 20px; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 5px 10px;");
            closeButton.setOnAction(e -> {
                dialog.setResult(ButtonType.CANCEL);
                dialog.close();
            });
            
            headerBox.getChildren().addAll(spacer, closeButton);
            
            Label messageLabel = new Label("Đối thủ đã rời đi, nhấn OK để xem kết quả");
            messageLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #333; -fx-wrap-text: true; -fx-text-alignment: center; -fx-font-family: 'Be Vietnam Pro';");
            messageLabel.setMaxWidth(400);
            
            VBox contentBox = new VBox(10);
            contentBox.setAlignment(javafx.geometry.Pos.CENTER);
            contentBox.getChildren().addAll(headerBox, messageLabel);
            
            content.getChildren().add(contentBox);
            container.getChildren().add(content);
            
            dialog.getDialogPane().setContent(container);
            dialog.getDialogPane().setStyle("-fx-background-color: transparent; -fx-background-radius: 20px;");
            dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
            
            // Xử lý khi nhấn OK hoặc X
            dialog.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    // Chuyển về Result screen với kết quả
                    ResultFrm resultFrm = new ResultFrm();
                    mySocket.setResultFrm(resultFrm);
                    ObjectWrapper resultData = new ObjectWrapper(ObjectWrapper.SERVER_SEND_RESULT, result + "||" + leftPlayerUsername + "||opponent_left");
                    resultFrm.receivedDataProcessing(resultData);
                }
                // Nếu response là CANCEL (khi click X), không làm gì cả
            });
        });
    }
    
    private void updateScoreDisplay() {
        Platform.runLater(() -> {
            try {
                Scene scene = mySocket.getImageQuizScene();
                if (scene == null) {
                    return;
                }
                
                Label lblMyScore = (Label) scene.lookup("#lblMyScore");
                if (lblMyScore != null) {
                    lblMyScore.setText(String.format("Your Score: %.1f", currentMyScore));
                }
                
                Label lblEnemyScore = (Label) scene.lookup("#lblEnemyScore");
                if (lblEnemyScore != null) {
                    lblEnemyScore.setText(String.format("Opponent: %.1f", currentEnemyScore));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
    
    public void onSubmitClick() {
        submitAnswer();
    }
}

