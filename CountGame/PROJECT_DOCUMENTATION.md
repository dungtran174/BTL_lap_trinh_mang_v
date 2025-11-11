# Tài Liệu Kỹ Thuật - Count Objects Game

## Mục Lục
1. [Tổng Quan Dự Án](#tổng-quan-dự-án)
2. [Kiến Trúc Kết Nối Client-Server](#kiến-trúc-kết-nối-client-server)
3. [Giao Thức Truyền Tin](#giao-thức-truyền-tin)
4. [Quản Lý Danh Sách Người Chơi](#quản-lý-danh-sách-người-chơi)
5. [Bảng Xếp Hạng](#bảng-xếp-hạng)
6. [Lịch Sử Đấu](#lịch-sử-đấu)
7. [Logic Chơi Game](#logic-chơi-game)
8. [Xử Lý Khi Người Chơi Thoát Trận](#xử-lý-khi-người-chơi-thoát-trận)
9. [Kết Thúc Trận Đấu](#kết-thúc-trận-đấu)
10. [Các Lỗi Còn Tồn Tại](#các-lỗi-còn-tồn-tại)
11. [Hướng Mở Rộng](#hướng-mở-rộng)

---

## Tổng Quan Dự Án

### Mô Tả
Count Objects Game là một game online đối kháng 2 người chơi, trong đó mỗi người chơi phải đếm số lượng vật thể trong hình ảnh được server gửi đến. Game được xây dựng bằng Java với JavaFX cho giao diện người dùng, sử dụng kiến trúc Client-Server với giao thức TCP/IP.

### Công Nghệ Sử Dụng
- **Backend**: Java (JDK 11+)
- **Frontend**: JavaFX
- **Database**: MySQL
- **Network**: TCP/IP Sockets
- **Build Tool**: Maven

### Cấu Trúc Thư Mục
```
CountGame/
├── src/main/java/
│   ├── client/          # Client-side code
│   │   ├── controller/  # ClientCtr - quản lý kết nối và luồng
│   │   ├── network/     # ClientListening - lắng nghe từ server
│   │   └── view/        # Các form JavaFX (Login, Main, Game, Result...)
│   ├── server/          # Server-side code
│   │   ├── controller/  # ServerCtr, ImageQuizGameCtr, ImageQuestionManager
│   │   ├── dao/         # PlayerDAO, MatchDAO - truy cập database
│   │   ├── network/     # ServerListening, ServerProcessing
│   │   └── view/        # ServerMainFrm - giao diện server
│   └── shared/           # Code dùng chung
│       ├── dto/         # ObjectWrapper, PlayerHistory, IPAddress
│       └── model/       # Player, Match
└── src/main/resources/
    ├── Fxml/            # FXML files cho JavaFX UI
    └── Styles/          # CSS files
```

---

## Kiến Trúc Kết Nối Client-Server

### 1. Kết Nối TCP/IP Socket

#### Server Side
- **File**: `server/network/ServerListening.java`
- **Cơ chế**: 
  - Server tạo `ServerSocket` tại port 8888 (mặc định) hoặc port tùy chỉnh
  - Server lắng nghe các kết nối mới trong vòng lặp `while(isListening)`
  - Khi có client kết nối, tạo `ServerProcessing` thread riêng cho mỗi client
  - Mỗi `ServerProcessing` quản lý một kết nối socket riêng biệt

```java
// ServerListening.java
ServerSocket serverSocket = new ServerSocket(port, 50, InetAddress.getByName(host));
while (isListening) {
    Socket clientSocket = serverSocket.accept();
    ServerProcessing sp = new ServerProcessing(clientSocket, serverCtr);
    sp.start(); // Mỗi client có thread riêng
    serverCtr.addServerProcessing(sp);
}
```

#### Client Side
- **File**: `client/controller/ClientCtr.java`
- **Cơ chế**:
  - Client tạo `Socket` kết nối đến server (IP: 26.161.164.36, Port: 8888 - IP Radmin VPN)
  - Tạo `ObjectOutputStream` để gửi dữ liệu
  - Tạo `ClientListening` thread để lắng nghe dữ liệu từ server

```java
// ClientCtr.java
mySocket = new Socket(serverAddress.getHost(), serverAddress.getPort());
this.oos = new ObjectOutputStream(mySocket.getOutputStream());
myListening = new ClientListening(instance);
myListening.start();
```

### 2. Giao Thức Truyền Dữ Liệu

#### ObjectWrapper - Wrapper Class
- **File**: `shared/dto/ObjectWrapper.java`
- **Mục đích**: Đóng gói tất cả các message giữa client và server
- **Cấu trúc**:
  ```java
  public class ObjectWrapper implements Serializable {
      private int performative;  // Loại message (constant)
      private Object data;        // Dữ liệu đi kèm
  }
  ```

#### Các Loại Message Chính
- **Login/Register**: `LOGIN_USER`, `SERVER_LOGIN_USER`, `REGISTER_USER`
- **User Management**: `GET_ALL_USER`, `SERVER_INFORM_CLIENT_WAITING`, `SERVER_INFORM_CLIENT_NUMBER`
- **Game Invitation**: `SEND_PLAY_REQUEST`, `ACCEPTED_PLAY_REQUEST`, `REJECTED_PLAY_REQUEST`
- **Game Play**: `START_IMAGE_QUIZ_GAME`, `SERVER_SEND_ROUND_DATA`, `SUBMIT_ANSWER`
- **Game Result**: `SERVER_SEND_ROUND_RESULT`, `SERVER_END_IMAGE_QUIZ_GAME`
- **Play Again**: `SERVER_ASK_PLAY_AGAIN`, `CLIENT_PLAY_AGAIN_RESPONSE`, `SERVER_START_NEW_GAME`
- **Leave Game**: `CLIENT_LEAVE_GAME`, `SERVER_PLAYER_LEFT_GAME`, `SERVER_OPPONENT_LEFT_SHOW_RESULT`

### 3. Luồng Xử Lý Dữ Liệu

#### Server Processing
- **File**: `server/network/ServerProcessing.java`
- **Cơ chế**:
  - Mỗi client có một thread `ServerProcessing` riêng
  - Thread lắng nghe dữ liệu từ client qua `ObjectInputStream`
  - Xử lý message dựa trên `performative` trong `ObjectWrapper`
  - Gửi response qua `sendData()` method

```java
// ServerProcessing.java - run() method
while (isRunning) {
    ObjectWrapper data = (ObjectWrapper) ois.readObject();
    switch (data.getPerformative()) {
        case ObjectWrapper.LOGIN_USER:
            // Xử lý login
            break;
        case ObjectWrapper.SUBMIT_ANSWER:
            // Xử lý câu trả lời
            break;
        // ... các case khác
    }
}
```

#### Client Listening
- **File**: `client/network/ClientListening.java`
- **Cơ chế**:
  - Thread `ClientListening` lắng nghe dữ liệu từ server
  - Phân loại message và route đến form tương ứng
  - Sử dụng `Platform.runLater()` để cập nhật UI thread-safe

```java
// ClientListening.java
ObjectWrapper data = (ObjectWrapper) ois.readObject();
switch (data.getPerformative()) {
    case ObjectWrapper.SERVER_SEND_ROUND_DATA:
        if (clientCtr.getImageQuizFrm() != null) {
            clientCtr.getImageQuizFrm().receivedDataProcessing(data);
        }
        break;
    // Route đến các form khác
}
```

---

## Giao Thức Truyền Tin

### 1. Serialization
- Tất cả dữ liệu được serialize qua `ObjectOutputStream` và `ObjectInputStream`
- `ObjectWrapper` implement `Serializable` để có thể truyền qua network
- Các object được serialize: `Player`, `Match`, `PlayerHistory`, `byte[]` (image), `Object[]`

### 2. Error Handling
- **Server**: Try-catch trong `sendData()` để xử lý `IOException` khi client disconnect
- **Client**: Try-catch trong `sendData()` để xử lý lỗi kết nối
- **Connection Loss**: 
  - Server phát hiện khi `readObject()` throw exception → set `isRunning = false`
  - Cleanup resources trong `finally` block

### 3. Thread Safety
- **Server**: Mỗi client có thread riêng, không có race condition giữa các client
- **Client**: JavaFX UI chỉ được cập nhật trong `Platform.runLater()`
- **Synchronization**: Sử dụng `synchronized` block khi xử lý game state (round processing, play again)

---

## Quản Lý Danh Sách Người Chơi

### 1. Cơ Chế Cập Nhật Danh Sách

#### Server Side
- **File**: `server/controller/ServerCtr.java` - `sendWaitingList()`
- **Logic**:
  1. Duyệt qua tất cả `ServerProcessing` trong `myProcess`
  2. Lọc các client `isOnline == true`
  3. Lấy thông tin `Player` từ database qua `getPlayer()`
  4. Set status: "Online" hoặc "In game" dựa trên `isInGame()`
  5. Gửi danh sách đến tất cả clients online

```java
public void sendWaitingList() {
    ArrayList<Player> listUsername = new ArrayList<>();
    for (ServerProcessing sp : myProcess) {
        if(sp != null && sp.isIsOnline()){
            Player player = sp.getPlayer();
            if (player != null) {
                if(sp.isInGame()) player.setStatus("In game");
                else player.setStatus("Online");
                listUsername.add(player);
            }
        }
    }
    // Gửi đến tất cả clients
    ObjectWrapper data = new ObjectWrapper(ObjectWrapper.SERVER_INFORM_CLIENT_WAITING, listUsername);
    for (ServerProcessing sp : myProcess) {
        if (sp != null && sp.isIsOnline()) {
            sp.sendData(data);
        }
    }
}
```

#### Khi Nào Cập Nhật?
- Khi client login thành công (`LOGIN_SUCCESSFUL`)
- Khi client logout (`EXIT_MAIN_FORM`)
- Khi client vào game (`SERVER_SET_GAME_READY`)
- Khi client quay về home (`BACK_TO_MAIN_FORM`)
- Khi client disconnect (trong `finally` block của `ServerProcessing.run()`)
- Khi client yêu cầu cập nhật (`UPDATE_WAITING_LIST_REQUEST`)

#### Client Side
- **File**: `client/view/MainFrm.java`
- **Xử lý**: 
  - Nhận `SERVER_INFORM_CLIENT_WAITING`
  - Cập nhật UI với danh sách user mới
  - Sắp xếp: Online > In Game > Offline, sau đó theo tên alphabet

```java
// Sắp xếp user theo status
listUserStatus.sort((o1, o2) -> {
    String status01 = mapUserStatus.get(o1);
    String status02 = mapUserStatus.get(o2);
    if (status01.equals(status02)) return o1.compareTo(o2);
    if(status01.equals("Online")) return -1;
    if(status01.equals("In Game") && status02.equals("Offline")) return -1;
    return 1;
});
```

### 2. Xử Lý Null và Exception
- Kiểm tra `sp != null` và `sp.isIsOnline()` trước khi truy cập
- Kiểm tra `player != null` trước khi gọi `getPlayer()`
- Try-catch khi gửi data để tránh crash khi client disconnect

---

## Bảng Xếp Hạng

### 1. Tiêu Chí Sắp Xếp

#### Database Query
- **File**: `server/dao/PlayerDAO.java` - `getLeaderboard()`
- **SQL Query**:
```sql
SELECT username, points, total_wins, total_losses, total_afk, total_draw 
FROM players 
ORDER BY points DESC, total_afk ASC, total_wins DESC, total_losses DESC
```

#### Thứ Tự Ưu Tiên
1. **Points (DESC)**: Điểm cao nhất trước
2. **Total_AFK (ASC)**: Số lần AFK ít nhất (nếu điểm bằng nhau)
3. **Total_Wins (DESC)**: Số trận thắng nhiều nhất
4. **Total_Losses (DESC)**: Số trận thua nhiều nhất (để phân biệt)

### 2. Phân Loại Rank

#### Client Side
- **File**: `client/view/RankingFrm.java`
- **Logic**:
  - **INTERN**: `points < 20`
  - **MASTER**: `20 <= points < 40`
  - **GRANDMASTER**: `40 <= points < 60`
  - **CHALLENGER**: `points >= 60`

```java
int point = playerRank.getPoints();
if (point < 20) {
    rank = "INTERN";
    listPlayerRankINTERN.add(playerRank);
} else if (point < 40) {
    rank = "MASTER";
    listPlayerRankMASTER.add(playerRank);
} else if (point < 60) {
    rank = "GRANDMASTER";
    listPlayerRankGRANDMASTER.add(playerRank);
} else {
    rank = "CHALLENGER";
    listPlayerRankCHALLENGER.add(playerRank);
}
```

### 3. Tính Rank (Thứ Hạng Tuyệt Đối)

#### Server Side
- **File**: `server/dao/PlayerDAO.java` - `calculateRanking()`
- **Logic**: Đếm số người chơi có điểm cao hơn hoặc bằng (nếu điểm bằng thì AFK ít hơn)

```java
private int calculateRanking(String username) {
    String sql = "SELECT username FROM players ORDER BY points DESC, total_afk ASC";
    int rank = 1;
    ResultSet rs = ps.executeQuery();
    while (rs.next()) {
        if (rs.getString("username").equals(username)) {
            break;
        }
        rank++;
    }
    return rank;
}
```

---

## Lịch Sử Đấu

### 1. Lưu Trữ Database

#### Database Schema
- **Table**: `matches`
- **Columns**:
  - `match_id` (AUTO_INCREMENT)
  - `user1_username` (VARCHAR)
  - `user2_username` (VARCHAR)
  - `timestamp` (DATETIME, DEFAULT CURRENT_TIMESTAMP)
  - `result_user1` (VARCHAR): "win", "loss", "draw", "afk"
  - `result_user2` (VARCHAR)
  - `points_change_user1` (INT): +1, -1, 0
  - `points_change_user2` (INT)

#### Insert Match Record
- **File**: `server/dao/MatchDAO.java` - `updateMatchResult()`
- **Khi nào lưu?**:
  - Khi game kết thúc bình thường (3 rounds)
  - Khi một người rời trận (AFK)
  - Khi game bị hủy (cancel)

```java
public boolean updateMatchResult(Match match) {
    String sql = "INSERT INTO matches (user1_username, user2_username, result_user1, result_user2, points_change_user1, points_change_user2) "
            + "VALUES (?, ?, ?, ?, ?, ?)";
    // Execute insert
}
```

### 2. Lấy Lịch Sử

#### Server Side
- **File**: `server/dao/MatchDAO.java` - `getMatchHistory()`
- **Query**:
```sql
SELECT user1_username, user2_username, timestamp, result_user1, result_user2, points_change_user1, points_change_user2 
FROM matches 
WHERE user1_username = ? OR user2_username = ?
```

#### Xác Định Vai Trò
- Nếu `username == user1_username`: enemy = `user2_username`, result = `result_user1`
- Nếu `username == user2_username`: enemy = `user1_username`, result = `result_user2`

#### Client Side
- **File**: `client/view/HistoryFrm.java`
- **Hiển thị**: 
  - Tên đối thủ
  - Kết quả (Win/Loss/Draw/AFK)
  - Thay đổi điểm (+1/-1/0)
  - Thời gian đấu

### 3. Ràng Buộc Dữ Liệu
- **Không có ràng buộc**: Lịch sử chỉ đơn giản là lấy từ database, không có filter hoặc limit
- **Có thể mở rộng**: Thêm pagination, filter theo thời gian, filter theo kết quả

---

## Logic Chơi Game

### 1. Khởi Tạo Game

#### Invitation Flow
1. Client A mời Client B (`SEND_PLAY_REQUEST`)
2. Server gửi request đến Client B (`RECEIVE_PLAY_REQUEST`)
3. Client B accept (`ACCEPTED_PLAY_REQUEST`)
4. Server gửi `SERVER_SET_GAME_READY` đến cả 2 clients
5. Cả 2 clients chuyển sang `WaitingFrm`

#### Start Game
- **File**: `server/network/ServerProcessing.java` - `START_IMAGE_QUIZ_GAME`
- **Điều kiện**: 
  - Chỉ `inviter` (người mời) mới có thể start game
  - Phải có `enemy != null` và `enemy.isOnline == true`
- **Logic**:
  1. Tạo `ImageQuizGameCtr` mới (shared giữa 2 players)
  2. Load 3 ảnh ngẫu nhiên từ `ImageQuestionManager`
  3. Gọi `startImageQuizRound()` để bắt đầu round 1

```java
if (isInviter && enemy != null && enemy.isOnline && !inGame) {
    // Tạo game controller
    imageQuizGameCtr = new ImageQuizGameCtr(this.username, enemy.username);
    enemy.imageQuizGameCtr = imageQuizGameCtr; // Shared instance
    
    // Start round 1
    startImageQuizRound();
}
```

### 2. Xử Lý Hình Ảnh

#### ImageQuestionManager
- **File**: `server/controller/ImageQuestionManager.java`
- **Singleton Pattern**: Đảm bảo chỉ có 1 instance
- **Load Images**: 
  - Đọc từ folder `src/main/resources/Images/img`
  - Hỗ trợ `.jpg`, `.png`, `.jpeg`
  - Lưu đường dẫn tuyệt đối và câu hỏi/đáp án tương ứng

#### Image Data Storage
- **Static Map**: `IMAGE_DATA` lưu mapping `filename -> (question, answer)`
- **Ví dụ**:
```java
IMAGE_DATA.put("image1.jpg", new QuestionAnswerPair("Có bao nhiêu con sứa?", 9));
IMAGE_DATA.put("image2.jpg", new QuestionAnswerPair("Có bao nhiêu quả cà chua?", 9));
```

#### Gửi Ảnh Đến Client
- **File**: `server/network/ServerProcessing.java` - `startImageQuizRound()`
- **Process**:
  1. Lấy ảnh hiện tại từ `ImageQuizGameCtr.getCurrentRoundQuestion()`
  2. Đọc ảnh thành `byte[]` qua `ImageQuestionManager.getImageBytes()`
  3. Đóng gói: `Object[] = {roundNumber, imageBytes, question}`
  4. Gửi đến cả 2 clients qua `SERVER_SEND_ROUND_DATA`

```java
ImageQuestionManager.ImageQuestion currentQuestion = imageQuizGameCtr.getCurrentRoundQuestion();
byte[] imageBytes = imageQuestionManager.getImageBytes(currentQuestion.getImagePath());
Object[] roundData = new Object[3];
roundData[0] = imageQuizGameCtr.getCurrentRound() + 1; // Round number
roundData[1] = imageBytes;
roundData[2] = currentQuestion.getQuestion();

sendData(new ObjectWrapper(ObjectWrapper.SERVER_SEND_ROUND_DATA, roundData));
enemy.sendData(new ObjectWrapper(ObjectWrapper.SERVER_SEND_ROUND_DATA, roundData));
```

#### Client Hiển Thị Ảnh
- **File**: `client/view/ImageQuizFrm.java` - `displayRound()`
- **Process**:
  1. Nhận `SERVER_SEND_ROUND_DATA`
  2. Convert `byte[]` thành `Image` qua `new Image(new ByteArrayInputStream(imageBytes))`
  3. Hiển thị trên `ImageView`
  4. Hiển thị câu hỏi trên `Label`
  5. Reset input field và enable submit button
  6. Start countdown timer (15 giây)

```java
byte[] imageBytes = (byte[]) roundData[1];
Image image = new Image(new ByteArrayInputStream(imageBytes));
ImageView imageView = (ImageView) scene.lookup("#imageView");
imageView.setImage(image);
```

### 3. Xử Lý Câu Trả Lời

#### Client Gửi Answer
- **File**: `client/view/ImageQuizFrm.java` - `btnSubmitActionPerformed()`
- **Logic**:
  1. Lấy answer từ `TextField`
  2. Validate (phải là số)
  3. Gửi `SUBMIT_ANSWER` với answer (Integer)
  4. Disable input và submit button (ngăn gửi nhiều lần)

```java
int answer = Integer.parseInt(txtAnswer.getText());
mySocket.sendData(new ObjectWrapper(ObjectWrapper.SUBMIT_ANSWER, answer));
txtAnswer.setDisable(true);
btnSubmit.setDisable(true);
```

#### Server Xử Lý Answer
- **File**: `server/network/ServerProcessing.java` - `SUBMIT_ANSWER` case
- **Logic**:
  1. Kiểm tra round đã được process chưa (tránh xử lý 2 lần)
  2. Lưu answer vào `ImageQuizGameCtr.submitAnswer()`
  3. Nếu là câu trả lời đầu tiên (chưa có ai trả lời):
     - Disable input cho cả 2 clients ngay lập tức
     - Gửi `SERVER_DISABLE_INPUT` đến cả 2
  4. Nếu cả 2 đã trả lời:
     - Process round ngay lập tức
  5. Nếu chỉ có 1 người trả lời:
     - Chờ đến khi hết thời gian hoặc người kia trả lời

```java
case ObjectWrapper.SUBMIT_ANSWER:
    Integer answer = (Integer) data.getData();
    
    // Check if round already processed
    if (imageQuizGameCtr.isRoundProcessed()) {
        break;
    }
    
    boolean wasFirstAnswer = !imageQuizGameCtr.hasAnyAnswer();
    imageQuizGameCtr.submitAnswer(this.username, answer);
    
    if (wasFirstAnswer) {
        // First answer - disable input for both
        sendData(new ObjectWrapper(ObjectWrapper.SERVER_DISABLE_INPUT));
        enemy.sendData(new ObjectWrapper(ObjectWrapper.SERVER_DISABLE_INPUT));
    }
    
    if (imageQuizGameCtr.isRoundFinished()) {
        // Both answered - process immediately
        processImageQuizRound();
    }
    break;
```

### 4. Tính Điểm

#### Scoring System
- **File**: `server/controller/ImageQuizGameCtr.java` - `processRound()`
- **Quy Tắc**:
  - **Đúng**: +1.0 điểm
  - **Sai**: -0.5 điểm
  - **Không trả lời**: 0 điểm (không thay đổi)

#### Xử Lý Khi Cả 2 Trả Lời
- Server xử lý độc lập cho mỗi player:
  - Nếu player1 đúng → `player1Score += 1.0`
  - Nếu player1 sai → `player1Score -= 0.5`
  - Tương tự cho player2

#### Xử Lý Khi Chỉ 1 Người Trả Lời
- Nếu hết thời gian và chỉ có 1 người trả lời:
  - Người trả lời: xử lý như bình thường (đúng +1, sai -0.5)
  - Người không trả lời: 0 điểm (không thay đổi)

#### Round Result
- **File**: `server/controller/ImageQuizGameCtr.java` - `RoundResult` class
- **Data Structure**:
```java
class RoundResult {
    int roundNumber;
    int correctAnswer;
    boolean player1Correct;
    boolean player2Correct;
    boolean player1First;
    boolean player2First;
    double player1Score;
    double player2Score;
    boolean gameFinished;
    String winner;
    String loser;
    double finalPlayer1Score;
    double finalPlayer2Score;
}
```

### 5. Countdown Timer

#### Server Side
- **File**: `server/network/ServerProcessing.java` - `countDownImageQuizRound()`
- **Cơ chế**:
  - Sử dụng `Timer` và `TimerTask` để đếm ngược
  - Mỗi giây kiểm tra `timeRemaining`
  - Khi `timeRemaining <= 0`:
    - Disable input cho cả 2 clients
    - Process round nếu chưa được process

```java
checkTimeTask = new TimerTask() {
    @Override
    public void run() {
        int remaining = timeTask.getTimeRemaining();
        if (remaining <= 0) {
            stopAllTimers();
            if (!imageQuizGameCtr.isRoundProcessed()) {
                sendData(new ObjectWrapper(ObjectWrapper.SERVER_DISABLE_INPUT));
                enemy.sendData(new ObjectWrapper(ObjectWrapper.SERVER_DISABLE_INPUT));
                processImageQuizRound();
            }
        }
    }
};
```

#### Client Side
- **File**: `client/view/ImageQuizFrm.java` - `startCountdown()`
- **Cơ chế**:
  - Sử dụng JavaFX `Timeline` với các `KeyFrame`
  - Tạo KeyFrame cho mỗi giây từ 0 đến `seconds` (15)
  - Hiển thị giá trị đếm ngược: `15 -> 14 -> ... -> 1 -> 0`
  - Khi về 0: disable input

```java
private void startCountdown(int seconds) {
    countdownTimeline = new Timeline();
    for (int i = 0; i <= seconds; i++) {
        final int displayValue = seconds - i;
        countdownTimeline.getKeyFrames().add(
            new KeyFrame(Duration.seconds(i), event -> {
                lblTimer.setText(String.valueOf(displayValue));
                if (displayValue == 0) {
                    txtAnswer.setDisable(true);
                    btnSubmit.setDisable(true);
                }
            })
        );
    }
    countdownTimeline.play();
}
```

### 6. Hiển Thị Kết Quả Round

#### Server Gửi Round Result
- **File**: `server/network/ServerProcessing.java` - `processImageQuizRound()`
- **Data Structure**: `Object[]` với 12 elements:
  - `[0]`: roundNumber
  - `[1]`: correctAnswer
  - `[2]`: player1Correct (boolean)
  - `[3]`: player2Correct (boolean)
  - `[4]`: player1First (boolean)
  - `[5]`: player2First (boolean)
  - `[6]`: player1Score (double)
  - `[7]`: player2Score (double)
  - `[8]`: gameFinished (boolean)
  - `[9]`: winner (String)
  - `[10]`: player1Username
  - `[11]`: player2Username

#### Client Hiển Thị
- **File**: `client/view/ImageQuizFrm.java` - `SERVER_SEND_ROUND_RESULT` case
- **Logic**:
  1. Hiển thị đáp án đúng
  2. Hiển thị điểm hiện tại của cả 2 players
  3. Hiển thị kết quả (đúng/sai) cho mỗi player
  4. Nếu `gameFinished == false`: Delay 0.5 giây rồi chuyển sang round tiếp theo
  5. Nếu `gameFinished == true`: Delay 0.5 giây rồi chuyển sang Result screen

```java
case ObjectWrapper.SERVER_SEND_ROUND_RESULT:
    Object[] resultData = (Object[]) data.getData();
    int correctAnswer = (Integer) resultData[1];
    boolean player1Correct = (Boolean) resultData[2];
    boolean player2Correct = (Boolean) resultData[3];
    double player1Score = (Double) resultData[6];
    double player2Score = (Double) resultData[7];
    boolean gameFinished = (Boolean) resultData[8];
    
    // Hiển thị đáp án và điểm
    lblResult.setText("Correct answer: " + correctAnswer);
    lblPlayer1Score.setText(String.valueOf(player1Score));
    lblPlayer2Score.setText(String.valueOf(player2Score));
    
    if (gameFinished) {
        // Delay 0.5s rồi chuyển sang Result screen
        Timeline delayTimeline = new Timeline(new KeyFrame(Duration.seconds(0.5), e -> {
            ResultFrm resultFrm = new ResultFrm();
            resultFrm.openScene();
        }));
        delayTimeline.play();
    }
    break;
```

---

## Xử Lý Khi Người Chơi Thoát Trận

### 1. Client Gửi Leave Request

#### Trigger
- **File**: `client/view/ImageQuizFrm.java` - "Leave Game" button
- **Logic**: Gửi `CLIENT_LEAVE_GAME` đến server

```java
mySocket.sendData(new ObjectWrapper(ObjectWrapper.CLIENT_LEAVE_GAME));
```

### 2. Server Xử Lý Leave

#### Server Processing
- **File**: `server/network/ServerProcessing.java` - `CLIENT_LEAVE_GAME` case
- **Logic**:
  1. Stop tất cả timers
  2. Update database:
     - Người rời: `updateAfk()` → `points -= 1`, `total_afk += 1`
     - Người còn lại: `updatePointsOnly()` → `points += 1` (KHÔNG cộng `total_wins`)
  3. Tạo match record:
     - Người còn lại: `result = "win"`, `points_change = +1`
     - Người rời: `result = "afk"`, `points_change = -1`
  4. Reset game state: `imageQuizGameCtr = null`
  5. Set results:
     - Người rời: `result = "afk"`
     - Người còn lại: `result = "win"`
  6. Gửi messages:
     - Người rời: `SERVER_PLAYER_LEFT_GAME` → về home
     - Người còn lại: `SERVER_OPPONENT_LEFT_SHOW_RESULT` với data `"win||<username_rời>"` → về Result screen

```java
case ObjectWrapper.CLIENT_LEAVE_GAME:
    if (inGame && enemy != null && imageQuizGameCtr != null) {
        // Stop all timers
        stopAllTimers();
        enemy.stopAllTimers();
        
        // Update AFK for player who left
        playerDAO.updateAfk(this.username); // -1 điểm, total_afk + 1
        
        // Add point for remaining player (but NOT total_wins)
        playerDAO.updatePointsOnly(enemy.username, 1);
        
        // Create match record
        Match match = new Match(enemy.username, this.username, "win", "afk", 1, -1);
        matchDAO.updateMatchResult(match);
        
        // Reset game state
        imageQuizGameCtr = null;
        enemy.imageQuizGameCtr = null;
        
        // Set results
        this.result = "afk";
        enemy.result = "win";
        
        // Send messages
        sendData(new ObjectWrapper(ObjectWrapper.SERVER_PLAYER_LEFT_GAME, this.username));
        enemy.sendData(new ObjectWrapper(ObjectWrapper.SERVER_OPPONENT_LEFT_SHOW_RESULT, "win||" + this.username));
        
        // Update waiting list
        serverCtr.sendWaitingList();
    }
    break;
```

### 3. Client Xử Lý Leave

#### Người Rời Trận
- **File**: `client/view/ImageQuizFrm.java` - `SERVER_PLAYER_LEFT_GAME` case
- **Logic**:
  1. Stop countdown timer
  2. Clear `ImageQuizFrm` và `ImageQuizScene` references
  3. Chuyển về `MainFrm` (home)

```java
case ObjectWrapper.SERVER_PLAYER_LEFT_GAME:
    if (countdownTimeline != null) {
        countdownTimeline.stop();
    }
    mySocket.setImageQuizFrm(null);
    mySocket.setImageQuizScene(null);
    MainFrm mainFrm = new MainFrm();
    mainFrm.openScene();
    break;
```

#### Người Còn Lại
- **File**: `client/view/ImageQuizFrm.java` - `SERVER_OPPONENT_LEFT_SHOW_RESULT` case
- **Logic**:
  1. Stop countdown timer
  2. Clear `ImageQuizFrm` và `ImageQuizScene` references
  3. Hiển thị dialog "Opponent has left"
  4. Click OK hoặc X → chuyển sang `ResultFrm` với kết quả "win"

```java
case ObjectWrapper.SERVER_OPPONENT_LEFT_SHOW_RESULT:
    if (countdownTimeline != null) {
        countdownTimeline.stop();
    }
    mySocket.setImageQuizFrm(null);
    mySocket.setImageQuizScene(null);
    
    String resultData = (String) data.getData();
    String[] parts = resultData.split("\\|\\|");
    String result = parts[0]; // "win"
    String opponentUsername = parts[1];
    
    showOpponentLeftDialog(opponentUsername);
    break;
```

### 4. Cơ Chế Tính Điểm Khi AFK

#### Database Methods
- **File**: `server/dao/PlayerDAO.java`
- **Methods**:
  - `updateAfk(String username)`: `points -= 1`, `total_afk += 1`
  - `updatePointsOnly(String username, int pointsChange)`: Chỉ cộng/trừ điểm, không thay đổi `total_wins`

```java
public boolean updateAfk(String username) {
    String sql = "UPDATE players SET total_afk = total_afk + 1, points = points - 1 WHERE username = ?";
    // Execute update
}

public boolean updatePointsOnly(String username, int pointsChange) {
    String sql = "UPDATE players SET points = points + ? WHERE username = ?";
    // Execute update - chỉ cộng điểm, không cộng total_wins
}
```

---

## Kết Thúc Trận Đấu

### 1. Kết Thúc Game Bình Thường (3 Rounds)

#### Server Xử Lý
- **File**: `server/network/ServerProcessing.java` - `processImageQuizRound()`
- **Khi nào**: `currentRound >= rounds.size()` (sau round 3)
- **Logic**:
  1. Xác định winner/loser/draw dựa trên điểm cuối cùng
  2. Update database:
     - **Win**: `updateWin(winner)` → `points += 1`, `total_wins += 1`
     - **Loss**: `updateLoss(loser)` → `points -= 1`, `total_losses += 1`
     - **Draw**: `updateDraw()` cho cả 2 → `total_draw += 1`, điểm không đổi
  3. Tạo match record
  4. Gửi `SERVER_END_IMAGE_QUIZ_GAME` đến cả 2 clients
  5. Reset game state: `imageQuizGameCtr = null`
  6. Sau 1 giây, hỏi cả 2 clients có muốn chơi lại không (`SERVER_ASK_PLAY_AGAIN`)

```java
if (roundResult.gameFinished) {
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
    
    // Send end game message
    Object[] endGameData = new Object[4];
    endGameData[0] = roundResult.finalPlayer1Score;
    endGameData[1] = roundResult.finalPlayer2Score;
    endGameData[2] = winner;
    endGameData[3] = loser;
    
    sendData(new ObjectWrapper(ObjectWrapper.SERVER_END_IMAGE_QUIZ_GAME, endGameData));
    enemy.sendData(new ObjectWrapper(ObjectWrapper.SERVER_END_IMAGE_QUIZ_GAME, endGameData));
    
    // Reset game state
    imageQuizGameCtr = null;
    enemy.imageQuizGameCtr = null;
    
    // Ask play again after 1 second
    Timer askPlayAgainTimer = new Timer();
    askPlayAgainTimer.schedule(new TimerTask() {
        @Override
        public void run() {
            playAgainResponse = null;
            enemy.playAgainResponse = null;
            sendData(new ObjectWrapper(ObjectWrapper.SERVER_ASK_PLAY_AGAIN));
            enemy.sendData(new ObjectWrapper(ObjectWrapper.SERVER_ASK_PLAY_AGAIN));
        }
    }, 1000);
}
```

#### Client Xử Lý
- **File**: `client/view/ImageQuizFrm.java` - `SERVER_END_IMAGE_QUIZ_GAME` case
- **Logic**:
  1. Delay 0.5 giây để hiển thị đáp án round cuối
  2. Chuyển sang `ResultFrm`

```java
case ObjectWrapper.SERVER_END_IMAGE_QUIZ_GAME:
    // Delay 0.5s để hiển thị đáp án round cuối
    Timeline delayTimeline = new Timeline(new KeyFrame(Duration.seconds(0.5), e -> {
        ResultFrm resultFrm = new ResultFrm();
        mySocket.setResultFrm(resultFrm);
        resultFrm.openScene();
    }));
    delayTimeline.play();
    break;
```

### 2. Play Again (Rematch)

#### Server Hỏi Play Again
- **File**: `server/network/ServerProcessing.java`
- **Message**: `SERVER_ASK_PLAY_AGAIN` (gửi đến cả 2 clients sau 1 giây game kết thúc)

#### Client Hiển Thị Dialog
- **File**: `client/view/ResultFrm.java` - `showPlayAgainDialog()`
- **Logic**:
  - Dialog có 2 nút: "YES" và "NO"
  - Có nút X để đóng (tương đương NO)
  - Dialog được center trên stage (không phải desktop)

#### Client Gửi Response
- **File**: `client/view/ResultFrm.java`
- **Message**: `CLIENT_PLAY_AGAIN_RESPONSE` với data `true` (YES) hoặc `false` (NO)

#### Server Xử Lý Response
- **File**: `server/network/ServerProcessing.java` - `CLIENT_PLAY_AGAIN_RESPONSE` case
- **Logic**:

##### Trường Hợp 1: Client A Chọn YES Trước
- Client A chọn YES → `isInviter = true`, `playAgainResponse = true`
- Server gửi `SERVER_OPPONENT_WAITING_RESPONSE` đến Client B
- Client B hiển thị notification "Opponent is waiting for your response..."
- Client B vẫn thấy dialog để chọn YES/NO

##### Trường Hợp 2: Cả 2 Chọn YES
- Khi Client B cũng chọn YES:
  - Server kiểm tra `this.playAgainResponse == true` và `enemy.playAgainResponse == true`
  - Sử dụng `synchronized` block và `playAgainProcessing` flag để tránh race condition
  - Người là `inviter` (chọn YES trước) sẽ tạo game mới
  - Gửi `SERVER_START_NEW_GAME` đến cả 2 clients
  - Tạo `ImageQuizGameCtr` mới và start round 1

```java
if (wantsPlayAgain) {
    if (enemy.playAgainResponse == null) {
        // First to say YES
        this.isInviter = true;
        enemy.isInviter = false;
        enemy.sendData(new ObjectWrapper(ObjectWrapper.SERVER_OPPONENT_WAITING_RESPONSE));
    } else if (enemy.playAgainResponse) {
        // Both said YES
        synchronized (this) {
            synchronized (enemy) {
                if (!this.playAgainProcessing && !enemy.playAgainProcessing) {
                    // Find inviter
                    ServerProcessing inviter = this.isInviter ? this : enemy;
                    
                    // Notify both
                    this.sendData(new ObjectWrapper(ObjectWrapper.SERVER_START_NEW_GAME));
                    enemy.sendData(new ObjectWrapper(ObjectWrapper.SERVER_START_NEW_GAME));
                    
                    // Create new game
                    inviter.imageQuizGameCtr = new ImageQuizGameCtr(inviter.username, enemy.username);
                    enemy.imageQuizGameCtr = inviter.imageQuizGameCtr;
                    
                    // Start round 1
                    Timer startGameTimer = new Timer();
                    startGameTimer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            inviter.startImageQuizRound();
                        }
                    }, 1000);
                }
            }
        }
    }
}
```

##### Trường Hợp 3: Một Người Chọn NO
- Khi Client A chọn NO:
  - Server gửi `SERVER_OPPONENT_DECLINED_PLAY_AGAIN` đến Client B
  - Client A: Dialog đóng ngay lập tức, không nhận notification
  - Client B: Dialog đóng ngay lập tức, nhận notification "Opponent has declined to play again"
  - Reset `playAgainResponse = null` cho cả 2

```java
else { // Player chose NO
    if (enemy.playAgainResponse == null || enemy.playAgainResponse) {
        // Opponent hasn't responded or said YES
        enemy.sendData(new ObjectWrapper(ObjectWrapper.SERVER_OPPONENT_DECLINED_PLAY_AGAIN));
    }
    this.playAgainResponse = null;
    if (enemy.playAgainResponse != null && !enemy.playAgainResponse) {
        enemy.playAgainResponse = null;
    }
}
```

#### Client Xử Lý Play Again Messages
- **File**: `client/view/ResultFrm.java`
- **Các Cases**:
  - `SERVER_START_NEW_GAME`: Đóng dialog và notification, chờ `SERVER_SEND_ROUND_DATA` để vào game
  - `SERVER_OPPONENT_WAITING_RESPONSE`: Hiển thị notification "Opponent is waiting..."
  - `SERVER_OPPONENT_DECLINED_PLAY_AGAIN`: Đóng dialog, hiển thị notification "Opponent has declined"

### 3. Các Trường Hợp Kết Thúc Khác

#### Game Bị Hủy
- Khi cả 2 players quay về home trước khi game kết thúc
- Không lưu match record
- Không cộng/trừ điểm

#### Client Disconnect Đột Ngột
- Server phát hiện trong `finally` block của `ServerProcessing.run()`
- Xử lý tương tự như `CLIENT_LEAVE_GAME`:
  - Người disconnect: AFK
  - Người còn lại: +1 điểm (không cộng total_wins)

---

## Các Lỗi Còn Tồn Tại

### 1. Countdown Timer Nhảy Số (Đã Sửa)

#### Vấn Đề
- **Mô tả**: Timer đếm ngược từ 15 giây nhưng chuyển round khi còn 2 giây thay vì 0 giây
- **Nguyên nhân**: 
  - Client-side `Timeline` không tạo KeyFrame cho tất cả các giây
  - Server-side timer bắt đầu đếm từ 1 giây thay vì 0 giây

#### Giải Pháp (Đã Áp Dụng)
- **Client**: Tạo KeyFrame cho mỗi giây từ 0 đến `seconds` (15)
```java
for (int i = 0; i <= seconds; i++) {
    final int displayValue = seconds - i;
    countdownTimeline.getKeyFrames().add(new KeyFrame(Duration.seconds(i), event -> {
        lblTimer.setText(String.valueOf(displayValue));
    }));
}
```
- **Server**: Đảm bảo timer bắt đầu đếm từ 0 và xử lý khi `remaining <= 0`

### 2. Final Round Answer Không Hiển Thị (Đã Sửa)

#### Vấn Đề
- **Mô tả**: Ở round cuối, đáp án không được hiển thị trước khi chuyển sang Result screen
- **Nguyên nhân**: `SERVER_END_IMAGE_QUIZ_GAME` được xử lý ngay lập tức, không có delay

#### Giải Pháp (Đã Áp Dụng)
- Thêm delay 0.5 giây trước khi chuyển sang Result screen:
```java
Timeline delayTimeline = new Timeline(new KeyFrame(Duration.seconds(0.5), e -> {
    ResultFrm resultFrm = new ResultFrm();
    resultFrm.openScene();
}));
delayTimeline.play();
```

### 3. Play Again Không Hoạt Động (Đã Sửa)

#### Vấn Đề
- **Mô tả**: Khi cả 2 clients chọn YES, game mới không được tạo
- **Nguyên nhân**: 
  - Race condition khi cả 2 threads gọi `handlePlayAgainDecision()` cùng lúc
  - `ImageQuizFrm` không được clear trước khi tạo game mới

#### Giải Pháp (Đã Áp Dụng)
- Sử dụng `synchronized` block và `playAgainProcessing` flag
- Clear `ImageQuizFrm` và `ImageQuizScene` references trước khi tạo game mới
- Client: Clear references khi nhận `SERVER_SEND_ROUND_DATA`

### 4. NullPointerException Khi Client Disconnect (Đã Sửa)

#### Vấn Đề
- **Mô tả**: Khi client disconnect đột ngột, server crash với `NullPointerException` trong `sendWaitingList()`
- **Nguyên nhân**: 
  - `getPlayer()` trả về `null` khi client chưa login hoặc đã disconnect
  - Gửi data đến client đã disconnect gây `SocketException`

#### Giải Pháp (Đã Áp Dụng)
- Kiểm tra `null` trước khi gọi `getPlayer().getUsername()`
- Try-catch trong `sendData()` để xử lý `IOException`
- Kiểm tra `socket != null` và `!socket.isClosed()` trước khi gửi
- Try-catch trong `sendWaitingList()` và `publicClientNumber()`

### 5. Các Lỗi Còn Tồn Tại (Chưa Sửa)

#### 5.1. Race Condition Trong Round Processing
- **Mô tả**: Nếu cả 2 clients gửi answer cùng lúc, có thể xử lý round 2 lần
- **Nguyên nhân**: `isRoundProcessed()` flag không được check đúng cách
- **Hướng khắc phục**: 
  - Sử dụng `AtomicBoolean` thay vì `boolean` cho `roundProcessed`
  - Double-check locking pattern

#### 5.2. Memory Leak - ImageQuizFrm Không Được Clear
- **Mô tả**: Sau nhiều game, memory tăng do `ImageQuizFrm` không được garbage collect
- **Nguyên nhân**: References không được clear đúng cách
- **Hướng khắc phục**: 
  - Explicitly set `null` cho tất cả references khi game kết thúc
  - Sử dụng WeakReference nếu cần

#### 5.3. Network Latency - Câu Trả Lời Đến Muộn
- **Mô tả**: Nếu client có latency cao, câu trả lời có thể đến sau khi round đã kết thúc
- **Nguyên nhân**: Không có validation về round number
- **Hướng khắc phục**: 
  - Thêm `roundNumber` vào `SUBMIT_ANSWER` message
  - Server kiểm tra `roundNumber` trước khi xử lý answer

#### 5.4. Image Loading Error Không Được Xử Lý
- **Mô tả**: Nếu ảnh không load được, game crash
- **Nguyên nhân**: Không có try-catch khi load ảnh
- **Hướng khắc phục**: 
  - Try-catch trong `ImageQuestionManager.getImageBytes()`
  - Fallback image nếu load thất bại

---

## Hướng Mở Rộng

### 1. Tăng Số Round (3 → 5 hoặc Nhiều Hơn)

#### Thay Đổi Cần Thiết
1. **Server**: `ImageQuizGameCtr.java`
   - Thay đổi `manager.getRandomImageQuestions(3)` thành `manager.getRandomImageQuestions(5)`
   - Hoặc thêm parameter `roundCount` vào constructor

2. **Client**: `ImageQuizFrm.java`
   - Update UI để hiển thị round progress (1/5, 2/5, ...)

3. **Database**: Không cần thay đổi (match record không lưu số round)

#### Code Example
```java
// ImageQuizGameCtr.java
public ImageQuizGameCtr(String player1Username, String player2Username, int roundCount) {
    this.roundCount = roundCount;
    ImageQuestionManager manager = ImageQuestionManager.getInstance();
    this.rounds = manager.getRandomImageQuestions(roundCount);
}
```

### 2. Xem Profile Người Chơi (Click Avatar)

#### Yêu Cầu
- Click vào avatar/username trong danh sách user → hiển thị profile
- Profile hiển thị: username, rank, points, total_wins, total_losses, total_draw, total_afk, match history

#### Implementation
1. **Database**: Thêm method `getPlayerProfile(String username)` trong `PlayerDAO`
2. **Server**: Thêm message `GET_PLAYER_PROFILE` và `SERVER_SEND_PLAYER_PROFILE`
3. **Client**: 
   - Thêm event handler cho avatar/username trong `MainFrm`
   - Tạo `ProfileFrm.java` để hiển thị thông tin
   - Gửi `GET_PLAYER_PROFILE` với username
   - Nhận và hiển thị `SERVER_SEND_PLAYER_PROFILE`

#### Code Example
```java
// ServerProcessing.java
case ObjectWrapper.GET_PLAYER_PROFILE:
    String targetUsername = (String) data.getData();
    PlayerHistory profile = playerDAO.getPlayerInfo(targetUsername);
    profile.setListMatch(matchDAO.getMatchHistory(targetUsername));
    sendData(new ObjectWrapper(ObjectWrapper.SERVER_SEND_PLAYER_PROFILE, profile));
    break;
```

### 3. Chat Trong Game

#### Yêu Cầu
- Cho phép 2 players chat với nhau trong khi chơi
- Chat chỉ hiển thị trong game, không lưu vào database

#### Implementation
1. **Server**: 
   - Thêm message `CLIENT_SEND_CHAT` và `SERVER_BROADCAST_CHAT`
   - Trong `SERVER_SEND_CHAT`, gửi message đến enemy
2. **Client**: 
   - Thêm chat UI trong `ImageQuizFrm`
   - Gửi message qua `CLIENT_SEND_CHAT`
   - Nhận và hiển thị `SERVER_BROADCAST_CHAT`

#### Code Example
```java
// ServerProcessing.java
case ObjectWrapper.CLIENT_SEND_CHAT:
    String message = (String) data.getData();
    if (enemy != null) {
        enemy.sendData(new ObjectWrapper(ObjectWrapper.SERVER_BROADCAST_CHAT, 
            this.username + ": " + message));
    }
    break;
```

### 4. Spectator Mode

#### Yêu Cầu
- Cho phép người chơi khác xem game đang diễn ra (không tham gia)

#### Implementation
1. **Server**: 
   - Thêm list `spectators` trong `ServerProcessing`
   - Broadcast round data và results đến spectators
2. **Client**: 
   - Thêm option "Watch Game" trong `MainFrm`
   - Tạo `SpectatorFrm.java` để hiển thị game (chỉ xem, không tương tác)

### 5. Tournament Mode

#### Yêu Cầu
- Tổ chức tournament với nhiều players
- Bracket elimination hoặc round-robin

#### Implementation
1. **Database**: Thêm table `tournaments` và `tournament_matches`
2. **Server**: 
   - Tạo `TournamentController.java` để quản lý tournament
   - Matchmaking tự động dựa trên bracket
3. **Client**: 
   - UI để tạo/join tournament
   - Hiển thị bracket và progress

### 6. Power-ups và Special Rounds

#### Yêu Cầu
- Thêm power-ups như "Double Points", "Extra Time", "Skip Round"
- Special rounds với điều kiện đặc biệt (ví dụ: chỉ có 5 giây)

#### Implementation
1. **Server**: 
   - Thêm logic xử lý power-ups trong `ImageQuizGameCtr`
   - Random special rounds với điều kiện khác nhau
2. **Client**: 
   - UI để sử dụng power-ups
   - Hiển thị special round indicator

### 7. Replay System

#### Yêu Cầu
- Lưu và phát lại các game đã chơi

#### Implementation
1. **Database**: 
   - Thêm table `game_replays` để lưu tất cả actions (round data, answers, timestamps)
2. **Server**: 
   - Lưu replay data khi game diễn ra
   - API để lấy replay data
3. **Client**: 
   - UI để xem replay từ match history
   - `ReplayFrm.java` để phát lại game

### 8. Mobile App (Android/iOS)

#### Yêu Cầu
- Port game sang mobile

#### Implementation
1. **Backend**: 
   - Giữ nguyên server code (Java)
   - Tạo REST API wrapper cho mobile clients
2. **Mobile**: 
   - Native app (Android: Kotlin/Java, iOS: Swift)
   - Hoặc React Native/Flutter để cross-platform

### 9. AI Bot

#### Yêu Cầu
- Cho phép người chơi chơi với AI bot khi không có đối thủ

#### Implementation
1. **Server**: 
   - Tạo `AIBot.java` implement `ServerProcessing` interface
   - AI trả lời dựa trên image analysis (computer vision)
   - Hoặc random với độ chính xác configurable
2. **Client**: 
   - Option "Play with AI" trong `MainFrm`

---

## Kết Luận

Dự án Count Objects Game đã được xây dựng với kiến trúc Client-Server rõ ràng, sử dụng TCP/IP Sockets để giao tiếp. Game có đầy đủ các tính năng cơ bản: login/register, matchmaking, gameplay, scoring, leaderboard, và match history. Một số lỗi đã được sửa (countdown timer, final round answer, play again, null pointer), nhưng vẫn còn một số vấn đề cần cải thiện (race conditions, memory leaks). Dự án có tiềm năng mở rộng lớn với nhiều tính năng mới như tournament mode, chat, replay system, và mobile app.

---

**Ngày tạo**: 2024  
**Phiên bản**: 1.0  
**Tác giả**: Development Team

