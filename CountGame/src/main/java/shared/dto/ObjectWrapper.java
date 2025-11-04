package shared.dto;

import java.io.Serializable;

public class ObjectWrapper implements Serializable {
    // client gửi request login
    public static final int LOGIN_USER = 1;

    // server respone request login
    public static final int SERVER_LOGIN_USER = 2;
    // server gửi thông báo tài khoản đã được sử dụng
    public static final int LOGIN_ACCOUNT_IN_USE = 74;

    // client gửi request get all user
    public static final int GET_ALL_USER = 18;

    // server gửi danh sách user
    public static final int SERVER_SEND_ALL_USER = 27;

    // server gửi cập nhật số number client online
    public static final int SERVER_INFORM_CLIENT_NUMBER = 3;

    // server gửi cập nhật các client đang rảnh
    public static final int SERVER_INFORM_CLIENT_WAITING = 4;

    // client gửi thông báo đăng nhập thành công, server cập nhật danh sách client rảnh
    public static final int LOGIN_SUCCESSFUL = 5;

    // 1 client request mời chơi client khác
    public static final int SEND_PLAY_REQUEST = 6;
    public static final int SERVER_SEND_PLAY_REQUEST_ERROR = 7;

    // server gửi request này cho client được mời
    public static final int RECEIVE_PLAY_REQUEST = 8;

    // client kia chấp nhận
    public static final int ACCEPTED_PLAY_REQUEST = 9;

    // server gửi request này cho cả 2 khi người kia chấp nhận
    public static final int SERVER_SET_GAME_READY = 10;

    // client kia từ chối
    public static final int REJECTED_PLAY_REQUEST = 11;

    // 
    public static final int SERVER_REJECTED_PLAY_REQUEST = 12;

    // client gửi request này tức logout ở mainfrm
    public static final int EXIT_MAIN_FORM = 22;

    // luồng 1 client bị ngắt đột ngột khi chơi ?
    public static final int SERVER_DISCONNECTED_CLIENT_ERROR = 23;

    // client yêu cầu cập nhật lại danh sách client rảnh
    public static final int UPDATE_WAITING_LIST_REQUEST = 24;

    public static final int GET_HISTORY = 40;
    public static final int GET_RANKING = 41;
    public static final int SERVER_SEND_HISTORY = 42;
    public static final int SERVER_SEND_RANKING = 43;

    // server gửi kết quả hiển thị cho result form
    public static final int GET_RESULT = 33;
    // Còn đây là client 2 afk khi đang chơi, server trả về cho playfrm
    public static final int SERVER_SEND_RESULT = 34;

    // xem kết quả xong ra trang chủ
    public static final int BACK_TO_MAIN_FORM = 39;
    
    public static final int REGISTER_USER = 50;
    public static final int REGISTER_SUCCESSFUL =51;
    public static final int SERVER_REGISTER_USER=52;
    
    // Image Quiz Game Constants
    // client (inviter) gửi request bắt đầu game
    public static final int START_IMAGE_QUIZ_GAME = 60;
    // server gửi round data (image bytes + question) cho cả 2 client
    public static final int SERVER_SEND_ROUND_DATA = 61;
    // client gửi câu trả lời (số lượng đồ vật)
    public static final int SUBMIT_ANSWER = 62;
    // server gửi kết quả round (đúng/sai, điểm hiện tại)
    public static final int SERVER_SEND_ROUND_RESULT = 63;
    // server gửi thông báo vô hiệu hóa input khi có người gửi đáp án
    public static final int SERVER_DISABLE_INPUT = 73;
    // server gửi thông báo hết thời gian round
    public static final int SERVER_ROUND_TIME_OUT = 64;
    // server gửi kết thúc game (tổng điểm, người thắng)
    public static final int SERVER_END_IMAGE_QUIZ_GAME = 65;
    
    // client gửi request rời trận giữa chừng
    public static final int CLIENT_LEAVE_GAME = 66;
    // server gửi thông báo người chơi đã rời trận và quay về màn hình chính (cho người rời trận)
    public static final int SERVER_PLAYER_LEFT_GAME = 67;
    // server gửi thông báo đối thủ đã rời trận và chuyển về Result screen (cho người còn lại)
    public static final int SERVER_OPPONENT_LEFT_SHOW_RESULT = 75;
    
    // Play Again - Rematch
    // client gửi response về việc muốn chơi lại hay không (YES/NO)
    public static final int CLIENT_PLAY_AGAIN_RESPONSE = 68;
    // server yêu cầu cả 2 client xác nhận có muốn chơi lại không
    public static final int SERVER_ASK_PLAY_AGAIN = 69;
    // server thông báo bắt đầu game mới (cả 2 đồng ý)
    public static final int SERVER_START_NEW_GAME = 70;
    // server thông báo một người từ chối, không chơi lại
    public static final int SERVER_PLAY_AGAIN_DECLINED = 71;
    // server thông báo đối thủ đang chờ bạn đồng ý YES
    public static final int SERVER_OPPONENT_WAITING_RESPONSE = 76;
    // server thông báo đối thủ đã từ chối chơi lại
    public static final int SERVER_OPPONENT_DECLINED_PLAY_AGAIN = 77;
    // server thông báo đang chờ đối thủ xác nhận play again
    public static final int SERVER_WAITING_OPPONENT_CONFIRM = 72;
    
    private int performative;
    private Object data;

    public ObjectWrapper() {
        super();
    }

    public ObjectWrapper(int performative, Object data) {
        super();
        this.performative = performative;
        this.data = data;
    }

    public ObjectWrapper(int performative) {
        this.performative = performative;
    }

    public int getPerformative() {
        return performative;
    }

    public void setPerformative(int performative) {
        this.performative = performative;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "ObjectWrapper{" + "performative=" + performative + ", data=" + data + '}';
    }
}
