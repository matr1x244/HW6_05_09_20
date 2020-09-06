import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    private final static String SERVER_IP = "localhost";
    private final static int SERVER_PORT = 8181;

    private static Socket socket;
    private static DataInputStream in;
    private static DataOutputStream out;

    private static boolean isRunning; // true; false;

    public static void main(String[] args) {
        try {
            connectServer ();
            closeConnection ();
        } catch (IOException e) {
            e.printStackTrace ();
        }
    }

    private static void connectServer() throws IOException {
        socket = new Socket (SERVER_IP, SERVER_PORT);
        in = new DataInputStream (socket.getInputStream ());
        out = new DataOutputStream (socket.getOutputStream ());

        isRunning = true;

        // действия потоков

        Thread OneIn = new Thread (() -> { // поток
            try {
                while (isRunning) {
                    if (!socket.isConnected ()) {
                        System.out.println ("Сервер закрылся");
                        isRunning = false;
                        break;
                    }

                    String strFromServer = in.readUTF ();

                    if (strFromServer.equalsIgnoreCase ("/end")) {
                        System.out.println ("Сервер закрылся");
                        isRunning = false;
                        break;
                    }

                    System.out.println ("Сервер пишет: " + strFromServer);
                }
            } catch (Exception e) {
                e.printStackTrace ();
            }
        });
        Thread TwoOut = new Thread (() -> { // поток
            Scanner sc = new Scanner (System.in);
            try {
                while (isRunning) {
                    if (!socket.isConnected ()) {
                        isRunning = false;
                        break;
                    }

                    String str = sc.nextLine ();
                    out.writeUTF (str);

                    if (str.equals ("/end")) {
                        isRunning = false;
                        break;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace ();
            }
        });

        //запуск потоков

        OneIn.start ();
        TwoOut.start ();

        try {
            OneIn.join (); // join пока кто то не напишет
            TwoOut.join ();
        } catch (InterruptedException e) {
            e.printStackTrace ();
        }
    }

    public static void closeConnection() throws IOException {
        in.close ();
        out.close ();

        if (!socket.isClosed ()) {
            socket.close ();
        }
    }
}