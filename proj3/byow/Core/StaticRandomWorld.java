package byow.Core;
import byow.TileEngine.TERenderer;
import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;

import java.util.ArrayList;
import java.util.Random;

import static byow.Core.RandomUtils.uniform;

public class StaticRandomWorld {
    private static final int WIDTH = 105;
    private static final int HEIGHT = 45;

    private static final long SEED = 2873123;


    public static int[] twoSortedRandomDistinct(Random random, int a, int b) {
        if (random == null) {
            random = new Random(); // 默认用系统时间种子
        }

        if (b - a < 2) {
            throw new IllegalArgumentException("range too small for two distinct numbers");
        }
        int x = uniform(random, a, b - 11); // 起点
        int y = uniform(random, x + 2, x + 10); // 保证 y > x
        return new int[]{x, y};
    }


    private static class Room {
        private final Random RANDOM;

        private int topXp;
        private int topYp;
        private int downXp;
        private int downYp;

        private Room(Random random) {
            RANDOM = random;
            int[] xp = twoSortedRandomDistinct(random, 1, WIDTH);
            int[] yp = twoSortedRandomDistinct(random, 1, HEIGHT);
            this.topXp = xp[1];
            this.topYp = yp[1];
            this.downXp = xp[0];
            this.downYp = yp[0];
        }

        private void drawRoom(TETile[][] tiles) {
            for (int i = downXp; i < topXp; i++) {
                for (int j = downYp; j < topYp; j++) {
                    tiles[i][j] = Tileset.FLOOR;
                }
            }
        }

        private int getDownXp() {
            return this.topXp;
        }

        private int getDownYp() {
            return this.downYp;
        }

        private boolean isOverlap(Room otherRoom) {
            return this.topXp >= otherRoom.downXp && this.downXp <= otherRoom.topXp &&
                    this.topYp >= otherRoom.downYp && this.downYp <= otherRoom.topYp;
        }

        private boolean isOK(ArrayList<Room> otherRooms) {
            for (Room otherRoom : otherRooms) {
                if (this.isOverlap(otherRoom)) {
                    return false;
                }
            }
            return true;
        }

        private int[] getRandomTilePosition() {
            int[] position = new int[2];
            position[0] = uniform(RANDOM, downXp + 1, topXp);
            position[1] = uniform(RANDOM, downYp + 1, topYp);
            return position;
        }

        int centerX() {
            return (topXp + downXp) / 2;
        }

        int centerY() {
            return (topYp + downYp) / 2;
        }

        private static double distance(Room r1, Room r2) {
            int dx = r1.centerX() - r2.centerX();
            int dy = r1.centerY() - r2.centerY();
            return Math.sqrt(dx * dx + dy * dy);


        }
    }

    private static void drawWall(TETile[][] tiles) {
        for (int x = 0; x < WIDTH; x++) {
            for (int y = 0; y < HEIGHT; y++) {
                if (tiles[x][y] == Tileset.FLOOR) {
                    for (int dx = -1; dx <= 1; dx++) {
                        for (int dy = -1; dy <= 1; dy++) {
                            int nx = x + dx;
                            int ny = y + dy;
                            if (nx >= 0 && nx < WIDTH && ny >= 0 && ny < HEIGHT && tiles[nx][ny] == Tileset.NOTHING) {
                                tiles[nx][ny] = Tileset.WALL;
                            }
                        }
                    }
                }
            }
        }
    }

    private static void drawHallway(TETile[][] tiles, Room fromRoom, Room targetRoom) {
        int[]xy1 = fromRoom.getRandomTilePosition();
        int[]xy2 = targetRoom.getRandomTilePosition();
        for (int x = Math.min(xy1[0], xy2[0]); x <= Math.max(xy1[0], xy2[0]); x++) {
            tiles[x][xy1[1]] = Tileset.FLOOR;
        }
        for (int y = Math.min(xy1[1], xy2[1]); y <= Math.max(xy1[1], xy2[1]); y++) {
            tiles[xy2[0]][y] = Tileset.FLOOR;
        }
    }

    public static TETile[][] staticRandomWorld (Random random) {
        TETile[][] tiles = new TETile[WIDTH][HEIGHT];
        for (int x = 0; x < WIDTH; x += 1) {
            for (int y = 0; y < HEIGHT; y += 1) {
                tiles[x][y] = Tileset.NOTHING;
            }
        }

        ArrayList<Room> rooms = new ArrayList<>();
        int roomCount = uniform(random, 16, 26);
        while (rooms.size() < roomCount) {
            Room addRoom = new Room(random);
            if (addRoom.isOK(rooms)) {
                rooms.add(addRoom);
            }
        }
        for (Room room : rooms) {
            room.drawRoom(tiles);
        }

        ArrayList<Room> connected = new ArrayList<>();
        ArrayList<Room> unconnected = new ArrayList<>(rooms);
        connected.add(unconnected.remove(0)); // 随便拿一个房间作为起点
        while (!unconnected.isEmpty()) {
            Room closestRoom = null;
            Room fromRoom = null;
            double minDist = Double.MAX_VALUE;
            // 找到最近的房间对
            for (Room c : connected) {
                for (Room u : unconnected) {
                    double d = Room.distance(c, u);
                    if (d < minDist) {
                        minDist = d;
                        closestRoom = u;
                        fromRoom = c;
                    }
                }
            }
            // 连接这两个房间
            if (fromRoom != null) {
                drawHallway(tiles, fromRoom, closestRoom);
            }
            connected.add(closestRoom);
            unconnected.remove(closestRoom);
        }
        drawWall(tiles);
        return tiles;
    }

    public static void main(String[] args) {
        TERenderer ter = new TERenderer();
        ter.initialize(WIDTH, HEIGHT);
        TETile[][] tiles = staticRandomWorld(new Random(SEED));
        ter.renderFrame(tiles);
    }

}

