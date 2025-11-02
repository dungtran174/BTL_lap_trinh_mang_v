package client.view;

import client.controller.ClientCtr;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import shared.dto.ObjectWrapper;

public class RankingFrm extends Application {

    FXMLLoader fxmlLoader;

    @Override
    public void start(Stage stage) throws Exception {

    }

    public static void openScene() {
        launch();
    }

    public void receivedDataProcessing(ObjectWrapper data) {
    }
}
