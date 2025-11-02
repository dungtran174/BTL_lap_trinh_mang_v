package server.controller;

import java.util.ArrayList;
import java.util.List;

public class ImageQuizGameCtr {
    
    private List<ImageQuestionManager.ImageQuestion> rounds;
    private double player1Score;
    private double player2Score;
    private int currentRound;
    private boolean player1Answered;
    private boolean player2Answered;
    private int player1Answer;
    private int player2Answer;
    private boolean gameFinished;
    private boolean roundProcessed; // Flag to prevent processing same round multiple times
    private String player1Username;
    private String player2Username;
    
    public ImageQuizGameCtr(String player1Username, String player2Username) {
        this.player1Username = player1Username;
        this.player2Username = player2Username;
        this.player1Score = 0.0;
        this.player2Score = 0.0;
        this.currentRound = 0;
        this.player1Answered = false;
        this.player2Answered = false;
        this.gameFinished = false;
        this.roundProcessed = false;
        
        // Load 3 random images/questions for the 3 rounds
        ImageQuestionManager manager = ImageQuestionManager.getInstance();
        this.rounds = manager.getRandomImageQuestions(3);
    }
    
    public ImageQuestionManager.ImageQuestion getCurrentRoundQuestion() {
        if (currentRound < rounds.size()) {
            return rounds.get(currentRound);
        }
        return null;
    }
    
    public void submitAnswer(String username, int answer) {
        if (gameFinished || currentRound >= rounds.size()) {
            return;
        }
        
        if (username.equals(player1Username) && !player1Answered) {
            player1Answer = answer;
            player1Answered = true;
        } else if (username.equals(player2Username) && !player2Answered) {
            player2Answer = answer;
            player2Answered = true;
        }
    }
    
    public RoundResult processRound() {
        if (gameFinished || currentRound >= rounds.size()) {
            return null;
        }
        
        ImageQuestionManager.ImageQuestion currentQuestion = rounds.get(currentRound);
        int correctAnswer = currentQuestion.getAnswer();
        
        RoundResult result = new RoundResult();
        result.roundNumber = currentRound + 1;
        result.correctAnswer = correctAnswer;
        
        // Check who answered first
        boolean player1AnsweredFirst = player1Answered && !player2Answered;
        boolean player2AnsweredFirst = player2Answered && !player1Answered;
        
        // Check player1 answer - process immediately
        if (player1Answered) {
            if (player1Answer == correctAnswer) {
                // Player1 answered correctly
                player1Score += 1.0;
                result.player1Correct = true;
                result.player1First = player1AnsweredFirst;
                result.player1Score = player1Score;
            } else {
                // Player1 answered incorrectly
                player1Score -= 0.5;
                result.player1Correct = false;
                result.player1Score = player1Score;
            }
        } else {
            // Player1 didn't answer - score remains unchanged
            result.player1Correct = false;
            result.player1Score = player1Score;
        }
        
        // Check player2 answer - process immediately
        if (player2Answered) {
            if (player2Answer == correctAnswer) {
                // Player2 answered correctly
                player2Score += 1.0;
                result.player2Correct = true;
                result.player2First = player2AnsweredFirst;
                result.player2Score = player2Score;
            } else {
                // Player2 answered incorrectly
                player2Score -= 0.5;
                result.player2Correct = false;
                result.player2Score = player2Score;
            }
        } else {
            // Player2 didn't answer - score remains unchanged
            result.player2Correct = false;
            result.player2Score = player2Score;
        }
        
        // Move to next round (regardless of whether both answered)
        // Reset answer flags for next round
        player1Answered = false;
        player2Answered = false;
        currentRound++;
        // Note: roundProcessed flag is reset by markRoundProcessed() being called AFTER this,
        // then it will be reset when starting the next round
        
        // Check if game is finished
        if (currentRound >= rounds.size()) {
            gameFinished = true;
            result.gameFinished = true;
            
            // Determine winner
            if (player1Score > player2Score) {
                result.winner = player1Username;
                result.loser = player2Username;
            } else if (player2Score > player1Score) {
                result.winner = player2Username;
                result.loser = player1Username;
            } else {
                result.winner = "draw";
                result.loser = "draw";
            }
        }
        
        result.finalPlayer1Score = player1Score;
        result.finalPlayer2Score = player2Score;
        
        return result;
    }
    
    public boolean isRoundFinished() {
        return player1Answered && player2Answered;
    }
    
    public boolean hasAnyAnswer() {
        return player1Answered || player2Answered;
    }
    
    public boolean isGameFinished() {
        return gameFinished;
    }
    
    public int getCurrentRoundNumber() {
        return currentRound + 1;
    }
    
    public double getPlayer1Score() {
        return player1Score;
    }
    
    public double getPlayer2Score() {
        return player2Score;
    }
    
    public boolean isRoundProcessed() {
        return roundProcessed;
    }
    
    public void markRoundProcessed() {
        this.roundProcessed = true;
    }
    
    public void resetRoundProcessed() {
        this.roundProcessed = false;
    }
    
    public static class RoundResult {
        public int roundNumber;
        public int correctAnswer;
        public boolean player1Correct;
        public boolean player2Correct;
        public boolean player1First;
        public boolean player2First;
        public double player1Score;
        public double player2Score;
        public double finalPlayer1Score;
        public double finalPlayer2Score;
        public boolean gameFinished;
        public String winner;
        public String loser;
    }
}

