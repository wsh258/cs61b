package byow.lab12;
import org.junit.Test;

import static org.junit.Assert.*;

import byow.TileEngine.TERenderer;
import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;

import java.util.Random;

/**
 * Draws a world consisting of hexagonal regions.
 */
public class HexWorld {
    private static final int WIDTH = 50;
    private static final int HEIGHT = 50;



//    private static void addHexagon (TETile[][] tiles, int length, int xp, int yp) {
//    int middleWidth = getHexMiddleWidth(length);
//        for (int i = 0; i < length * 2; i++){
//            if (i < length) {
//                xp--;
//                yp--;
//                addRowTile(tiles,length + (i*2), xp, yp);
//            } else if (i == length){
//                yp--;
//                addRowTile(tiles,middleWidth, xp, yp);
//            }
//            else {
//                xp++;
//                yp--;
//                addRowTile(tiles,middleWidth - (((i-1) % length) * 2) - 2, xp, yp);
//            }
//        }
//    }


    private static void addHexagon(TETile[][] tiles, int length, int xp, int yp) {
        int totalRows = 2 * length;
        TETile ran = randomTile();
        for (int i = 0; i < totalRows; i++) {
            // 当前行的宽度
            int rowWidth = length + 2 * Math.min(i, totalRows - 1 - i);
            // 当前行的起始 x 偏移
            int xOffset = -Math.min(i, totalRows - 1 - i);
            // 行的 y 坐标：逐行往下
            int y = yp + i;
            // 添加这一行
            addRowTile(tiles, ran, rowWidth, xp + xOffset, y);
        }

    }
    private static final long SEED = 2873123;
    private static final Random RANDOM = new Random(SEED);

    private static TETile randomTile() {
        int tileNum = RANDOM.nextInt(2);
        switch (tileNum) {
            case 0: return Tileset.WALL;
            case 1: return Tileset.FLOWER;
            default: return Tileset.NOTHING;
        }
    }

    private static void addRowTile(TETile[][] tiles, TETile teTile, int length, int xp, int yp) {

        while (length > 0) {
            length--;
            tiles[xp+length][yp] = teTile;
        }
    }

    private static int[] getLeftPosition(int length, int xp, int yp) {
        int[] xy = new int[2];
        xy[0] = xp - (2*length) + 1;
        xy[1] = yp + length;
        return xy;
    }

    private static int[] getRightTilePosition(int length, int xp, int yp) {
        int[] xy = new int[2];
        xy[0] = xp + (2*length) - 1;
        xy[1] = yp + length;
        return xy;
    }

    private static void addUpHexagan (TETile[][] tiles, int length, int xp, int yp, int count) {
        for (int i = 0; i < count; i++) {
            addHexagon(tiles, length, xp, yp + i * length * 2);
        }
    }

    private static void addAll(TETile[][] tiles, int length, int xp, int yp) {
        addUpHexagan(tiles, length, xp, yp, 5);

        int[] left1 = getLeftPosition(length, xp, yp);
        addUpHexagan(tiles, length, left1[0], left1[1], 4);

        int[] left2 = getLeftPosition(length, left1[0], left1[1]);
        addUpHexagan(tiles, length, left2[0], left2[1], 3);


        int[] right1 = getRightTilePosition(length, xp, yp);
        addUpHexagan(tiles, length, right1[0], right1[1],4);

        int[] right2 = getRightTilePosition(length, right1[0], right1[1]);
        addUpHexagan(tiles, length, right2[0], right2[1], 3);

    }

    public static void main(String[] args) {
    TERenderer ter = new TERenderer();
    ter.initialize(WIDTH, HEIGHT);

    TETile[][] Tiles = new TETile[WIDTH][HEIGHT];
    for (int x = 0; x < WIDTH; x += 1) {
        for (int y = 0; y < HEIGHT; y += 1) {
            Tiles[x][y] = Tileset.NOTHING;
        }
    }

    addAll(Tiles,3,25,0);
    ter.renderFrame(Tiles);
    }
}
