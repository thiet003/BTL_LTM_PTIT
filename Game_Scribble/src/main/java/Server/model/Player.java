package Server.model;

public class Player {
    private String playerId;
    private String nickname;
    private String avatar;
    private int score;
    public Player() {
    }
    public Player(String playerId, String nickname, String avatar, int score) {
        this.playerId = playerId;
        this.nickname = nickname;
        this.avatar = avatar;
        this.score = score;
    }

    public String getPlayerId() {
        return playerId;
    }

    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }
}
