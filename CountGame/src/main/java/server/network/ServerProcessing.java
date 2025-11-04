package server.network;

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

    private ImageQuizGameCtr imageQuizGameCtr; // Image quiz game controller
    private boolean isInviter = false; // Track if this player is the inviter
    
    private CountDownTimer timeTask;
    private Timer timer;
    private Timer checkTimer;
    private TimerTask checkTimeTask;

    private String result; // win, loss, afk, cancelled
    
    // Play Again variables
    private Boolean playAgainResponse = null; // null = not answered, true = yes, false = no
    private volatile boolean playAgainProcessing = false; // Flag to prevent multiple calls to handlePlayAgainDecision

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
                            // Kiểm tra xem tài khoản đã được sử dụng chưa
                            boolean accountInUse = false;
                            for (ServerProcessing sp : serverCtr.getMyProcess()) {
                                if (sp.isOnline && sp.username != null && sp.username.equals(player.getUsername())) {
                                    accountInUse = true;
                                    break;
                                }
                            }
                            
                            if (accountInUse) {
                                // Tài khoản đã được sử dụng
                                sendData(new ObjectWrapper(ObjectWrapper.LOGIN_ACCOUNT_IN_USE, "This account is already in use. Please use another account."));
                            } else {
                                // Kiểm tra username/password trong database
                                String loginResult = playerDAO.checkLogin(player);
                                sendData(new ObjectWrapper(ObjectWrapper.SERVER_LOGIN_USER, loginResult));
                            }
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
                                
                                // Disable input immediately for both players if this is the first answer
                                if (wasFirstAnswer && !imageQuizGameCtr.isRoundProcessed()) {
                                    System.out.println("Server: First answer received from " + this.username + " in round " + 
                                                     imageQuizGameCtr.getCurrentRoundNumber() + ", disabling input for both players");
                                    // Send disable input signal to both players immediately
                                    sendData(new ObjectWrapper(ObjectWrapper.SERVER_DISABLE_INPUT));
                                    enemy.sendData(new ObjectWrapper(ObjectWrapper.SERVER_DISABLE_INPUT));
                                    
                                    // Process round immediately after disabling input
                                    processImageQuizRound();
                                }
                            }
                            break;
                        case ObjectWrapper.CLIENT_PLAY_AGAIN_RESPONSE:
                            if (enemy != null) {
                                Boolean wantsPlayAgain = (Boolean) data.getData();
                                this.playAgainResponse = wantsPlayAgain;
                                System.out.println("Server: " + this.username + " play again response: " + wantsPlayAgain);
                                System.out.println("Server: " + this.username + " - isInviter=" + isInviter + 
                                                 ", enemy=" + enemy.username + ", enemy.isInviter=" + enemy.isInviter);
                                
                                // Notify opponent based on response
                                if (wantsPlayAgain) {
                                    // Player chose YES - notify opponent if they haven't responded yet
                                    if (enemy.playAgainResponse == null) {
                                        System.out.println("Server: " + this.username + " chose YES, notifying " + enemy.username + " that opponent is waiting");
                                        enemy.sendData(new ObjectWrapper(ObjectWrapper.SERVER_OPPONENT_WAITING_RESPONSE));
                                    }
                                } else {
                                    // Player chose NO - notify opponent immediately and close their dialog
                                    System.out.println("Server: " + this.username + " chose NO, notifying " + enemy.username + " that opponent declined");
                                    enemy.sendData(new ObjectWrapper(ObjectWrapper.SERVER_OPPONENT_DECLINED_PLAY_AGAIN));
                                }
                                
                                // Check if both players have responded
                                if (this.playAgainResponse != null && enemy.playAgainResponse != null) {
                                    System.out.println("Server: Both players responded - " + this.username + " is the last responder");
                                    // The thread that receives the last response (making both responses available) should call handlePlayAgainDecision()
                                    // Use synchronized to ensure only one thread processes it
                                    // Prefer inviter, but if inviter already processed or this is non-inviter's thread, handle it here
                                    synchronized (this) {
                                        synchronized (enemy) {
                                            // Check if already being processed
                                            if (!this.playAgainProcessing && !enemy.playAgainProcessing) {
                                                System.out.println("Server: " + this.username + " (last responder) calling handlePlayAgainDecision()");
                                                handlePlayAgainDecision();
                                            } else {
                                                System.out.println("Server: Play again decision already being processed, skipping");
                                            }
                                        }
                                    }
                                } else {
                                    System.out.println("Server: Waiting for other player's response. " + 
                                                     this.username + ": " + this.playAgainResponse + 
                                                     ", " + (enemy != null ? enemy.username : "null") + ": " + 
                                                     (enemy != null ? enemy.playAgainResponse : "null"));
                                }
                            }
                            break;
                            
                        case ObjectWrapper.CLIENT_LEAVE_GAME:
                            // Handle player leaving game mid-match
                            if (inGame && enemy != null && imageQuizGameCtr != null) {
                                System.out.println("Server: " + this.username + " is leaving the game mid-match");
                                
                                // Stop all timers
                                stopAllTimers();
                                enemy.stopAllTimers();
                                
                                // Update AFK for the player who left (-1 điểm, total_afk + 1)
                                playerDAO.updateAfk(this.username);
                                
                                // Cộng điểm cho người còn lại (+1 điểm, KHÔNG cộng total_wins)
                                playerDAO.updatePointsOnly(enemy.username, 1);
                                
                                // Không cập nhật loss cho người rời trận (vì AFK đã đủ)
                                
                                // Create match record - người còn lại cộng 1 điểm
                                Match match = new Match(enemy.username, this.username, "win", "afk", 1, -1);
                                matchDAO.updateMatchResult(match);
                                
                                // Reset game state
                                imageQuizGameCtr = null;
                                enemy.imageQuizGameCtr = null;
                                
                                // Set results
                                this.result = "afk";
                                enemy.result = "win"; // Người còn lại thắng nhưng không cộng điểm
                                
                                // Người rời trận: về home
                                sendData(new ObjectWrapper(ObjectWrapper.SERVER_PLAYER_LEFT_GAME, this.username));
                                
                                // Người còn lại: chuyển về Result screen với kết quả
                                // Gửi result data: "win||" + username của người rời trận
                                enemy.sendData(new ObjectWrapper(ObjectWrapper.SERVER_OPPONENT_LEFT_SHOW_RESULT, "win||" + this.username));
                                
                                // Reset in-game status
                                inGame = false;
                                enemy.inGame = false;
                                
                                // Update waiting list
                                serverCtr.sendWaitingList();
                                
                                System.out.println("Server: " + this.username + " left the game, " + enemy.username + " wins (+1 point)");
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
            // Client disconnected or error occurred
            System.out.println("Server: Client disconnected or error: " + (username != null ? username : "unknown"));
            // Set offline status
            isOnline = false;
            // Update waiting list
            if (serverCtr != null) {
                serverCtr.sendWaitingList();
            }
            // Remove from server processing list
            if (serverCtr != null) {
                serverCtr.removeServerProcessing(this);
            }
            try {
                if (mySocket != null && !mySocket.isClosed()) {
                    mySocket.close();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            isRunning = false;
        } finally {
            // Set offline status when client disconnects
            isOnline = false;
            // Update waiting list
            if (serverCtr != null) {
                serverCtr.sendWaitingList();
            }
            serverCtr.removeServerProcessing(this);
            if (inGame && enemy != null) {
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
                    // Round time is up, disable input and process the round if not already processed
                    if (imageQuizGameCtr != null && enemy != null && !imageQuizGameCtr.isRoundProcessed()) {
                        System.out.println("Server: Round time expired, disabling input and processing round");
                        // Disable input for both players when time expires
                        sendData(new ObjectWrapper(ObjectWrapper.SERVER_DISABLE_INPUT));
                        enemy.sendData(new ObjectWrapper(ObjectWrapper.SERVER_DISABLE_INPUT));
                        // Process round after disabling input
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
            // Include player1Username and player2Username so clients can determine their role
            Object[] resultData = new Object[12];
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
            resultData[10] = imageQuizGameCtr.player1Username; // Add player1 username
            resultData[11] = imageQuizGameCtr.player2Username; // Add player2 username
            
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
                // These will be reset when players go back to main form OR when starting new game
                imageQuizGameCtr = null;
                enemy.imageQuizGameCtr = null;
                System.out.println("Server: Game finished, reset game state for " + username + " and " + enemy.username);
                System.out.println("Server: Enemy reference maintained: " + (enemy != null ? enemy.username : "null") + 
                                 ", isInviter: " + isInviter);
                
                // After game ends, ask both players if they want to play again
                Timer askPlayAgainTimer = new Timer();
                askPlayAgainTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        if (enemy != null) {
                            // Reset play again responses
                            playAgainResponse = null;
                            enemy.playAgainResponse = null;
                            
                            // Ask both players
                            sendData(new ObjectWrapper(ObjectWrapper.SERVER_ASK_PLAY_AGAIN));
                            enemy.sendData(new ObjectWrapper(ObjectWrapper.SERVER_ASK_PLAY_AGAIN));
                            System.out.println("Server: Asking both players if they want to play again");
                        }
                    }
                }, 1000); // Wait 2 seconds after game ends before asking
            } else {
                // Move to next round after a delay (0.5 seconds to show answer)
                Timer delayTimer = new Timer();
                delayTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        startImageQuizRound();
                    }
                }, 500); // 0.5 seconds to show correct answer
            }
        }
    }
    
    private void handlePlayAgainDecision() {
        if (enemy == null) {
            System.err.println("Server: handlePlayAgainDecision() called but enemy is null for " + username);
            return;
        }
        
        // Prevent multiple calls - use synchronized check on both objects
        synchronized (this) {
            synchronized (enemy) {
                // Check if already processing
                if (this.playAgainProcessing || enemy.playAgainProcessing) {
                    System.out.println("Server: handlePlayAgainDecision() already being processed, skipping duplicate call");
                    return;
                }
                
                // Set processing flag
                this.playAgainProcessing = true;
                enemy.playAgainProcessing = true;
                
                System.out.println("Server: handlePlayAgainDecision() - " + username + ": isInviter=" + isInviter + 
                                 ", playAgainResponse=" + playAgainResponse + 
                                 ", enemy=" + enemy.username + 
                                 ", enemy.isInviter=" + enemy.isInviter + 
                                 ", enemy.playAgainResponse=" + enemy.playAgainResponse);
                
                // Check if both want to play again
                if (this.playAgainResponse != null && this.playAgainResponse && 
                    enemy.playAgainResponse != null && enemy.playAgainResponse) {
                    System.out.println("Server: Both players want to play again, starting new game");
                    
                    // Reset play again responses
                    this.playAgainResponse = null;
                    enemy.playAgainResponse = null;
                    
                    // Notify both clients to start new game
                    sendData(new ObjectWrapper(ObjectWrapper.SERVER_START_NEW_GAME));
                    enemy.sendData(new ObjectWrapper(ObjectWrapper.SERVER_START_NEW_GAME));
                    
                    System.out.println("Server: SERVER_START_NEW_GAME sent to both players");
                    System.out.println("Server: Checking inviter - " + username + ": isInviter=" + isInviter + 
                                     ", " + enemy.username + ": isInviter=" + enemy.isInviter);
                    
                    // Keep enemy reference and start new game
                    // Create new game controller - only inviter creates it
                    // IMPORTANT: Only create game controller once, from the thread of the inviter
                    if (isInviter) {
                        System.out.println("Server: " + this.username + " is inviter, creating new game controller");
                        
                        // Double check enemy reference is still valid
                        if (enemy == null || enemy.imageQuizGameCtr != null) {
                            System.err.println("Server: ERROR - enemy is null or already has game controller");
                            return;
                        }
                        
                        imageQuizGameCtr = new ImageQuizGameCtr(this.username, enemy.username);
                        enemy.imageQuizGameCtr = imageQuizGameCtr;
                        
                        System.out.println("Server: Game controller created. Player1: " + imageQuizGameCtr.player1Username + ", Player2: " + imageQuizGameCtr.player2Username);
                        
                        // Start the first round after a short delay
                        Timer startGameTimer = new Timer();
                        startGameTimer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                System.out.println("Server: Starting first round of new game for " + username + " and " + enemy.username);
                                System.out.println("Server: imageQuizGameCtr is " + (imageQuizGameCtr != null ? "not null" : "null"));
                                System.out.println("Server: enemy.imageQuizGameCtr is " + (enemy != null && enemy.imageQuizGameCtr != null ? "not null" : "null"));
                                
                                if (imageQuizGameCtr != null && enemy != null) {
                                    startImageQuizRound();
                                } else {
                                    System.err.println("Server: ERROR - Cannot start round, imageQuizGameCtr or enemy is null");
                                }
                            }
                        }, 1000);
                    } else if (enemy.isInviter) {
                        System.out.println("Server: " + enemy.username + " is inviter, they will create game controller");
                        // Enemy will create the game controller and start the round in their handlePlayAgainDecision()
                    } else {
                        System.err.println("Server: ERROR - Neither player is inviter! " + username + ": isInviter=" + isInviter + 
                                         ", " + enemy.username + ": isInviter=" + enemy.isInviter);
                        // Fallback: make the first player the inviter
                        System.out.println("Server: Making " + username + " the inviter as fallback");
                        this.isInviter = true;
                        enemy.isInviter = false;
                        
                        imageQuizGameCtr = new ImageQuizGameCtr(this.username, enemy.username);
                        enemy.imageQuizGameCtr = imageQuizGameCtr;
                        
                        Timer startGameTimer = new Timer();
                        startGameTimer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                if (imageQuizGameCtr != null && enemy != null) {
                                    startImageQuizRound();
                                }
                            }
                        }, 1000);
                    }
                    
                } else {
                    // At least one player declined
                    System.out.println("Server: At least one player declined play again");
                    System.out.println("Server: " + username + " response: " + playAgainResponse + 
                                     ", " + enemy.username + " response: " + enemy.playAgainResponse);
                    
                    // Reset play again responses
                    this.playAgainResponse = null;
                    enemy.playAgainResponse = null;
                    
                    // Notify both clients
                    sendData(new ObjectWrapper(ObjectWrapper.SERVER_PLAY_AGAIN_DECLINED));
                    enemy.sendData(new ObjectWrapper(ObjectWrapper.SERVER_PLAY_AGAIN_DECLINED));
                }
                
                // Reset processing flag
                this.playAgainProcessing = false;
                enemy.playAgainProcessing = false;
            }
        }
    }
}
