package Server.model;

public class Phase {
    private String idPhase;
    private String roomId;
    private String nicknameUser;  // User ID của người đang vẽ
    private String word;     // Từ mà người chơi cần vẽ
    private int countCorrectPlayer;
    public Phase(String idPhase, String roomId, String nicknameUser, String word) {
        this.idPhase = idPhase;
        this.roomId = roomId;
        this.nicknameUser = nicknameUser;
        this.word = word;
        this.countCorrectPlayer = 0;
    }

    // Getters và Setters
    public String getIdPhase() {
        return idPhase;
    }

    public void setIdPhase(String idPhase) {
        this.idPhase = idPhase;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public String getNicknameUser() {
        return nicknameUser;
    }

    public void setNicknameUser(String nicknameUser) {
        this.nicknameUser = nicknameUser;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public int getCountCorrectPlayer() {
        return countCorrectPlayer;
    }

    public void setCountCorrectPlayer(int countCorrectPlayer) {
        this.countCorrectPlayer = countCorrectPlayer;
    }

    // Phương thức chuyển đổi sang chuỗi để gửi qua mạng
    @Override
    public String toString() {
        return "Phase{" +
                "idPhase='" + idPhase + '\'' +
                ", roomId='" + roomId + '\'' +
                ", userId='" + nicknameUser + '\'' +
                ", word='" + word + '\'' +
                '}';
    }
}
