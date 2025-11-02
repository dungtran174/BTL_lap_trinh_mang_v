package client.view;

import client.controller.ClientCtr;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import shared.dto.ObjectWrapper;

public class WaitingFrm {
    
    private ClientCtr mySocket = ClientCtr.getInstance();
    private Stage stage = mySocket.getStage();
    private boolean isInviter;
    
    public WaitingFrm(boolean isInviter) {
        this.isInviter = isInviter;
    }
    
    public void setIsInviter(boolean isInviter) {
        this.isInviter = isInviter;
    }
    
    public void openScene() {
        Platform.runLater(() -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/Fxml/Client/Waiting.fxml"));
                Scene scene = new Scene(loader.load());
                mySocket.setWaitingScene(scene);
                mySocket.setWaitingFrm(this);
                
                stage.setScene(scene);
                stage.setTitle("Waiting for Game Start");
                stage.show();
                
                Label waitingLabel = (Label) scene.lookup("#waitingLabel");
                Button btnStart = (Button) scene.lookup("#btnStart");
                
                if (isInviter) {
                    waitingLabel.setText("Chờ đối thủ sẵn sàng...\nNhấn Start để bắt đầu!");
                    btnStart.setVisible(true);
                    btnStart.setOnAction(event -> {
                        mySocket.sendData(new ObjectWrapper(ObjectWrapper.START_IMAGE_QUIZ_GAME));
                    });
                } else {
                    waitingLabel.setText("Chờ đối thủ bắt đầu game...");
                    btnStart.setVisible(false);
                }
                
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}

