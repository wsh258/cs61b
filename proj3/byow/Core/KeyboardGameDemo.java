package byow.Core;

import byow.TileEngine.TERenderer;
import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;
import edu.princeton.cs.introcs.StdDraw;
import java.awt.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Random;

public class KeyboardGameDemo {

    // Canvas and World Constants

    private static final int TILE_SIZE = 16;
    private long seed;
    private static final String SAVE_FILE = "byow/Core/save.txt";
    private static final int WIDTH = 105;
    private static final int HEIGHT = 45;
    // Font and Text Constants
    private static final String FONT_NAME = "Monaco";
    private static final int TITLE_FONT_SIZE = 40;
    private static final int MENU_FONT_SIZE = 25;
    private static final int HUD_FONT_SIZE = 16;
    private static final int SEED_PROMPT_FONT_SIZE = 30;

    // Menu and UI Constants

    private static final String TITLE_TEXT = "CS61B: THE GAME";
    private static final String NEW_GAME_TEXT = "New Game (N)";
    private static final String LOAD_GAME_TEXT = "Load Game (L)";
    private static final String QUIT_TEXT = "Quit (Q)";
    private static final String SEED_PROMPT_TEXT = "please enter a random seed:";
    private static final String SAVE_QUIT_TEXT = "Press :Q to Save & Quit";
    private static final String TILE_INFO_PREFIX = "Tile: ";

    // Game Loop Constants
    private static final int PAUSE_DELAY_MS = 20;
    private static final int MOVE_DELAY_MS = 33;
    private static final char KEY_NEW_GAME = 'N';
    private static final char KEY_LOAD_GAME = 'L';
    private static final char KEY_QUIT = 'Q';
    private static final char KEY_UP = 'W';
    private static final char KEY_DOWN = 'S';
    private static final char KEY_LEFT = 'A';
    private static final char KEY_RIGHT = 'D';
    private static final char KEY_SAVE_PREFIX = ':';
    private static final char KEY_SAVE_SUFFIX = 'Q';
    private final double MENU_CENTER_X;
    private final double MENU_TITLE_Y;
    private final double NEW_GAME_Y;
    private final double LOAD_GAME_Y;
    private final double QUIT_GAME_Y;
    private final double SEED_PROMPT_Y;
    private final double SEED_INPUT_Y;
    private final double HUD_TEXT_Y;
    private final double HUD_TEXT_LEFT_X;
    private final double HUD_TEXT_RIGHT_X;
    // Game State and World
    private final int width;
    private final int height;
    TETile[][] world;
    public static void main(String[] args) {
        new KeyboardGameDemo(WIDTH, HEIGHT).gaming();
    }

    public void gaming() {
        initCanvas();
        drawMenu();
        gameLoop();
    }
    public KeyboardGameDemo(int width, int height) {
        this.width = width;
        this.height = height;
        MENU_CENTER_X = width / 2.0;
        MENU_TITLE_Y = height * 0.75;
        NEW_GAME_Y = height / 2.0 + 2;
        LOAD_GAME_Y = height / 2.0;
        QUIT_GAME_Y = height / 2.0 - 2;
        SEED_PROMPT_Y = height / 2.0 - 5;
        SEED_INPUT_Y = height / 2.0 - 8;
        HUD_TEXT_Y = height - 1;
        HUD_TEXT_LEFT_X = 1;
        HUD_TEXT_RIGHT_X = height - 1;
        world = new TETile[width][height];
    }

    /**
     * 初始化画布
     */
    private void initCanvas() {
        StdDraw.setCanvasSize(this.width * TILE_SIZE, this.height * TILE_SIZE);
        StdDraw.setXscale(0, this.width);
        StdDraw.setYscale(0, this.height);
        StdDraw.clear(Color.BLACK);
        StdDraw.enableDoubleBuffering();
        StdDraw.setPenColor(Color.MAGENTA);
    }

    /**
     * 绘制主菜单
     */
    private void drawMenu() {
        Font font = new Font(FONT_NAME, Font.BOLD, MENU_FONT_SIZE);
        StdDraw.setFont(font);
        StdDraw.text(MENU_CENTER_X, NEW_GAME_Y, NEW_GAME_TEXT);
        StdDraw.text(MENU_CENTER_X, LOAD_GAME_Y, LOAD_GAME_TEXT);
        StdDraw.text(MENU_CENTER_X, QUIT_GAME_Y, QUIT_TEXT);

        font = new Font(FONT_NAME, Font.BOLD, TITLE_FONT_SIZE);
        StdDraw.setFont(font);
        StdDraw.text(MENU_CENTER_X, MENU_TITLE_Y, TITLE_TEXT);
        StdDraw.show();
    }

