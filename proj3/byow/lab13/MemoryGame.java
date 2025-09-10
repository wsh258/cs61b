package byow.lab13;

import edu.princeton.cs.introcs.StdDraw;

import java.awt.Color;
import java.awt.Font;
import java.util.Random;

public class MemoryGame {
    /** The width of the window of this game. */
    private int width;
    /** The height of the window of this game. */
    private int height;
    /** The current round the user is on. */
    private int round;
    /** The Random object used to randomly generate Strings. */
    private Random rand;
    /** Whether or not the game is over. */
    private boolean gameOver;
    /** Whether or not it is the player's turn. Used in the last section of the
     * spec, 'Helpful UI'. */
    private boolean playerTurn;
    /** The characters we generate random Strings from. */
    private static final char[] CHARACTERS = "abcdefghijklmnopqrstuvwxyz".toCharArray();
    /** Encouraging phrases. Used in the last section of the spec, 'Helpful UI'. */
    private static final String[] ENCOURAGEMENT = {"You can do this!", "I believe in you!",
                                                   "You got this!", "You're a star!", "Go Bears!",
                                                   "Too easy for you!", "Wow, so impressive!"};

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Please enter a seed");
            return;
        }

        long seed = Long.parseLong(args[0]);
        MemoryGame game = new MemoryGame(40, 40, seed);
        game.startGame();
    }

    public MemoryGame(int width, int height, long seed) {
        /* Sets up StdDraw so that it has a width by height grid of 16 by 16 squares as its canvas
         * Also sets up the scale so the top left is (0,0) and the bottom right is (width, height)
         */
        this.width = width;
        this.height = height;
        StdDraw.setCanvasSize(this.width * 16, this.height * 16);
        Font font = new Font("Monaco", Font.BOLD, 30);
        StdDraw.setFont(font);
        StdDraw.setXscale(0, this.width);
        StdDraw.setYscale(0, this.height);
        StdDraw.clear(Color.BLACK);
        StdDraw.enableDoubleBuffering();

        //TODO: Initialize random number generator
        rand = new Random(seed);
    }

    public String generateRandomString(int n) {
        //TODO: Generate random string of letters of length n
        StringBuilder sb = new StringBuilder(n);
        for (int i = 0; i < n; i++) {
            int index = rand.nextInt(CHARACTERS.length);
            sb.append(CHARACTERS[index]);
        }
        return sb.toString();
    }

    public void drawFrame(String s) {
        //TODO: Take the string and display it in the center of the screen
        StdDraw.clear(Color.BLACK);

        StdDraw.enableDoubleBuffering();

        StdDraw.text(this.width / 2.0, this.height / 2.0 , s);

        //TODO: If game is not over, display relevant game information at the top of the screen
        if (!gameOver) {
            StdDraw.textLeft(0, this.height - 1, "Round: " + round);
            int index = rand.nextInt(ENCOURAGEMENT.length);
            StdDraw.textRight(this.width, this.height - 1, ENCOURAGEMENT[index]);
        }

        if (playerTurn) {
            StdDraw.textLeft(width / 2.0 - 3, this.height - 1, "Type!");
        } else {
            StdDraw.textLeft(width / 2.0 - 3, this.height - 1, "Watch!");
        }
        StdDraw.setPenColor(Color.WHITE); // 设置颜色
        StdDraw.line(0, height - 2, width, height - 2);       // 起点 (x1, y)，终点 (x2, y)

        StdDraw.show();
    }

    public void flashSequence(String letters) {
        playerTurn = true;
        //TODO: Display each character in letters, making sure to blank the screen between letters
        for (int i = 0; i < letters.length(); i++) {
            // 显示第 i 个字符
            String ch = Character.toString(letters.charAt(i));
            drawFrame(ch);       // 用你写的 drawFrame 居中显示
            StdDraw.pause(1000); // 显示 1 秒
            // 清空屏幕，空白间隔
            drawFrame("");

            StdDraw.pause(500);  // 空白 0.5 秒
        }

    }

    public String solicitNCharsInput(int n) {
        //TODO: Read n letters of player input
        StringBuilder sb = new StringBuilder();
        playerTurn = false;
        while (sb.length() < n) {
            if (StdDraw.hasNextKeyTyped()) {
                char c = StdDraw.nextKeyTyped();  // 取出一个按键
                sb.append(c);

                // 每次更新画面
                drawFrame(sb.toString());
            }
        }
        return sb.toString();
    }

    public void startGame() {
        //TODO: Set any relevant variables before the game starts
        StdDraw.clear(Color.BLACK);
        StdDraw.setPenColor(Color.WHITE);
        while (!gameOver) {
            String randomLetters = generateRandomString(round);
            flashSequence(randomLetters);
            if (!solicitNCharsInput(round).equals(randomLetters)) {
                gameOver = true;
            } else {
                round ++;
                drawFrame("Round: " + round);

                StdDraw.pause(500);
            }
        }
        drawFrame("Game Over! You made it to round:" + round);
        //TODO: Establish Engine loop
    }

}
