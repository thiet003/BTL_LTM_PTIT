package Server.handle;

import Server.model.GameRoom;
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
                            room.addClient(clientSocket);
                            Player player = new Player(userId, nickname, avatar, 0);
                            room.getListPlayer().add(player);
                            String rq = "JOIN_ROOM_SUCCESS:" + roomId;
                            for (Player p : room.getListPlayer()) {
                                rq +=":"+ p.getPlayerId()+","+p.getNickname()+","+p.getAvatar()+","+p.getScore();
                            }
                            out.writeUTF(rq);
                            out.flush();
                        } else {
                            out.writeUTF("JOIN_ROOM_FAIL:Room is full");
                            out.flush();
                        }
                    } else {
                        out.writeUTF("JOIN_ROOM_FAIL:ROOM_NOT_FOUND");
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
