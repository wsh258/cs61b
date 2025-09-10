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
        private final Random RANDOM = new Random();

        private int topXp;
        private int topYp;
        private int downXp;
        private int downYp;


        private Room() {
            int[] xp = twoSortedRandomDistinct(null, 1, WIDTH);
            int[] yp = twoSortedRandomDistinct(null, 1, HEIGHT);
            this.topXp = xp[1];
            this.topYp = yp[1];
            this.downXp = xp[0];
            this.downYp = yp[0];
        }
        private void draw(TETile[][] tiles, TETile teTile) {
            for (int i = downXp; i < topXp;i++) {
                for (int j = downYp;j < topYp;j++) {
                    tiles[i][j] = teTile;
                }
            }
        }
        private int getDownXp(){
            return this.topXp;
        }
        private int getDownYp(){
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

    }

    public static void main(String[] args) {
        TERenderer ter = new TERenderer();

        ter.initialize(WIDTH, HEIGHT);

        TETile[][] tiles = new TETile[WIDTH][HEIGHT];
        for (int x = 0; x < WIDTH; x += 1) {
            for (int y = 0; y < HEIGHT; y += 1) {
                tiles[x][y] = Tileset.NOTHING;
            }
        }

        ArrayList<Room> rooms = new ArrayList<>();

        int roomCount = uniform(new Random(), 16, 26);

        while (rooms.size() < roomCount) {
            Room addRoom = new Room();
            if (addRoom.isOK(rooms)) {
                rooms.add(addRoom);
            }
        }

        for (Room room : rooms) {
            room.draw(tiles,Tileset.FLOOR);
        }

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



        ter.renderFrame(tiles);
    }
}
