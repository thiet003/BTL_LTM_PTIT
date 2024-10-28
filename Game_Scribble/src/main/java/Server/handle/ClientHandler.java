package Server.handle;

import Server.model.GameRoom;
import Server.model.Phase;
import Server.model.Player;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Random;
import java.util.UUID;

public class ClientHandler implements Runnable {
    private Socket clientSocket;
    private static ArrayList<Socket> clientSockets;
    private static ArrayList<GameRoom> rooms;

    public ClientHandler(Socket socket, ArrayList<Socket> clientSockets, ArrayList<GameRoom> rooms) {
        this.clientSocket = socket;
        ClientHandler.clientSockets = clientSockets;
        ClientHandler.rooms = rooms;
    }

    @Override
    public void run() {
        DataInputStream in = null;
        DataOutputStream out = null;
        try {
            in = new DataInputStream(clientSocket.getInputStream());
            out = new DataOutputStream(clientSocket.getOutputStream());

            String request;
            while ((request = in.readUTF()) != null) {
                String[] command = request.split(":"); // Yêu cầu theo định dạng "ACTION:DATA"
                System.out.println(request);
                if (command[0].equals("ADD_USER")) {
                    String[] data = command[1].split(",");
                    String userId = generateUserId();
                    System.out.println(userId);
                    out.writeUTF("ADD_USER:" + userId + "," + data[0] + "," + data[1]);
                    out.flush();
                } else if (command[0].equals("CHAT")) {
                    String username = command[1];
                    String message = command[2];
                    broadcastMessage("CHAT:" + username + ": " + message);
                } else if (command[0].equals("ONLINE")) {
                    System.out.println("Online!");
                    String nickname = command[1];
                    broadcastMessage("ONLINE:" + nickname);
                } else if (command[0].equals("OFFINE")) {
                    String nickname = command[1];
                    broadcastMessage("OFFLINE:" + nickname);
                } else if (command[0].equals("CREATE_ROOM")) {
                    int maxPlayers = Integer.parseInt(command[1]);
                    int targetScore = Integer.parseInt(command[2]);
                    String userId = command[3];
                    String nickname = command[4];
                    String avatar = command[5];
                    String roomId = generateRoomId();
                    System.out.println(roomId);
                    GameRoom newRoom = new GameRoom(maxPlayers, roomId, targetScore);
                    rooms.add(newRoom);
                    newRoom.addClient(clientSocket);
                    Player player = new Player(userId, nickname, avatar, 0);
                    newRoom.getListPlayer().add(player);
                    String rq = roomId;
                    rq = "ROOMID:" + rq+":";
                    for (Player p : newRoom.getListPlayer()) {
                        rq += p.getPlayerId()+","+p.getNickname()+","+p.getAvatar()+","+p.getScore()+":";
                    }
                    out.writeUTF(rq);
                    out.flush();
                } else if (command[0].equals("JOIN_ROOM")) {
                    String roomId = command[1];
                    String userId = command[2];
                    String nickname = command[3];
                    String avatar = command[4];
                    GameRoom room = findRoomById(roomId);
                    if (room != null) {
                        if (!room.isFull()) {
                            if(room.getStatus().equals("PLAYING")){
                                out.writeUTF("ROOM_IS_PLAYING");
                                out.flush();
                                continue;
                            }
                            else if(room.getStatus().equals("ENDING")){
                                out.writeUTF("ROOM_IS_ENDING");
                                out.flush();
                                continue;
                            }
                            else {
                                room.addClient(clientSocket);
                                Player player = new Player(userId, nickname, avatar, 0);
                                room.getListPlayer().add(player);
                                String rq = "JOIN_ROOM_SUCCESS:" + roomId;
                                for (Player p : room.getListPlayer()) {
                                    rq +=":"+ p.getPlayerId()+","+p.getNickname()+","+p.getAvatar()+","+p.getScore();
                                }
                                out.writeUTF(rq);
                                out.flush();
                            }
                        } else {
                            out.writeUTF("ROOM_IS_FULL");
                            out.flush();
                        }
                    } else {
                        out.writeUTF("ROOM_NOT_FOUND");
                        out.flush();
                    }
                } else if (command[0].equals("CHAT_ROOM")) {
                    String roomId = command[1];
                    String username = command[2];
                    String message = command[3];
                    System.out.println(roomId);
                    GameRoom room = findRoomById(roomId);
                    System.out.println(room);
                    if (room != null) {
                        broadcastMessageToRoom(room, "CHATROOM:" + username + ": " + message);
                    }
                } else if (command[0].equals("DRAW")) {
                    // Xử lý vẽ trong phòng
                    String roomId = command[1];
                    int x1 = Integer.parseInt(command[2]);
                    int y1 = Integer.parseInt(command[3]);
                    int x2 = Integer.parseInt(command[4]);
                    int y2 = Integer.parseInt(command[5]);
                    int colorValue = Integer.parseInt(command[6]);

                    GameRoom room = findRoomById(roomId);
                    if (room != null) {
                        broadcastMessageToRoom(room, "DRAW:" + x1 + ":" + y1 + ":" + x2 + ":" + y2 + ":" + colorValue);
                    }
                } else if (command[0].equals("START_GAME")) {
                    String roomId = command[1];
                    GameRoom room = findRoomById(roomId);
                    if (room != null) {
                        room.setStatus("PLAYING");
                        broadcastMessageToRoom(room, "START_GAME");
                    }
                }
                else if(command[0].equals("ADD_PLAYER_TO_ROOM")){
                    String roomId = command[1];
                    GameRoom room = findRoomById(roomId);
                    if (room != null) {
                            String rq = "ADD_PLAYER_TO_ROOM:" + roomId;
                            for (Player p : room.getListPlayer()) {
                                rq +=":"+ p.getPlayerId()+","+p.getNickname()+","+p.getAvatar()+","+p.getScore();
                            }
                            broadcastMessageToRoom(room, rq);
                    }
                }
                else if(command[0].equals("LEAVE_ROOM"))
                {
                    String roomId = command[1];
                    String nickname = command[2];
                    String host = command[3];
                    GameRoom room = findRoomById(roomId);
                    if(room!=null){
                        if(host.equals("true"))
                        {
                            room.setStatus("ENDING");
                            broadcastMessage("DELETE_ROOM:"+roomId);
                        }
                        else {
                            Player player = findPlayerByNickname(nickname,room);
                            room.getListPlayer().remove(player);
                            room.removeClient(clientSocket);
                            String rq = "UPDATE_PLAYER:" + roomId;
                            for (Player p : room.getListPlayer()) {
                                rq +=":"+ p.getPlayerId()+","+p.getNickname()+","+p.getAvatar()+","+p.getScore();
                            }
                            broadcastMessageToRoom(room, rq);
                        }
                    }
                    clientSocket.close();

                }
                else if(command[0].equals("NOTIFY")){
                    String roomID = command[1];
                    String notify = command[2];
                    String rq = "NOTIFY:"+notify;
                    GameRoom room = findRoomById(roomID);
                    if(room!=null){
                        broadcastMessageToRoom(room,rq);
                    }
                }
                else if(command[0].equals("CHOOSE_PHASE")){
                    String roomID = command[1];
                    String nickname = command[2];
                    String currentPlayerId = command[3];
                    String rq = "CHOOSE_PHASE:"+roomID+":"+nickname+":"+currentPlayerId;
                    GameRoom room = findRoomById(roomID);
                    if(room!=null){
                        broadcastMessageToRoom(room,rq);
                    }
                }
                else if(command[0].equals("DRAW_PHASE")){
                    String roomID = command[1];
                    String nickname = command[2];
                    String word = command[3];
                    String currentPlayerId = command[4];
                    String phaseId = generateRoomId();
                    GameRoom room = findRoomById(roomID);
                    if(room!=null){
                        Phase phase = new Phase(phaseId,roomID,nickname,word);
                        room.getListPhase().add(phase);
                        String rq = "DRAW_PHASE:"+roomID+":"+nickname+":"+word+":"+currentPlayerId+":"+phaseId;
                        broadcastMessageToRoom(room,rq);
                    }
                }
                else if(command[0].equals("GUESS"))
                {
                    String roomID = command[1];
                    String phaseID = command[2];
                    String myNickname = command[3];
                    String word = command[4];
                    String currentNickname = command[5];
                    GameRoom room = findRoomById(roomID);
                    if(room!=null){

                        Phase phase = findPhaseById(phaseID,room);
                        if(phase!=null){
                            if(phase.getWord().equals(word)){
                                Player myPlayer = findPlayerByNickname(myNickname,room);
                                Player currentPlayer = findPlayerByNickname(currentNickname,room);
                                int count = phase.getCountCorrectPlayer();
                                myPlayer.addScore(10-count);
                                currentPlayer.addScore(5);
                                count++;
                                int targetScore = room.getTargetScore();
                                String rq = "GUESS:TRUE:"+myNickname+":"+word+":"+count+":"+targetScore;
                                phase.setCountCorrectPlayer(count);
                                for (Player p : room.getListPlayer()) {
                                    rq +=":"+ p.getPlayerId()+","+p.getNickname()+","+p.getAvatar()+","+p.getScore();
                                }
                                broadcastMessageToRoom(room,rq);
                            }
                            else{
                                String rq = "GUESS:FALSE:"+myNickname+":"+word;
                                broadcastMessageToRoom(room,rq);
                            }
                        }
                    }
                }
                else if(command[0].equals("CLEAR_PANEL"))
                {
                    String roomID = command[1];
                    GameRoom room = findRoomById(roomID);
                    if(room!=null){
                        broadcastMessageToRoom(room,"CLEAR_PANEL:"+roomID);
                    }
                }
                else if (command[0].equals("END_GAME")) {
                    String roomId = command[1];
                    GameRoom room = findRoomById(roomId);
                    if (room != null) {
                        room.setStatus("ENDING");
                        broadcastMessageToRoom(room, "END_GAME:"+roomId);
                    }
                }
            }
        } catch (EOFException e) {
            // Client closed connection
            System.out.println("Client disconnected: " + clientSocket.getRemoteSocketAddress());
        } catch (IOException e) {
            System.err.println("IOException occurred: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
                if (out != null) {
                    out.close();
                }
                if (clientSocket != null) {
                    clientSockets.remove(clientSocket);
                    clientSocket.close();
                }
            } catch (IOException e) {
                System.err.println("Error while closing resources: " + e.getMessage());
            }
        }
    }

