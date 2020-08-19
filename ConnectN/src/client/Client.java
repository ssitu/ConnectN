package client;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Client {

    public static void main(String[] args) throws IOException {
        Console console = System.console();
        String jarPath = new Object() {
        }.getClass().getProtectionDomain().getCodeSource().getLocation().toString().substring(6);
        if (console == null && !java.awt.GraphicsEnvironment.isHeadless() && jarPath.endsWith(".jar")) {
            Runtime.getRuntime().exec(new String[]{"cmd", "/c", "start", "cmd", "/k", "java -jar \"" + jarPath + "\""});
        } else {
            try {
                Scanner sc = new Scanner(System.in);
                System.out.println("Enter an ip");
                String ip = sc.nextLine();
                System.out.println("Enter a port");
                int port = sc.nextInt();
                sc.nextLine();
                Socket s = new Socket(ip, port);
                String address = s.getInetAddress().toString();
                System.out.println("Connected with " + address);
                BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
                PrintStream out = new PrintStream(s.getOutputStream());
                Thread t = new Thread(() -> {
                    while (true) {
                        try {
                            System.out.println(in.readLine());
                        } catch (java.net.SocketException se) {
                            System.out.println(se + "; Lost connection with host");
                            System.exit(0);
                        } catch (Exception e) {
                            System.out.println(e);
                            System.exit(0);
                        }
                    }
                });
                t.setDaemon(true);
                t.start();
                while (true) {
                    try {
                        out.println(sc.nextLine());
                    } catch (Exception e) {
                        System.out.println(e);
                        System.exit(0);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
