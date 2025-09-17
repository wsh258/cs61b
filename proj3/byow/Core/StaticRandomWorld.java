package byow.Core;
import byow.TileEngine.TERenderer;
import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;

import java.util.ArrayList;
import java.util.Random;

import static byow.Core.RandomUtils.uniform;

public class StaticRandomWorld {
    private final int width;
    private final int height;

    private static final long SEED = 2873123;
    TETile[][] tiles;

    StaticRandomWorld(int width, int height) {
        this.width = width;
        this.height = height;
        tiles = new TETile[width][height];
    }


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


    private class Room {
        private final Random RANDOM;

        private final int topXp;
        private final int topYp;
        private final int downXp;
        private final int downYp;

        private Room(Random random) {
            RANDOM = random;
            int[] xp = twoSortedRandomDistinct(random, 1, width);
            int[] yp = twoSortedRandomDistinct(random, 1, height);
            this.topXp = xp[1];
            this.topYp = yp[1];
            this.downXp = xp[0];
            this.downYp = yp[0];
        }

        private void drawRoom() {
            for (int i = downXp; i < topXp; i++) {
                for (int j = downYp; j < topYp; j++) {
                    tiles[i][j] = Tileset.FLOOR;
                }
            }
        }



        private boolean isOverlap(Room otherRoom) {
            return this.topXp >= otherRoom.downXp && this.downXp <= otherRoom.topXp
                    && this.topYp >= otherRoom.downYp && this.downYp <= otherRoom.topYp;
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

    }

    private static double distance(Room r1, Room r2) {
        int dx = r1.centerX() - r2.centerX();
        int dy = r1.centerY() - r2.centerY();
        return Math.sqrt(dx * dx + dy * dy);

    }

    private void drawWall() {
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (tiles[x][y] == Tileset.FLOOR) {
                    for (int dx = -1; dx <= 1; dx++) {
                        for (int dy = -1; dy <= 1; dy++) {
                            int nx = x + dx;
                            int ny = y + dy;
                            if (nx >= 0 && nx < width && ny >= 0 && ny < height
                                    && tiles[nx][ny] == Tileset.NOTHING) {
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

    public TETile[][] staticRandomWorld(Random random) {
        for (int x = 0; x < width; x += 1) {
            for (int y = 0; y < height; y += 1) {
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
            room.drawRoom();
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
                    double d = distance(c, u);
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
        drawWall();
        return tiles;
    }

    public int[] generatePlayer(TETile[][] originWorld, Random rand) {
        while (true) {
            int x = rand.nextInt(width);
            int y = rand.nextInt(height);

            if (originWorld[x][y] == Tileset.FLOOR) {
                originWorld[x][y] = Tileset.AVATAR; // 找到合法出生点
                return new int[]{x, y};
            }
        }
    }

    public static void generatePlayer(TETile[][] originWorld, int[] playerPosition) {
        originWorld[playerPosition[0]][playerPosition[1]] = Tileset.AVATAR;
    }

    public static void main(String[] args) {
        TERenderer ter = new TERenderer();
        int width = 105;
        int height = 80;
        ter.initialize(105, 80);
        Random random = new Random(SEED);
        StaticRandomWorld worldGen = new StaticRandomWorld(width, height);
        // 生成地图
        TETile[][] tiles = worldGen.staticRandomWorld(random);
        worldGen.generatePlayer(tiles, random);
        ter.renderFrame(tiles);
    }
}

