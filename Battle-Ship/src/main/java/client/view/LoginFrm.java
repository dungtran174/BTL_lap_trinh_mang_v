package client.view;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Stage;
import javafx.application.Platform;

import javafx.stage.WindowEvent;
import shared.dto.ObjectWrapper;
import client.controller.ClientCtr;
import shared.model.Player;

import java.io.File;
import java.util.concurrent.TimeUnit;

public class LoginFrm extends Application {

    private ClientCtr mySocket = ClientCtr.getInstance();
    private Stage stage = mySocket.getStage();
    MediaPlayer backgroundMusicPlayer;

    public LoginFrm() {

    }


    @Override
    public void start(Stage stage) throws Exception {

        mySocket.setStage(stage);
        try {
            mySocket.setStage(stage);
            stage.setOnCloseRequest((WindowEvent event) -> {
                mySocket.sendData(new ObjectWrapper(ObjectWrapper.EXIT_MAIN_FORM, null));
                mySocket.setMainScene(null);
                mySocket.setSetShipFrm(null);
                mySocket.setLoginScreen(null);
            });
            openScene();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void openScene (){
        if(mySocket.getStage() == null) launch();

        Platform.runLater(() -> {
            try {
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/Fxml/Client/Login.fxml"));
                Scene scene = new Scene(fxmlLoader.load());
                mySocket.setLoginScreen(scene);

                stage = mySocket.getStage();
                stage.setScene(scene);
                stage.setTitle("Login");
                stage.show();

                // JavaFX UI Controls
                TextField usernameLoginTextField = (TextField) fxmlLoader.getNamespace().get("usernameLoginTextField");
                PasswordField passwordLoginField = (PasswordField) fxmlLoader.getNamespace().get("passwordLoginField");
                Button loginButton = (Button) fxmlLoader.getNamespace().get("loginButton");
                Button signupButton = (Button) fxmlLoader.getNamespace().get("signupButton");

                Label msg = (Label) scene.lookup("#msg");
                ImageView imgErr = (ImageView) scene.lookup("#iconErr");
                msg.setVisible(false);
                imgErr.setVisible(false);

                // Set up click event handlers
                loginButton.setOnAction(event -> {
                    audioClickButton();
                    // Handle login button click
                    String username = usernameLoginTextField.getText();
                    String password = passwordLoginField.getText();

                    if(username.isEmpty()){
                        msg.setVisible(true);
                        imgErr.setVisible(true);
                        msg.setText("Error: Username is not empty!");
                        return;
                    }
                    if(password.isEmpty()){
                        msg.setVisible(true);
                        imgErr.setVisible(true);
                        msg.setText("Error: Password is not empty!");
                        return;
                    }
                    // Process login logic
                    Player player = new Player(username, password);
                    mySocket.sendData(new ObjectWrapper(ObjectWrapper.LOGIN_USER, player));
                });

                signupButton.setOnAction(event -> {
                    audioClickButton();
                    if (mySocket.getRegisterFrm() == null) {
                        RegisterFrm registerFrm = new RegisterFrm();
                        mySocket.setRegisterFrm(registerFrm);
                    }
                    backgroundMusicPlayer.stop();
                    mySocket.getRegisterFrm().openScene();
                });
                initializeBackgroundMusic();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public void receivedDataProcessing(ObjectWrapper data) {
        Platform.runLater(() -> {

            String result = (String) data.getData();
            Scene loginScreen = mySocket.getLoginScreen();
            if (result.equals("false")) {
                Scene scene = mySocket.getLoginScreen();
                Label msg = (Label) scene.lookup("#msg");
                ImageView imgErr = (ImageView) scene.lookup("#iconErr");
                msg.setVisible(true);
                imgErr.setVisible(true);
                msg.setWrapText(true);
                msg.setText("Error: Incorrect username or password.");
            } else {
                TextField usernameLoginTextField = (TextField) loginScreen.lookup("#usernameLoginTextField");
                String username = usernameLoginTextField.getText();

                mySocket.setUsername(username);
                mySocket.setPoints(Integer.parseInt(result));
//                System.out.println(username);

                mySocket.sendData(new ObjectWrapper(ObjectWrapper.LOGIN_SUCCESSFUL, mySocket.getUsername()));

                if (mySocket.getMainFrm() == null) {
                    MainFrm mainFrm = new MainFrm();
                    mySocket.setMainFrm(mainFrm);
                }
                mySocket.getBackgroundMusicPlayer().stop();

                mySocket.getMainFrm().openScene();

            }
        });
    }

    public void initializeBackgroundMusic(){
        // tao am thanh nen
        String backgroundMusicFile = new File("src/main/resources/Sounds/login.mp3").toURI().toString();
        Media backgroundMusic = new Media(backgroundMusicFile);
        backgroundMusicPlayer = new MediaPlayer(backgroundMusic);
        mySocket.setBackgroundMusicPlayer(backgroundMusicPlayer);

        backgroundMusicPlayer.setVolume(0.6);
        backgroundMusicPlayer.setCycleCount(MediaPlayer.INDEFINITE);
        
        // Thêm error handler
        backgroundMusicPlayer.setOnError(() -> {
            System.out.println("Media error occurred: " + backgroundMusicPlayer.getError());
        });

        // Thêm status listener để debug
        backgroundMusicPlayer.statusProperty().addListener((observable, oldValue, newValue) -> {
            System.out.println("Status changed from " + oldValue + " to " + newValue);
        });

        backgroundMusicPlayer.play();
    }

    public void audioClickButton(){
        String clickButtonFile = new File("src/main/resources/Sounds/click-233950.mp3").toURI().toString();
        Media clickButton = new Media(clickButtonFile);
        MediaPlayer clickButtonPlayer = new MediaPlayer(clickButton);
        clickButtonPlayer.setVolume(0.8);
        clickButtonPlayer.play();
    }



}
