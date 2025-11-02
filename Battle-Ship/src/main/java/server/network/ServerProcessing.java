package server.network;

import server.controller.GameCtr;
import server.controller.ImageQuestionManager;
import server.controller.ImageQuizGameCtr;
import server.controller.ServerCtr;
import server.dao.MatchDAO;
import server.dao.PlayerDAO;
import server.helper.CountDownTimer;
import shared.dto.ObjectWrapper;
import shared.dto.PlayerHistory;
import shared.model.Match;
import shared.model.Player;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class ServerProcessing extends Thread {

    private Socket mySocket;
    private ServerCtr serverCtr;
    private volatile boolean isRunning = true;
    private ObjectInputStream ois;
    private ObjectOutputStream oos;

    private String username;
    private ServerProcessing enemy;

    private boolean isOnline = false; // online
    private boolean inGame = false;   // trong game

    private GameCtr gameCtr; // Old battleship game controller (keeping for compatibility)
    private ImageQuizGameCtr imageQuizGameCtr; // New image quiz game controller
    private boolean isInviter = false; // Track if this player is the inviter
    
    private CountDownTimer timeTask;
    private Timer timer;
    private Timer checkTimer;
    private TimerTask checkTimeTask;

    private String result; // win, loss, afk, cancelled

    private PlayerDAO playerDAO = new PlayerDAO();
    private MatchDAO matchDAO = new MatchDAO();

    public ServerProcessing(Socket s, ServerCtr serverCtr) throws IOException {
        super();
        mySocket = s;
        this.serverCtr = serverCtr;
        ois = new ObjectInputStream(mySocket.getInputStream());
        oos = new ObjectOutputStream(mySocket.getOutputStream());
    }

    public void sendData(Object obj) {
        try {
            oos.writeObject(obj);
            oos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            while (isRunning) {
                Object o = ois.readObject();
                if (o instanceof ObjectWrapper) {
                    ObjectWrapper data = (ObjectWrapper) o;

                    switch (data.getPerformative()) {
                        case ObjectWrapper.REGISTER_USER:
                            Player registerInfor = (Player) data.getData();
                            boolean checkRes = playerDAO.checkExistAccount(registerInfor);
                            if(checkRes){
                                sendData(new ObjectWrapper(ObjectWrapper.SERVER_REGISTER_USER, "false"));
                                break;
                            }else{
                                playerDAO.CreateAccount(registerInfor);
                                sendData(new ObjectWrapper(ObjectWrapper.SERVER_REGISTER_USER, "true"));
                                break;
                            }
                        case ObjectWrapper.LOGIN_USER:
                            Player player = (Player) data.getData();
                            sendData(new ObjectWrapper(ObjectWrapper.SERVER_LOGIN_USER, playerDAO.checkLogin(player)));
                            break;
                        case ObjectWrapper.LOGIN_SUCCESSFUL:
                            String username = (String) data.getData();
                            this.username = username;
                            inGame = false;
                            isOnline = true;
                            serverCtr.sendWaitingList();
                            break;
                        case ObjectWrapper.SEND_PLAY_REQUEST: // data la username nguoi nhan
                            String username1 = (String) data.getData();
                            boolean canSend = false;
                            for (ServerProcessing sp : serverCtr.getMyProcess()) {
                                if (sp.getUsername().equals(username1) && !sp.inGame) {
                                    canSend = true;
                                    System.out.println(new ObjectWrapper(ObjectWrapper.RECEIVE_PLAY_REQUEST, this.username));
                                    sp.enemy = this;
                                    this.enemy = sp;
                                    this.isInviter = true; // This player is the inviter
                                    sp.isInviter = false; // The other player is not the inviter
                                    System.out.println("Enemy before send play request: " + enemy);
                                    sp.sendData(new ObjectWrapper(ObjectWrapper.RECEIVE_PLAY_REQUEST, this.username));
                                    break;
                                }
                            }

                            System.out.println("Enemy before send play request after loop: " + enemy);

                            if (!canSend) {
                                sendData(new ObjectWrapper(ObjectWrapper.SERVER_SEND_PLAY_REQUEST_ERROR));
                            }
                            break;
                        case ObjectWrapper.ACCEPTED_PLAY_REQUEST:
                            if (!enemy.inGame && enemy.isOnline) {
                                enemy.enemy = this;
                                // Set inviter flag - the player who sent the request is the inviter
                                // This should already be set in SEND_PLAY_REQUEST, but ensure it's correct
                                if (enemy.isInviter) {
                                    this.isInviter = false;
                                } else {
                                    enemy.isInviter = true;
                                    this.isInviter = false;
                                }
                                
                                inGame = true;
                                enemy.inGame = true;
                                // Send to waiting interface (both players)
                                enemy.sendData(new ObjectWrapper(ObjectWrapper.SERVER_SET_GAME_READY, enemy.isInviter));
                                sendData(new ObjectWrapper(ObjectWrapper.SERVER_SET_GAME_READY, this.isInviter));
                                serverCtr.sendWaitingList();
                            } else {
//                                enemy.sendData(new ObjectWrapper(ObjectWrapper.ENEMY_IN_GAME_ERROR));
                            }
                            break;
                        case ObjectWrapper.REJECTED_PLAY_REQUEST:
                            enemy = null;
                            break;
                        case ObjectWrapper.START_IMAGE_QUIZ_GAME:
                            // Only inviter can start the game
                            if (isInviter && enemy != null) {
                                // Reset any existing game state first
                                if (imageQuizGameCtr != null) {
                                    imageQuizGameCtr = null;
                                }
                                if (enemy.imageQuizGameCtr != null) {
                                    enemy.imageQuizGameCtr = null;
                                }
                                
                                System.out.println("Server: Starting new image quiz game between " + this.username + " and " + enemy.username);
                                
                                // Initialize the image quiz game for both players
                                imageQuizGameCtr = new ImageQuizGameCtr(this.username, enemy.username);
                                enemy.imageQuizGameCtr = imageQuizGameCtr; // Share the same game controller
                                
                                System.out.println("Server: Game initialized, starting round 1");
                                
                                // Start round 1
                                startImageQuizRound();
                            } else {
                                System.out.println("Server: Cannot start game - isInviter: " + isInviter + ", enemy: " + (enemy != null ? enemy.username : "null"));
                            }
                            break;
                        case ObjectWrapper.SUBMIT_ANSWER:
                            if (imageQuizGameCtr != null && enemy != null) {
                                Integer answer = (Integer) data.getData();
                                
                                // Check if round already processed
                                if (imageQuizGameCtr.isRoundProcessed()) {
                                    System.out.println("Server: Round already processed, ignoring answer from " + this.username);
                                    break;
                                }
                                
                                boolean wasFirstAnswer = !imageQuizGameCtr.hasAnyAnswer();
                                System.out.println("Server: Answer received from " + this.username + " = " + answer + 
                                                 ", wasFirstAnswer = " + wasFirstAnswer + 
                                                 ", round = " + imageQuizGameCtr.getCurrentRoundNumber() +
                                                 ", roundProcessed = " + imageQuizGameCtr.isRoundProcessed());
                                
                                imageQuizGameCtr.submitAnswer(this.username, answer);
                                
                                // Process immediately if this is the first answer in the round
                                if (wasFirstAnswer && !imageQuizGameCtr.isRoundProcessed()) {
                                    System.out.println("Server: First answer received from " + this.username + " in round " + 
                                                     imageQuizGameCtr.getCurrentRoundNumber() + ", processing round immediately");
                                    processImageQuizRound();
                                }
                            }
                            break;
                        case ObjectWrapper.READY_PLAY_GAME: // OLD BATTLESHIP CODE - keeping for compatibility
                            stopAllTimers();
                            gameCtr.setSetup(true);
                            ArrayList<String> shipsLocation = (ArrayList<String>) data.getData();

                            System.out.println("Server: Có ai đó đã xếp xong");

                            System.out.println(this.username + "Bên server:");
                            for (String x : shipsLocation) {
                                System.out.print(x + " ");
                            }

                            // set ship location in game control ?
                            gameCtr.setPlayerShips(new ArrayList<>(shipsLocation));
                            enemy.gameCtr.setEnemyShips(new ArrayList<>(shipsLocation));

                            if (enemy.gameCtr.isSetup()) {
                                if ((int) Math.random() * 10 % 2 == 0) {
                                    sendData(new ObjectWrapper(ObjectWrapper.SERVER_RANDOM_TURN));
                                    gameCtr.setPlayerTurn(true);
                                    countDownPlay(18);

                                    enemy.sendData(new ObjectWrapper(ObjectWrapper.SERVER_RANDOM_NOT_TURN));
                                    enemy.gameCtr.setPlayerTurn(false);
                                } else {
                                    sendData(new ObjectWrapper(ObjectWrapper.SERVER_RANDOM_NOT_TURN));
                                    gameCtr.setPlayerTurn(false);
                                    enemy.sendData(new ObjectWrapper(ObjectWrapper.SERVER_RANDOM_TURN));
                                    enemy.gameCtr.setPlayerTurn(true);
                                    enemy.countDownPlay(18);
                                }
                                sendData(new ObjectWrapper(ObjectWrapper.SERVER_START_PLAY_GAME));
                                enemy.sendData(new ObjectWrapper(ObjectWrapper.SERVER_START_PLAY_GAME));
                            }
                            break;
                        case ObjectWrapper.EXIT_MAIN_FORM:
                            inGame = false;
                            isOnline = false;
                            serverCtr.sendWaitingList();
                            break;
                        case ObjectWrapper.UPDATE_WAITING_LIST_REQUEST:
                            serverCtr.sendWaitingList();
                            break;
                        case ObjectWrapper.SHOOT_REQUEST:
                            gameCtr.setShot(true);
                            gameCtr.setCntMissTurn(0);
                            String location = (String) data.getData();
                            System.out.println("Server shoot "+ location);

                            Object[] result = gameCtr.handleShot(location);
                            stopAllTimers();

                            if ((int) result[0] == 0) {
                                sendData(new ObjectWrapper(ObjectWrapper.SERVER_TRANSFER_SHOOT_FAILTURE, location));
                                gameCtr.setPlayerTurn(false);
                                gameCtr.setShot(false);
                                enemy.sendData(new ObjectWrapper(ObjectWrapper.SERVER_TRANSFER_SHOOT_FAILTURE, location));
                                enemy.gameCtr.setPlayerTurn(true);
                                enemy.countDownPlay(18);
                            } else {
                                if (result[1] != null) {
                                    String[] destroyedShip = (String[]) result[1];
                                    if (!(boolean) result[2]) {
                                        sendData(new ObjectWrapper(ObjectWrapper.SERVER_TRANSFER_SHOOT_HIT_SHIP, destroyedShip));
                                        gameCtr.setShot(false);
                                        enemy.sendData(new ObjectWrapper(ObjectWrapper.SERVER_TRANSFER_SHOOT_HIT_SHIP, destroyedShip));
                                        countDownPlay(18);
                                    } else {
                                        this.result = "win";
                                        enemy.result = "loss";

                                        // update result match ở đây (DAO)
                                        Match match = new Match(this.username, enemy.username, "win", "loss", 1, 0);
                                        matchDAO.updateMatchResult(match);

                                        playerDAO.updateWin(this.username);
                                        playerDAO.updateLoss(enemy.username);
                                        sendData(new ObjectWrapper(ObjectWrapper.SERVER_TRANSFER_END_GAME, destroyedShip));
                                        gameCtr.setShot(false);
                                        enemy.sendData(new ObjectWrapper(ObjectWrapper.SERVER_TRANSFER_END_GAME, destroyedShip));
                                    }
                                } else {
                                    sendData(new ObjectWrapper(ObjectWrapper.SERVER_TRANSFER_SHOOT_HIT_POINT, location));
                                    gameCtr.setShot(false);
                                    enemy.sendData(new ObjectWrapper(ObjectWrapper.SERVER_TRANSFER_SHOOT_HIT_POINT, location));
                                    countDownPlay(18);
                                }
                            }
                            break;
                        case ObjectWrapper.GET_RESULT:
                            if (this.result.equals("win")) {
                                sendData(new ObjectWrapper(ObjectWrapper.SERVER_SEND_RESULT, "win||" + enemy.getUsername()));
                            } else if (this.result.equals("loss")) {
                                sendData(new ObjectWrapper(ObjectWrapper.SERVER_SEND_RESULT, "loss||" + enemy.getUsername()));
                            } else if (this.result.equals("cancelled")) {
                                sendData(new ObjectWrapper(ObjectWrapper.SERVER_SEND_RESULT, "cancelled||" + enemy.getUsername()));
                            } else if (this.result.equals("draw")) {
                                sendData(new ObjectWrapper(ObjectWrapper.SERVER_SEND_RESULT, "draw||" + enemy.getUsername()));
                            }
                            break;
                        case ObjectWrapper.QUIT_WHEN_SET_SHIP:
                            stopAllTimers();
                            enemy.stopAllTimers();
                            inGame = false;
                            enemy.result = "cancelled";
                            enemy.sendData(new ObjectWrapper(ObjectWrapper.SERVER_TRANSFER_QUIT_WHEN_SET_SHIP, this.username));

                            Match match1 = new Match(this.username, enemy.username, "afk", "cancelled", -1, 0);
                            matchDAO.updateMatchResult(match1);

                            playerDAO.updateAfk(this.username);

                            enemy = null;

                            serverCtr.sendWaitingList();

                            break;
                        case ObjectWrapper.QUIT_WHEN_PLAY:
                            stopAllTimers();
                            enemy.stopAllTimers();
                            inGame = false;
                            enemy.result = "cancelled";
                            enemy.sendData(new ObjectWrapper(ObjectWrapper.SERVER_TRANSFER_QUIT_WHEN_PLAY, this.username));

                            Match match2 = new Match(this.username, enemy.username, "afk", "cancelled", -1, 0);
                            matchDAO.updateMatchResult(match2);

                            playerDAO.updateAfk(this.username);

                            enemy = null;

                            serverCtr.sendWaitingList();

                            break;
                        case ObjectWrapper.BACK_TO_MAIN_FORM:
                            // Reset game state completely
                            stopAllTimers();
                            if (enemy != null) {
                                enemy.stopAllTimers();
                                enemy.imageQuizGameCtr = null;
                            }
                            imageQuizGameCtr = null;
                            isInviter = false; // Reset inviter flag
                            if (enemy != null) {
                                enemy.isInviter = false; // Reset enemy's inviter flag too
                            }
                            enemy = null;
                            inGame = false;
                            serverCtr.sendWaitingList();
                            System.out.println("Server: " + this.username + " returned to main form, reset all game state");
                            break;
                        case ObjectWrapper.GET_HISTORY:
                            PlayerHistory playerHistory = playerDAO.getPlayerInfo(this.username);
                            playerHistory.setListMatch(matchDAO.getMatchHistory(this.username));
                            sendData(new ObjectWrapper(ObjectWrapper.SERVER_SEND_HISTORY, playerHistory));
                            break;
                        case ObjectWrapper.GET_RANKING:
                            List<PlayerHistory> leaderboard = playerDAO.getLeaderboard();
                            sendData(new ObjectWrapper(ObjectWrapper.SERVER_SEND_RANKING, leaderboard));
                            break;

                        case ObjectWrapper.GET_ALL_USER:
                            ArrayList<Player> allUser = playerDAO.getAllUser();
                            sendData(new ObjectWrapper(ObjectWrapper.SERVER_SEND_ALL_USER, allUser));
                            break;
                    }

                }
            }
        } catch (Exception e) {
//            e.printStackTrace();
//            serverCtr.removeServerProcessing(this);
//            try {
//                mySocket.close();
//            } catch (Exception ex) {
//                ex.printStackTrace();
//            }
//            this.stop();
        } finally {
            serverCtr.removeServerProcessing(this);
            if (inGame) {
                enemy.inGame = false;
                enemy.sendData(new ObjectWrapper(ObjectWrapper.SERVER_DISCONNECTED_CLIENT_ERROR));
                enemy.enemy = null;
            }
            closeSocket();
        }
    }

    public void stopProcessing() {
        isRunning = false;
        closeSocket();
    }

    private void closeSocket() {
        try {
            if (ois != null) {
                ois.close();
            }
            if (oos != null) {
                oos.close();
            }
            if (mySocket != null && !mySocket.isClosed()) {
                mySocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getUsername() {
        return username;
    }

    public Player getPlayer() {
        PlayerDAO playerDAO = new PlayerDAO();
        return playerDAO.getPlayer(username);
    }

    public ServerProcessing getEnemy() {
        return enemy;
    }

    public boolean isInGame() {
        return inGame;
    }

    public boolean isIsOnline() {
        return isOnline;
    }

    @Override
    public String toString() {
        return "ServerProcessing{" + "username=" + username + ", inGame=" + inGame + '}';
    }

    private void countDownSetShip(int timeRemaining) {
        timeTask = new CountDownTimer(timeRemaining);
        timer = new Timer();
        timer.scheduleAtFixedRate(timeTask, 0, 1000);

        // TimerTask để kiểm tra thời gian và thực hiện khi đếm ngược về 0
        checkTimeTask = new TimerTask() {
            @Override
            public void run() {
                int remaining = timeTask.getTimeRemaining();
                System.out.println(username + "Server Time remaining: " + remaining);

                if (remaining <= 0) {
                    stopAllTimers(); // Hủy tất cả các timer
                    System.out.println("Countdown finished.");

                    // Hết thời gian vẫn chưa ready (trong lúc xếp)
                    if (!gameCtr.isSetup()) {
                        System.out.println(username + " Bên server: Có ai đó chưa xếp xong");
                        sendData(new ObjectWrapper(ObjectWrapper.SERVER_REQUEST_READY_GAME));
                    }
                }
            }
        };

        // Khởi chạy task kiểm tra mà không ảnh hưởng đến ServerProcessing
        checkTimer = new Timer();
        checkTimer.scheduleAtFixedRate(checkTimeTask, 0, 1000);
    }

    private void countDownPlay(int timeRemaining) {
        timeTask = new CountDownTimer(timeRemaining);
        timer = new Timer();
        timer.scheduleAtFixedRate(timeTask, 0, 1000);

        // TimerTask để kiểm tra thời gian và thực hiện khi đếm ngược về 0
        checkTimeTask = new TimerTask() {
            @Override
            public void run() {
                int remaining = timeTask.getTimeRemaining();
                System.out.println(username + " Server Time remaining: " + remaining);

                if (remaining <= 0) {
                    stopAllTimers(); // Hủy tất cả các timer
                    System.out.println(username + " Countdown finished.");

                    // Hết thời gian vẫn chưa bắn (trong lúc xếp)
                    if (!gameCtr.isShot() && gameCtr.isPlayerTurn()) {
                        gameCtr.setCntMissTurn(gameCtr.getCntMissTurn() + 1);
                        // cả 2 bỏ liên tục 3 lượt -> hoà
                        if (gameCtr.getCntMissTurn() == 3 && enemy.gameCtr.getCntMissTurn() == 3) {
                            result = "draw";
                            enemy.result = "draw";
                            // update result match ở đây (DAO)
                            Match match = new Match(username, enemy.username, "draw", "draw", 0, 0);
                            matchDAO.updateMatchResult(match);

                            playerDAO.updateDraw(username);
                            playerDAO.updateDraw(enemy.username);
                            sendData(new ObjectWrapper(ObjectWrapper.SERVER_TRANSFER_END_GAME_DRAW));
                            enemy.sendData(new ObjectWrapper(ObjectWrapper.SERVER_TRANSFER_END_GAME_DRAW));
                        } 
                        else {
                            System.out.println("Server, người chưa bắn là: " + username);
                            sendData(new ObjectWrapper(ObjectWrapper.SERVER_TRANSFER_SHOOT_MISS_TURN));
                            enemy.sendData(new ObjectWrapper(ObjectWrapper.SERVER_TRANSFER_SHOOT_MISS_TURN));
                            gameCtr.setShot(false);
                            gameCtr.setPlayerTurn(false);
                            enemy.gameCtr.setPlayerTurn(true);
                            enemy.countDownPlay(18);
                        }
                    }
                }
            }
        };

        // Khởi chạy task kiểm tra mà không ảnh hưởng đến ServerProcessing
        checkTimer = new Timer();
        checkTimer.scheduleAtFixedRate(checkTimeTask, 0, 1000);
    }

    // Phương thức để hủy tất cả các timer
    public void stopAllTimers() {
        if (timer != null) {
            timer.cancel();
            timeTask.cancel();
            timer = null;
        }
        if (checkTimer != null) {
            checkTimer.cancel();
            checkTimeTask.cancel();
            checkTimer = null;
        }
    }
    
    // Image Quiz Game Methods
    private void startImageQuizRound() {
        if (imageQuizGameCtr == null || enemy == null) {
            System.out.println("Server: Cannot start round - imageQuizGameCtr or enemy is null");
            return;
        }
        
        // Ensure round state is reset for new round
        synchronized (imageQuizGameCtr) {
            // Reset round processed flag for the new round
            // This must be done BEFORE starting the round to ensure first answer can trigger processing
            imageQuizGameCtr.resetRoundProcessed();
            System.out.println("Server: Reset roundProcessed flag for new round " + imageQuizGameCtr.getCurrentRoundNumber());
        }
        
        ImageQuestionManager.ImageQuestion currentQuestion = imageQuizGameCtr.getCurrentRoundQuestion();
        if (currentQuestion == null) {
            System.out.println("Server: Cannot start round - currentQuestion is null, currentRound: " + imageQuizGameCtr.getCurrentRoundNumber());
            return;
        }
        
        int roundNumber = imageQuizGameCtr.getCurrentRoundNumber();
        byte[] imageBytes = currentQuestion.getImageBytes();
        String question = currentQuestion.getQuestion();
        
        System.out.println("Server: Starting round " + roundNumber + " for " + username + " and " + enemy.username);
        System.out.println("Server: Question: " + question + ", Image bytes: " + (imageBytes != null ? imageBytes.length : "null"));
        System.out.println("Server: hasAnyAnswer() = " + imageQuizGameCtr.hasAnyAnswer() + ", roundProcessed = " + imageQuizGameCtr.isRoundProcessed());
        
        // Create round data object
        Object[] roundData = new Object[3];
        roundData[0] = roundNumber;
        roundData[1] = imageBytes;
        roundData[2] = question;
        
        // Send round data to both players
        System.out.println("Server: Sending round data to " + username);
        sendData(new ObjectWrapper(ObjectWrapper.SERVER_SEND_ROUND_DATA, roundData));
        System.out.println("Server: Sending round data to " + enemy.username);
        enemy.sendData(new ObjectWrapper(ObjectWrapper.SERVER_SEND_ROUND_DATA, roundData));
        
        // Start 15-second countdown for the round
        countDownImageQuizRound(15);
    }
    
    private void countDownImageQuizRound(int timeRemaining) {
        stopAllTimers();
        timeTask = new CountDownTimer(timeRemaining);
        timer = new Timer();
        timer.scheduleAtFixedRate(timeTask, 0, 1000);
        
        checkTimeTask = new TimerTask() {
            @Override
            public void run() {
                int remaining = timeTask.getTimeRemaining();
                
                if (remaining <= 0) {
                    stopAllTimers();
                    // Round time is up, process the round if not already processed
                    if (imageQuizGameCtr != null && enemy != null && !imageQuizGameCtr.isRoundProcessed()) {
                        System.out.println("Server: Round time expired, processing round");
                        processImageQuizRound();
                    }
                }
            }
        };
        
        checkTimer = new Timer();
        checkTimer.scheduleAtFixedRate(checkTimeTask, 0, 1000);
    }
    
    private void processImageQuizRound() {
        if (imageQuizGameCtr == null || enemy == null) {
            System.out.println("Server: Cannot process round - imageQuizGameCtr or enemy is null");
            return;
        }
        
        // Prevent processing the same round multiple times
        synchronized (imageQuizGameCtr) {
            // Check if round was already processed
            if (imageQuizGameCtr.isRoundProcessed()) {
                System.out.println("Server: Round already processed, skipping");
                return;
            }
            
            stopAllTimers();
            
            ImageQuizGameCtr.RoundResult roundResult = imageQuizGameCtr.processRound();
            if (roundResult == null) {
                System.out.println("Server: Round result is null");
                return;
            }
            
            // Mark round as processed
            imageQuizGameCtr.markRoundProcessed();
            
            // Convert RoundResult to Object[] for serialization
            Object[] resultData = new Object[10];
            resultData[0] = roundResult.roundNumber;
            resultData[1] = roundResult.correctAnswer;
            resultData[2] = roundResult.player1Correct;
            resultData[3] = roundResult.player2Correct;
            resultData[4] = roundResult.player1Score;
            resultData[5] = roundResult.player2Score;
            resultData[6] = roundResult.finalPlayer1Score;
            resultData[7] = roundResult.finalPlayer2Score;
            resultData[8] = roundResult.gameFinished;
            resultData[9] = roundResult.winner;
            
            System.out.println("Server: Sending round result - Round " + roundResult.roundNumber + 
                             ", P1 score: " + roundResult.player1Score + 
                             ", P2 score: " + roundResult.player2Score + 
                             ", Finished: " + roundResult.gameFinished);
            
            // Send round result to both players
            sendData(new ObjectWrapper(ObjectWrapper.SERVER_SEND_ROUND_RESULT, resultData));
            enemy.sendData(new ObjectWrapper(ObjectWrapper.SERVER_SEND_ROUND_RESULT, resultData));
            
            // Check if game is finished
            if (roundResult.gameFinished) {
                // Game is finished, determine winner and update database
                String winner = roundResult.winner;
                String loser = roundResult.loser;
                
                if (winner.equals("draw")) {
                    this.result = "draw";
                    enemy.result = "draw";
                    Match match = new Match(this.username, enemy.username, "draw", "draw", 0, 0);
                    matchDAO.updateMatchResult(match);
                    playerDAO.updateDraw(this.username);
                    playerDAO.updateDraw(enemy.username);
                } else {
                    if (winner.equals(this.username)) {
                        this.result = "win";
                        enemy.result = "loss";
                    } else {
                        this.result = "loss";
                        enemy.result = "win";
                    }
                    
                    Match match = new Match(winner, loser, "win", "loss", 1, 0);
                    matchDAO.updateMatchResult(match);
                    playerDAO.updateWin(winner);
                    playerDAO.updateLoss(loser);
                }
                
                // Send end game message to both players
                Object[] endGameData = new Object[4];
                endGameData[0] = roundResult.finalPlayer1Score;
                endGameData[1] = roundResult.finalPlayer2Score;
                endGameData[2] = winner;
                endGameData[3] = loser;
                
                sendData(new ObjectWrapper(ObjectWrapper.SERVER_END_IMAGE_QUIZ_GAME, endGameData));
                enemy.sendData(new ObjectWrapper(ObjectWrapper.SERVER_END_IMAGE_QUIZ_GAME, endGameData));
                
                // Stop all timers
                stopAllTimers();
                if (enemy != null) {
                    enemy.stopAllTimers();
                }
                
                // Reset game state for next game (but keep enemy reference and isInviter flag)
                // These will be reset when players go back to main form
                imageQuizGameCtr = null;
                enemy.imageQuizGameCtr = null;
                System.out.println("Server: Game finished, reset game state for " + username + " and " + enemy.username);
                System.out.println("Server: Enemy reference maintained: " + (enemy != null ? enemy.username : "null") + 
                                 ", isInviter: " + isInviter);
            } else {
                // Move to next round after a short delay (2 seconds)
                Timer delayTimer = new Timer();
                delayTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        startImageQuizRound();
                    }
                }, 2000);
            }
        }
    }
}
