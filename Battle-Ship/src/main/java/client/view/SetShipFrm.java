package client.view;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Stage;
import shared.dto.ObjectWrapper;
import client.controller.ClientCtr;
import client.helper.ShipGenerator;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Optional;
import java.util.Timer;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.util.Duration;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import server.helper.CountDownTimer;

public class SetShipFrm {

    private ClientCtr mySocket = ClientCtr.getInstance();
    private Stage stage = mySocket.getStage();
    private boolean playerTurn = false;
    private int shipSize; //Kích thước tàu hiện tại được chọn để sắp
    private int shipIndexList; //Vị trí tàu được chọn (trong list) hiện tại trong danh sách tàu
    private boolean horizontal = true;
    private ArrayList<String> shipListModel = new ArrayList<>(Arrays.asList("#ship2", "#ship3-1", "#ship3-2", "#ship4", "#ship5"));
    private ArrayList<String> shipsLocation = new ArrayList<>();
    private Timeline TimeCD;
    public SetShipFrm() {
    }

    public void openScene(){
        

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Fxml/Client/SetShip.fxml"));
            Scene scene = new Scene(loader.load());
            mySocket.setSetShipScene(scene);
            
            stage.setScene(scene);
            stage.setTitle("Set Ship");
            stage.show();
            for(String shipId : shipListModel){
                ImageView ship = (ImageView) scene.lookup(shipId);
                if (ship != null) {
                    double originalScaleX = ship.getScaleX();
                    double originalScaleY = ship.getScaleY();
                   
                    ship.setOnMouseClicked(event -> {
                        playSound("setShip.wav");
                        if(shipId == "#ship2") {
                            shipSize =2;
                            shipIndexList=0;
                        }
                        else if(shipId == "#ship3-1" ) {
                            shipSize=3;
                            shipIndexList=1;
                        }
                        else if(shipId=="#ship3-2"){
                             shipIndexList=2;
                             shipSize=3;

                        }
                        else if(shipId == "#ship4") {
                            shipSize=4;
                            shipIndexList=3;
                        }
                        else {
                            shipSize=5;
                            shipIndexList=4;
                        }
                        System.out.println("Image clicked!  " + shipId);

                        // Phục hồi kích thước cho tất cả các tàu trước đó (nếu có tàu đã được phóng to)
                        for (String otherShipId : shipListModel) {
                            ImageView otherShip = (ImageView) scene.lookup(otherShipId);
                            if (otherShip != null && !otherShip.equals(ship)) {
                                otherShip.setScaleX(originalScaleX);
                                otherShip.setScaleY(originalScaleY);
                            }
                        }

                        // Phóng to tàu hiện tại
                        ship.setScaleX(originalScaleX * 1.2);
                        ship.setScaleY(originalScaleY * 1.2);
                    });

                    scene.setOnMouseClicked(event -> {
                        for (String otherShipId : shipListModel) {
                            ImageView otherShip = (ImageView) scene.lookup(otherShipId);
                            if (otherShip != null && !otherShip.isHover()) {
                                otherShip.setScaleX(originalScaleX);
                                otherShip.setScaleY(originalScaleY);
                            }
                        }
                    });
                }
                    
            }
            
            
            //add event to Board
            GridPane gridPane = (GridPane) scene.lookup("#board-ship");
            for (javafx.scene.Node node : gridPane.getChildren()) {
            if (node instanceof Pane) {
                Pane cell = (Pane) node;
                cell.setOnMouseClicked(event -> handleGridClick(event, cell));
            }
        }
            
            
            // add event to random btn 
            Button randomBtn = (Button) scene.lookup("#randomBtn");
            randomBtn.setOnMouseClicked(e ->{
                playSound("setShip.wav");
                random();
            });
            
            // add event to reset btn 
            Button resetBtn = (Button) scene.lookup("#resetBtn");
            resetBtn.setOnMouseClicked(e ->{
                playSound("setShip.wav");
                reset();
            });
            
            // add event to ready btn 
            Button readyBtn = (Button) scene.lookup("#readyBtn");
            readyBtn.setOnMouseClicked(e ->{
                playSound("ready.wav");
                ready();
            });
            
            Button exitBtn = (Button) scene.lookup("#exitBtn");
            exitBtn.setOnMouseClicked(e ->{
                audioBeep();
                Alert alert = new Alert(AlertType.CONFIRMATION);
                alert.setTitle("Thoát");
                alert.setHeaderText("Bạn có thật sự muốn thoát trận đấu? Điều này sẽ khiến bạn bị trừ điểm.");

                // Thêm các nút tùy chọn YES và NO
                ButtonType yesButton = new ButtonType("Yes");
                ButtonType noButton = new ButtonType("No");
                alert.getButtonTypes().setAll(yesButton, noButton);

                // Hiển thị hộp thoại và chờ người dùng chọn
                Optional<ButtonType> result = alert.showAndWait();

                if (result.isPresent() && result.get() == yesButton) {
                    TimeCD.stop();
                    mySocket.sendData(new ObjectWrapper(ObjectWrapper.QUIT_WHEN_SET_SHIP));
                    mySocket.setPoints(mySocket.getPoints()-1);
                    mySocket.setSetShipFrm(null);
                    mySocket.setSetShipScene(null);
                    if (mySocket.getMainFrm() == null) {
                        MainFrm mainFrm = new MainFrm();
                        mySocket.setMainFrm(mainFrm);
                    }
                    mySocket.getMainFrm().openScene();
                }
            });
            
            //  add event to vetical radio
            RadioButton ver = (RadioButton) scene.lookup("#vertical");
            //  add event to horizontal radio
            RadioButton hor = (RadioButton) scene.lookup("#horizontal");
                      
            ToggleGroup group = new ToggleGroup();
            ver.setToggleGroup(group);
            hor.setToggleGroup(group);
            
            group.selectedToggleProperty().addListener((observable, oldToggle, newToggle) -> {
                audioClickButton();
                if (newToggle == ver) {
                   horizontal=false;
                } else if (newToggle == hor) {
                   horizontal=true;
                }
            });
            TimeCD = setCountDownTime();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean isValidLocation(String location) {
        int row = location.charAt(1) - '0';
        int col = location.charAt(0) - '0';

        System.out.println("Check valid location: " + row + " " + col);

        if (horizontal) {
            if (col + shipSize > 10) {
                return false;
            }
        } else {
            if (row + shipSize > 10) {
                return false;
            }
        }

        for (int i = 0; i < shipSize; i++) {
            String loc;
            if (horizontal) {
                loc = String.valueOf(col+i) + String.valueOf(row);
            } else {
                loc = String.valueOf(col) + String.valueOf(row+i);
            }

            if (shipsLocation.contains(loc)) {
                return false;
            }
        }
        return true;
    }

    private void handleGridClick(MouseEvent e, Pane cell) {
        playSound("setShip.wav");
        String cellName = cell.getId();
        if (shipSize == 0 || !isValidLocation(cellName)) {
            System.out.println(shipSize + " " + cellName);
            return;
        }

        addShip(cellName);
        DisableShip(shipIndexList);
        resetShipSelection();
    }
    
    private void resetShipSelection() {
        shipIndexList = 5;
        shipSize = 0;
    }
    private void addShip(String location) {
        shipsLocation.add("/");
        int row = location.charAt(1) - '0';
        int col = location.charAt(0) - '0';

        ArrayList<String> currentShip = new ArrayList<>();

        for (int i = 0; i < shipSize; i++) {
            shipsLocation.add(location);
            currentShip.add(location);
            location = horizontal ? ++col + "" + row : col + "" + ++row;
        }
        drawShip(currentShip);
  
    }
    private void drawShip(ArrayList<String> currentShip){
        int startRow = currentShip.get(0).charAt(1) - '0';
        int startCol = currentShip.get(0).charAt(0) - '0';
        int endRow = currentShip.get(currentShip.size() - 1).charAt(1) - '0';
        boolean isHorizontal = (startRow == endRow);

        GridPane gridPane = (GridPane) mySocket.getSetShipScene().lookup("#board-ship");
        String shipImagePath =  getClass().getResource("/Images/ship" + currentShip.size() + ".png").toExternalForm();
        
        if (!isHorizontal) {
            shipImagePath= getClass().getResource("/Images/ship" + currentShip.size()+"-ver" + ".png").toExternalForm();
        }
        Image shipImage = new Image(shipImagePath);
        ImageView shipImageView = new ImageView(shipImage);
        if(isHorizontal){
            shipImageView.setFitWidth(35 * currentShip.size());
            shipImageView.setFitHeight(32);
        }else{
            shipImageView.setFitWidth(32);
            shipImageView.setFitHeight(35 * currentShip.size());
        }
        Pane shipPane = new Pane();
        shipPane.getChildren().add(shipImageView);
        
        gridPane.add(shipPane, startCol, startRow);

        if (isHorizontal) {
            GridPane.setColumnSpan(shipPane, currentShip.size()); 
        } else {
            GridPane.setRowSpan(shipPane, currentShip.size()); 
        }
    }
    
    private void initiateShips(){
        shipListModel = new ArrayList<>(Arrays.asList("#ship2", "#ship3-1", "#ship3-2", "#ship4", "#ship5"));

    }
    private void reset() {
        shipsLocation.clear();
        initiateShips();
        for(int i=0;i<shipListModel.size();i+=1){
            ImageView arrangedShip = (ImageView) mySocket.getSetShipScene().lookup(shipListModel.get(i));
            arrangedShip.setVisible(true);
            arrangedShip.setDisable(false);
        }
        GridPane gridPane = (GridPane) mySocket.getSetShipScene().lookup("#board-ship");
        Iterator<Node> iterator = gridPane.getChildren().iterator();
        while (iterator.hasNext()) {
            Node node = iterator.next();
            if (node instanceof Pane) {
                Pane pane = (Pane) node;
                if (!pane.getChildren().isEmpty() && pane.getChildren().get(0) instanceof ImageView) {
                    iterator.remove();
                }
            }
        }
    }
    private void DisableShip(int idx){
        ImageView arrangedShip = (ImageView) mySocket.getSetShipScene().lookup(shipListModel.get(idx));
        arrangedShip.setVisible(false);
        arrangedShip.setDisable(true);
    }
    private void random() {
            reset();
            shipsLocation = ShipGenerator.generateShip();
            ArrayList<String> currentShip = new ArrayList<>();

            for (String loc : shipsLocation) {
                if (loc.equals("/")) {
                    if (!currentShip.isEmpty()) {
                        drawShip(currentShip);
                        currentShip = new ArrayList<>();
                    }
                    currentShip = new ArrayList<>();
                } else {
                    currentShip.add(loc);
                }
            }
            // Vẽ con tàu cuối cùng nếu có
            if (!currentShip.isEmpty()) {
                drawShip(currentShip);
            }
 
            for(int i=0;i<shipListModel.size();i+=1){
                DisableShip(i);
            }
            for(int i=shipListModel.size()-1;i>=0;i-=1){
                shipListModel.remove(i);
            }
    }
    private ArrayList<String> swapshipsLocation(ArrayList<String> locationArr){
        ArrayList<String> tmp = new ArrayList<>();
        for(String pos : locationArr){
            if(pos.compareTo("/") == 0){
                tmp.add(pos);
            }else{
                tmp.add(pos.charAt(1) + "" + pos.charAt(0));
            }
        }
        return tmp;
    }
    private void ready() {
        int cnt=0;
        for(String id : shipListModel){
            ImageView img = (ImageView) mySocket.getSetShipScene().lookup(id);
            if(img.isDisable()) cnt+=1;
        }
        Label msg = (Label) mySocket.getSetShipScene().lookup("#message");
        if (cnt == 5 || shipListModel.size() == 0) {
            TimeCD.stop();

            ObjectWrapper objectWrapper = new ObjectWrapper(ObjectWrapper.READY_PLAY_GAME, swapshipsLocation(shipsLocation));
            mySocket.sendData(objectWrapper);
            
            msg.setText("Waiting Your Enemy");
            RadioButton ver = (RadioButton) mySocket.getSetShipScene().lookup("#vertical");
            ver.setDisable(true);
            RadioButton hor = (RadioButton) mySocket.getSetShipScene().lookup("#horizontal");
            hor.setDisable(true);
            
            Button randomBtn = (Button) mySocket.getSetShipScene().lookup("#randomBtn");
            randomBtn.setDisable(true);
            Button resetBtn = (Button) mySocket.getSetShipScene().lookup("#resetBtn");
            resetBtn.setDisable(true);
            Button readyBtn = (Button) mySocket.getSetShipScene().lookup("#readyBtn");
            readyBtn.setDisable(true);
        } else {
            msg.setText("Xếp đủ tàu đi");
        }
    }
    private Timeline  setCountDownTime() {
        final int[] timeRemaining = {91};
        final Timeline[] timelineWrapper = new Timeline[1]; // Sử dụng mảng để bọc Timeline

        timelineWrapper[0] = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            timeRemaining[0]--;

            Label lblTime = (Label) mySocket.getSetShipScene().lookup("#clock");
            lblTime.setText(String.valueOf(timeRemaining[0]));

            // Kiểm tra khi hết giờ và dừng Timeline
            if (timeRemaining[0] <= 0) {
                timelineWrapper[0].stop(); // Dừng Timeline khi hết thời gian
            }
        }));
        timelineWrapper[0].setCycleCount(Timeline.INDEFINITE); // Đặt để lặp vô hạn
        timelineWrapper[0].play(); // Bắt đầu bộ đếm thời gian

        return timelineWrapper[0]; // Trả về Timeline để có thể điều khiển từ bên ngoài
    }
    public void receivedDataProcessing(ObjectWrapper data) {
        Platform.runLater(() -> {
            switch (data.getPerformative()) {
                case ObjectWrapper.SERVER_RANDOM_NOT_TURN:
                    playerTurn = false;
                    break;
                case ObjectWrapper.SERVER_RANDOM_TURN:
                    playerTurn = true;
                    break;
                case ObjectWrapper.SERVER_REQUEST_READY_GAME:
                    random();
                    ready();
                    break;
                case ObjectWrapper.SERVER_START_PLAY_GAME:
                    if (mySocket.getPlayFrm()== null) {
                        PlayFrm playFrm = new PlayFrm(shipsLocation, playerTurn);
                        mySocket.setPlayFrm(playFrm);
                    }
                    mySocket.getPlayFrm().openScene();
                    break;
                case ObjectWrapper.SERVER_TRANSFER_QUIT_WHEN_SET_SHIP:
                    TimeCD.stop();
                    
                    Alert alert = new Alert(AlertType.INFORMATION);
                    alert.setTitle("Kết thúc trận đấu");
                    alert.setHeaderText(null);
                    alert.setContentText("Đối thủ của bạn đã rời đi, nhấn OK để xem kết quả");
                    alert.showAndWait();
                    ResultFrm resultFrm = new ResultFrm();
                    mySocket.setResultFrm(resultFrm);
                    mySocket.getResultFrm().openScene();
                    break;
            }
        });
    }

    public void audioClickButton(){
        String clickButtonFile = new File("src/main/resources/Sounds/clickButton.mp3").toURI().toString();
        Media clickButton = new Media(clickButtonFile);
        MediaPlayer clickButtonPlayer = new MediaPlayer(clickButton);
        clickButtonPlayer.setVolume(0.8);
        clickButtonPlayer.play();
    }

    public void audioBeep(){
        String beepFile = new File("src/main/resources/Sounds/beeping.mp3").toURI().toString();
        Media beep = new Media(beepFile);
        MediaPlayer beepPlayer = new MediaPlayer(beep);
        beepPlayer.setVolume(1);
        beepPlayer.play();
    }
    
    
    private void playSound(String soundFileName) {
        try {
            InputStream audioSrc = getClass().getClassLoader().getResourceAsStream("Sounds/" + soundFileName);
            if (audioSrc == null) {
                System.out.println("File không tồn tại: " + soundFileName);
                return;
            }

            // Đọc audio từ InputStream
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(audioSrc);
            Clip clip = AudioSystem.getClip();
            clip.open(audioStream);
            clip.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
