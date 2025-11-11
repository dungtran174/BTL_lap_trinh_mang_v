package server.dao;

import shared.dto.PlayerHistory;
import shared.model.Player;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class PlayerDAO extends DAO {

    public PlayerDAO() {
        super();
    }

    public Player getPlayer(String username) {
        Player player = null;
        String sql = "SELECT * FROM players WHERE username = ?";
        try {
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                player = new Player();
                player.setUsername(rs.getString("username"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return player;
    }
    
    public void CreateAccount(Player player){
        // Database structure: username (PK), password, points (DEFAULT 0), total_wins (DEFAULT 0), 
        // total_losses (DEFAULT 0), total_afk (DEFAULT 0), total_draw (DEFAULT 0)
        String sql = "INSERT INTO players (username, password, points, total_wins, total_losses, total_afk, total_draw) VALUES(?,?,?,?,?,?,?)";
        try {
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, player.getUsername());
            ps.setString(2, player.getPassword());
            ps.setInt(3, player.getPoints());
            ps.setInt(4, player.getTotalWins());
            ps.setInt(5, player.getTotalLosses());
            ps.setInt(6, player.getTotalAfk());
            ps.setInt(7, player.getTotalDraw());
            
            ps.executeUpdate();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ArrayList<Player> getAllUser(){
        ArrayList<Player> list = new ArrayList<>();

        String sql = "SELECT * FROM players";
        try {
            PreparedStatement ps = con.prepareStatement(sql);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String username = rs.getString("username");
                Player player = new Player();
                player.setUsername(username);
                list.add(player);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    public boolean checkExistAccount(Player player){
        boolean result = false;
        String sql = "SELECT username FROM players WHERE username = ?";
        try {
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, player.getUsername());

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                result = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
    
    public String checkLogin(Player player) {
        boolean result = false;
        System.out.println(player.getUsername() + " " + player.getPassword());
        String sql = "SELECT username, password, points FROM players WHERE username = ? AND password = ?";
        try {
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, player.getUsername());
            ps.setString(2, player.getPassword());

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                // Handle both INT and DECIMAL types - round to nearest int for display
                double pointsValue = rs.getDouble("points");
                int pointsRounded = (int) Math.round(pointsValue);
                return String.valueOf(pointsRounded);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "false";
    }

    public boolean updateAfk(String username) {
        String sql = "UPDATE players SET total_afk = total_afk + 1, points = points - 1 WHERE username = ?";
        try {
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, username);
            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean updateDraw(String username) {
        String sql = "UPDATE players SET total_draw = total_draw + 1, points = points + 0.5 WHERE username = ?";
        try {
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, username);
            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean updateWin(String username) {
        String sql = "UPDATE players SET total_wins = total_wins + 1, points = points + 1 WHERE username = ?";
        try {
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, username);
            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean updateLoss(String username) {
        String sql = "UPDATE players SET total_losses = total_losses + 1, points = points - 1 WHERE username = ?";
        try {
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, username);
            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    
    // Chỉ cộng điểm, không cộng total_wins (dùng khi đối thủ rời trận)
    public boolean updatePointsOnly(String username, int pointsChange) {
        String sql = "UPDATE players SET points = points + ? WHERE username = ?";
        try {
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, pointsChange);
            ps.setString(2, username);
            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    
    // Chỉ tăng total_losses, không trừ điểm (dùng khi đã trừ điểm từ AFK)
    public boolean updateLossOnly(String username) {
        String sql = "UPDATE players SET total_losses = total_losses + 1 WHERE username = ?";
        try {
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, username);
            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    
    // Lấy thông tin người chơi

    public PlayerHistory getPlayerInfo(String username) {
        PlayerHistory playerHistory = null;
        String sql = "SELECT points, total_wins, total_losses, total_afk, total_draw FROM players WHERE username = ?";
        try {
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                // Handle both INT and DECIMAL types - round to nearest int for display
                double pointsValue = rs.getDouble("points");
                int points = (int) Math.round(pointsValue);
                int totalWins = rs.getInt("total_wins");
                int totalLosses = rs.getInt("total_losses");
                int totalAfk = rs.getInt("total_afk");
                int totalDraw = rs.getInt("total_draw");
                int ranking = calculateRanking(username);
                playerHistory = new PlayerHistory(ranking, points, totalWins, totalLosses, totalAfk, totalDraw);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return playerHistory;
    }

    // Tính toán thứ hạng người chơi dựa trên điểm và số lần AFK
    private int calculateRanking(String username) {
        String sql = "SELECT username FROM players ORDER BY points DESC, total_afk ASC";
        int rank = 1;
        try {
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                if (rs.getString("username").equals(username)) {
                    break;
                }
                rank++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rank;
    }

    // Lấy danh sách bảng xếp hạng tất cả người chơi
    public List<PlayerHistory> getLeaderboard() {
        List<PlayerHistory> leaderboard = new ArrayList<>();
        String sql = "SELECT username, points, total_wins, total_losses, total_afk, total_draw FROM players ORDER BY points DESC, total_afk ASC, total_wins DESC, total_losses DESC";
        try {
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            int rank = 1;
            while (rs.next()) {
                String username = rs.getString("username");
                // Handle both INT and DECIMAL types - round to nearest int for display
                double pointsValue = rs.getDouble("points");
                int points = (int) Math.round(pointsValue);
                int totalWins = rs.getInt("total_wins");
                int totalLosses = rs.getInt("total_losses");
                int totalAfk = rs.getInt("total_afk");
                int totalDraw = rs.getInt("total_draw");
                leaderboard.add(new PlayerHistory(username, rank, points, totalWins, totalLosses, totalAfk, totalDraw));
                rank++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return leaderboard;
    }
}