    /**
     * 绘制种子输入界面
     */
    private void drawSeedInput(String s) {
        StdDraw.clear(Color.BLACK);
        Font font = new Font(FONT_NAME, Font.BOLD, MENU_FONT_SIZE);
        StdDraw.setFont(font);

        StdDraw.text(MENU_CENTER_X, NEW_GAME_Y, NEW_GAME_TEXT);
        StdDraw.text(MENU_CENTER_X, LOAD_GAME_Y, LOAD_GAME_TEXT);
        StdDraw.text(MENU_CENTER_X, QUIT_GAME_Y, QUIT_TEXT);

        font = new Font(FONT_NAME, Font.BOLD, TITLE_FONT_SIZE);
        StdDraw.setFont(font);
        StdDraw.text(MENU_CENTER_X, MENU_TITLE_Y, TITLE_TEXT);

        font = new Font(FONT_NAME, Font.BOLD, SEED_PROMPT_FONT_SIZE);
        StdDraw.setFont(font);
        StdDraw.text(MENU_CENTER_X, SEED_PROMPT_Y, SEED_PROMPT_TEXT);
        StdDraw.text(MENU_CENTER_X, SEED_INPUT_Y, s);
        StdDraw.show();
    }

    /**
     * 游戏主循环
     */
    private void gameLoop() {
        while (true) {
            if (StdDraw.hasNextKeyTyped()) {
                char c = Character.toUpperCase(StdDraw.nextKeyTyped());
                switch (c) {
                    case KEY_NEW_GAME:
                        seed = Long.parseLong(handleNewGame());
                        Random random = new Random(seed);
                        StaticRandomWorld worldGen = new StaticRandomWorld(width, height);
                        world = worldGen.staticRandomWorld(random);

                        int[] player = worldGen.generatePlayer(world, random);
                        TERenderer ter = new TERenderer();
                        ter.initialize(width, height);
                        ter.renderFrame(world);
                        moving(ter, player);
                        return;
                    case KEY_LOAD_GAME:
                        System.out.println("Load Game (TODO)");
                        loadWorld();
                        return;
                    case KEY_QUIT:
                        System.exit(0);
                        return;
                    default:
                        break;
                }
            }
            StdDraw.pause(PAUSE_DELAY_MS);
        }
    }

    private TETile[][] getWorld() {
        return world;
    }

    private void moving(TERenderer ter, int[] playerPosition) {
        boolean waitingForQ = false;

        while (true) {
            if (StdDraw.hasNextKeyTyped()) {
                char c = Character.toUpperCase(StdDraw.nextKeyTyped());

                if (waitingForQ) {
                    if (c == KEY_SAVE_SUFFIX) {
                        save(playerPosition);
                        System.exit(0);
                    }
                    waitingForQ = false;
                }

                switch (c) {
                    case KEY_UP:
                        move(0, 1, playerPosition);
                        break;
                    case KEY_DOWN:
                        move(0, -1, playerPosition);
                        break;
                    case KEY_LEFT:
                        move(-1, 0, playerPosition);
                        break;
                    case KEY_RIGHT:
                        move(1, 0, playerPosition);
                        break;
                    case KEY_SAVE_PREFIX:
                        waitingForQ = true;
                        break;
                    default:
                        break;
                }
            }
            drawHUD();
            StdDraw.pause(MOVE_DELAY_MS);
            ter.renderFrame(world);
        }
    }

    /**
     * 移动方法：尝试移动玩家，如果成功就更新位置并重绘。
     */
    private void move(int dx, int dy, int[] playerPosition) {
        int newX = playerPosition[0] + dx;
        int newY = playerPosition[1] + dy;

        if (swap(playerPosition[0], playerPosition[1], newX, newY)) {
            playerPosition[0] = newX;
            playerPosition[1] = newY;
        }
    }

    private boolean swap(int playerX, int playerY, int targetX, int targetY) {
        if (world[playerX][playerY].equals(Tileset.AVATAR)
                && world[targetX][targetY].equals(Tileset.FLOOR)) {
            world[playerX][playerY] = Tileset.FLOOR;
            world[targetX][targetY] = Tileset.AVATAR;
            return true;
        }
        return false;
    }

    /**
     * 处理新游戏输入种子
     */
    private String handleNewGame() {
        StringBuilder sb = new StringBuilder();
        while (true) {
            if (StdDraw.hasNextKeyTyped()) {
                char s = Character.toUpperCase(StdDraw.nextKeyTyped());
                if (s >= '0' && s <= '9') {
                    sb.append(s);
                }
                drawSeedInput(sb.toString());
                if (s == 'S') {
                    break;
                }
            }
        }
        return sb.toString();
    }

