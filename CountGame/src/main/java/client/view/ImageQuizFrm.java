package client.view;

import client.controller.ClientCtr;
import javafx.application.Platform;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
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
                
                // Set button action handler
                Button btnSubmit = (Button) scene.lookup("#btnSubmit");
                if (btnSubmit != null) {
                    btnSubmit.setOnAction(event -> submitAnswer());
                }
                
            } catch (Exception e) {
                e.printStackTrace();
            }
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
                
                // Display question
                Label lblQuestion = (Label) scene.lookup("#lblQuestion");
                if (lblQuestion != null) {
                    lblQuestion.setText(question);
                }
                
                // Display image
                ImageView imgView = (ImageView) scene.lookup("#imgView");
                if (imgView != null && imageBytes != null) {
                    ByteArrayInputStream bis = new ByteArrayInputStream(imageBytes);
                    Image image = new Image(bis);
                    imgView.setImage(image);
                }
                
                // Clear answer field
                TextField txtAnswer = (TextField) scene.lookup("#txtAnswer");
                if (txtAnswer != null) {
                    txtAnswer.setText("");
                    txtAnswer.setDisable(false);
                }
                
                // Enable submit button
                Button btnSubmit = (Button) scene.lookup("#btnSubmit");
                if (btnSubmit != null) {
                    btnSubmit.setDisable(false);
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
        if (lblTimer != null) {
            lblTimer.setText(String.valueOf(seconds));
        }
        
        final int[] timeRemaining = {seconds};
        countdownTimeline = new Timeline();
        countdownTimeline.getKeyFrames().add(new KeyFrame(Duration.seconds(1), event -> {
            timeRemaining[0]--;
            if (lblTimer != null) {
                lblTimer.setText(String.valueOf(timeRemaining[0]));
            }
            
            if (timeRemaining[0] <= 0) {
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
        
        countdownTimeline.setCycleCount(seconds);
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
                            displayRound(roundNumber, imageBytes, question);
                        } else {
                            System.err.println("ImageQuizFrm: Invalid round data received");
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
                        
                        // Go to result screen
                        ResultFrm resultFrm = new ResultFrm();
                        mySocket.setResultFrm(resultFrm);
                        resultFrm.openScene();
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
                
                // Show round result
                Label lblResult = (Label) scene.lookup("#lblResult");
                if (lblResult != null) {
                    String resultText = "Round " + roundNumber + " kết thúc!\n";
                    resultText += "Đáp án đúng: " + correctAnswer + "\n";
                    resultText += "Điểm của bạn: " + currentMyScore + "\n";
                    resultText += "Điểm đối thủ: " + currentEnemyScore;
                    if (gameFinished) {
                        resultText += "\n\nGame kết thúc!";
                    }
                    lblResult.setText(resultText);
                    lblResult.setVisible(true);
                    lblResult.setStyle("-fx-text-fill: white; -fx-background-color: rgba(0, 0, 0, 0.8); -fx-background-radius: 10px; -fx-padding: 15px;");
                }
                
                // Stop countdown
                if (countdownTimeline != null) {
                    countdownTimeline.stop();
                }
                
            } catch (Exception e) {
                e.printStackTrace();
            }
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
                    lblMyScore.setText(String.format("Điểm của bạn: %.1f", currentMyScore));
                }
                
                Label lblEnemyScore = (Label) scene.lookup("#lblEnemyScore");
                if (lblEnemyScore != null) {
                    lblEnemyScore.setText(String.format("Điểm đối thủ: %.1f", currentEnemyScore));
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