    private void broadcastMessageToRoom(GameRoom room, String message) {
        for (Socket socket : room.getClientSockets()) {
            try {
                DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                out.writeUTF(message);
                out.flush();
            } catch (IOException e) {
                System.err.println("Error broadcasting message: " + e.getMessage());
            }
        }
    }

    private GameRoom findRoomById(String roomId) {
        for (GameRoom room : rooms) {
            if (room.getRoomId().equals(roomId)) {
                return room;
            }
        }
        return null;
    }
    private Phase findPhaseById(String phaseId, GameRoom rooms) {
        {
            for (Phase phase : rooms.getListPhase()) {
                if (phase.getIdPhase().equals(phaseId)) {
                    return phase;
                }
            }
            return null;
        }
    }
    private String generateRoomId() {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder roomId = new StringBuilder(10);
        Random random = new Random();
        for (int i = 0; i < 10; i++) {
            roomId.append(characters.charAt(random.nextInt(characters.length())));
        }
        return roomId.toString();
    }

    private void broadcastMessage(String message) {
        for (Socket socket : clientSockets) {
            try {
                DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                out.writeUTF(message);
                out.flush();
            } catch (IOException e) {
                System.err.println("Error broadcasting message: " + e.getMessage());
            }
        }
    }
    private Player findPlayerByNickname(String nickname, GameRoom room) {
        for (Player player : room.getListPlayer()) {
            if (player.getNickname().equals(nickname)) {
                return player;
            }
        }
        return null;
    }
    private String generateUserId() {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder userId = new StringBuilder(10);
        Random random = new Random();
        for (int i = 0; i < 10; i++) {
            userId.append(characters.charAt(random.nextInt(characters.length())));
        }
        return userId.toString();
    }
}