    private void drawHUD() {
        StdDraw.setPenColor(Color.WHITE);
        StdDraw.setFont(new Font(FONT_NAME, Font.BOLD, HUD_FONT_SIZE));
        int mx = (int) StdDraw.mouseX();
        int my = (int) StdDraw.mouseY();
        if (mx >= 0 && mx < width && my >= 0 && my < height) {
            String tileInfo = world[mx][my].description();
            StdDraw.textLeft(HUD_TEXT_LEFT_X, HUD_TEXT_Y, TILE_INFO_PREFIX + tileInfo);
        }

        StdDraw.textRight(HUD_TEXT_RIGHT_X, HUD_TEXT_Y, SAVE_QUIT_TEXT);
        StdDraw.show();
    }

    private void save(int[] playerPosition) {
        Path file = Paths.get(SAVE_FILE);
        String gameSave;
        gameSave = seed + " ";
        gameSave = gameSave + playerPosition[0] + " " + playerPosition[1];
        System.out.println(gameSave);
        try {
            Files.writeString(file, gameSave,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadWorld() {
        Path file = Paths.get(SAVE_FILE);
        String loadString = "";

        try {
            loadString = Files.readString(file); // 一次性读取整个文件内容
            System.out.println(loadString);
        } catch (IOException e) {
            e.printStackTrace();
        }

        String[] loadChar = loadString.split(" ");
        seed = Long.parseLong(loadChar[0]);
        int x = Integer.parseInt(loadChar[1]);
        int y = Integer.parseInt(loadChar[2]);
        int[] playerPosition = new int[]{x, y};

        Random random = new Random(seed);

        StaticRandomWorld worldGen = new StaticRandomWorld(width, height);
        world = worldGen.staticRandomWorld(random);
        StaticRandomWorld.generatePlayer(world, playerPosition);
        TERenderer ter = new TERenderer();
        ter.initialize(width, height);
        ter.renderFrame(world);
        moving(ter, playerPosition);
    }

    private int[] loadWorldLogic() {
        Path file = Paths.get(SAVE_FILE);
        String loadString = "";

        try {
            loadString = Files.readString(file);
        } catch (IOException e) {
            e.printStackTrace();
        }

        String[] loadChar = loadString.split(" ");
        seed = Long.parseLong(loadChar[0]);
        int x = Integer.parseInt(loadChar[1]);
        int y = Integer.parseInt(loadChar[2]);
        int[] playerPosition = new int[]{x, y};

        Random random = new Random(seed);
        StaticRandomWorld worldGen = new StaticRandomWorld(width, height);
        world = worldGen.staticRandomWorld(random);
        StaticRandomWorld.generatePlayer(world, playerPosition);
        return playerPosition;
    }

    public TETile[][] interactWithString(String input) {
        input = input.toUpperCase();
        int index = 0;
        int[] player;
        boolean waitingForQ = false;
        while (index < input.length()) {
            char c = input.charAt(index);
            switch (c) {
                case KEY_NEW_GAME:
                    // 读种子
                    StringBuilder sb = new StringBuilder();
                    index++;
                    while (index < input.length()) {
                        char s = input.charAt(index);
                        if (s >= '0' && s <= '9') {
                            sb.append(s);
                            index++;
                        } else if (s == 'S') {
                            index++;
                            break;
                        } else {
                            index++;
                        }
                    }
                    seed = Long.parseLong(sb.toString());
                    Random random = new Random(seed);
                    StaticRandomWorld worldGen = new StaticRandomWorld(width, height);
                    world = worldGen.staticRandomWorld(random);
                    player = worldGen.generatePlayer(world, random);
                    // 执行后续命令 (WASD :Q)
                    while (index < input.length()) {
                        char move = input.charAt(index++);
                        if (waitingForQ) {
                            if (move == KEY_SAVE_SUFFIX) {
                                save(player);
                                return world; // 保存后退出
                            }
                            waitingForQ = false;
                        }
                        switch (move) {
                            case KEY_UP:    move(0, 1, player); break;
                            case KEY_DOWN:  move(0, -1, player); break;
                            case KEY_LEFT:  move(-1, 0, player); break;
                            case KEY_RIGHT: move(1, 0, player); break;
                            case KEY_SAVE_PREFIX: waitingForQ = true; break;
                            default: break;
                        }
                    }
                    return world;
                case KEY_LOAD_GAME:
                    player = loadWorldLogic();
                    while (index < input.length()) {
                        char move = input.charAt(index++);
                        if (waitingForQ) {
                            if (move == KEY_SAVE_SUFFIX) {
                                save(player);
                                return world; // 保存后退出
                            }
                            waitingForQ = false;
                        }
                        switch (move) {
                            case KEY_UP:    move(0, 1, player); break;
                            case KEY_DOWN:  move(0, -1, player); break;
                            case KEY_LEFT:  move(-1, 0, player); break;
                            case KEY_RIGHT: move(1, 0, player); break;
                            case KEY_SAVE_PREFIX: waitingForQ = true; break;
                            default: break;
                        }
                    }
                    return world;
                case KEY_QUIT:
                    return world;
                default:
                    index++;
                    break;
            }
        }
        return world;
    }
}
