package Server.model;

import java.net.Socket;
import java.util.ArrayList;

public class GameRoom {
    private String roomId;
    private final ArrayList<Socket> clientSockets;
    private int maxPlayers;
    private int targetScore;

    public GameRoom(int maxPlayers, String roomId, int targetScore) {
        this.targetScore = targetScore;
        this.clientSockets = new ArrayList<>();
        this.roomId = roomId;
        this.maxPlayers = maxPlayers;
    }

    public String getRoomId() {
        return roomId;
    }

    public ArrayList<Socket> getClientSockets() {
        return clientSockets;
    }

    public void addClient(Socket clientSocket) {
        synchronized (clientSockets) {
            clientSockets.add(clientSocket);
        }
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }

    public int getTargetScore() {
        return targetScore;
    }

    public boolean isFull() {
        return clientSockets.size() >= maxPlayers;
    }
}
