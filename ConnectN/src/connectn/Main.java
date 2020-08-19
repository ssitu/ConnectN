package connectn;

import java.awt.GraphicsEnvironment;
import java.io.*;

public class Main {

    public static void main(String[] args) throws IOException {
        Console console = System.console();
        String filename = new Object() {
        }.getClass().getProtectionDomain().getCodeSource().getLocation().toString().substring(6);
        if (console == null && !GraphicsEnvironment.isHeadless() && filename.endsWith(".jar")) {
            Runtime.getRuntime().exec(new String[]{"cmd", "/c", "start", "cmd", "/k", "java -jar \"" + filename + "\""});
        } else {
            Game game = new Game(13, 11, 7);
            game.waitForNewPeer(7777, 0);
            game.play();
        }
    }
}
