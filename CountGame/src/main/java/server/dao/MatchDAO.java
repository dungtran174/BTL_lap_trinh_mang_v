package server.dao;

import shared.model.Match;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static server.dao.DAO.con;

public class MatchDAO {

    public MatchDAO() {
        super();
    }

    public boolean updateMatchResult(Match match) {
        // Database structure: match_id (AUTO_INCREMENT), user1_username, user2_username, 
        // timestamp (DEFAULT CURRENT_TIMESTAMP), result_user1, result_user2, points_change_user1, points_change_user2
        String sql = "INSERT INTO matches (user1_username, user2_username, result_user1, result_user2, points_change_user1, points_change_user2) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        try {
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, match.getUser1Username());
            ps.setString(2, match.getUser2Username());
            ps.setString(3, match.getResultUser1());
            ps.setString(4, match.getResultUser2());
            ps.setInt(5, match.getPointsChangeUser1());
            ps.setInt(6, match.getPointsChangeUser2());

            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<Match> getMatchHistory(String username) {
        List<Match> listMatch = new ArrayList<>();
        String sql = "SELECT user1_username, user2_username, timestamp, result_user1, result_user2, points_change_user1, points_change_user2 "
                + "FROM matches WHERE user1_username = ? OR user2_username = ?";
        try {
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, username);
            ps.setString(2, username);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String user1Username = rs.getString("user1_username");
                String user2Username = rs.getString("user2_username");
                // Lấy timestamp dưới dạng String để tránh lỗi múi giờ
                String timestampStr = rs.getString("timestamp");
                LocalDateTime timestamp = LocalDateTime.parse(timestampStr.replace(" ", "T"));

                // Xác định vai trò của `username` trong trận đấu và thiết lập thông tin tương ứng
                String enemy;
                String result;
                int pointsChange;

                if (username.equals(user1Username)) {
                    enemy = user2Username;
                    result = rs.getString("result_user1");
                    pointsChange = rs.getInt("points_change_user1");
                } else {
                    enemy = user1Username;
                    result = rs.getString("result_user2");
                    pointsChange = rs.getInt("points_change_user2");
                }

                listMatch.add(new Match(enemy, timestamp, result, pointsChange));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return listMatch;
    }

}
