import java.io.*;
import java.net.*;
import java.util.concurrent.*;
import java.util.Random;

public class SocketServer {
  static final int PORT = 17;
  static final int BUFFERSIZE = 1024;
  static final String[] QUOTES = new String[] {
    "Death cannot stop true love. All it can do is delay it for a while.",
    "I always think that everything could be a trap, which is why I'm still alive.",
    "My name is Inigo Montoya. You killed my father. Prepare to die.",
    "Lying is a form of love.",
  };
  static final Random RAND = new Random();

  public static void main(String[] args) {
    new Thread(new tcpServer()).start();
    new Thread(new udpServer()).start();
  }

  static class tcpServer implements Runnable{
    public void run() {
      try (
        ServerSocket server = new ServerSocket(PORT);
      ) {
        ExecutorService executorService = Executors.newFixedThreadPool(5);
        while (true) {
          Socket client = server.accept();
          executorService.submit(() -> handleClient(client));
        }
      } catch(Exception e) {
        System.out.println(e);
      }
    }

    private static void handleClient(Socket client) {
      try (
        PrintWriter out = new PrintWriter(client.getOutputStream(), true);
      ) {
        out.println(getQuote());
      } catch (Exception e) {
        System.out.println(e);
      }
    }
  }

  static class udpServer implements Runnable {
    public void run() {
      try (
        DatagramSocket sock = new DatagramSocket(PORT);
        ) {
          ExecutorService executorService = Executors.newFixedThreadPool(5);
          while (true) {
            DatagramPacket pack = new DatagramPacket(new byte[BUFFERSIZE], BUFFERSIZE);
            sock.receive(pack);
            executorService.submit(() -> handleClient(sock, pack));
          }
      } catch (Exception e) {
        System.out.println(e);
      }
    }

    private static void handleClient(DatagramSocket sock, DatagramPacket pack) {
      InetAddress address = pack.getAddress();
      int port = pack.getPort();
      byte[] buf = getQuote().getBytes();
      try {
        sock.send(new DatagramPacket(buf, buf.length, address, port));
      } catch (Exception e) {
        System.out.println(e);
      }
    }
  }

  public static String getQuote() {
    return QUOTES[(int) (RAND.nextDouble() * QUOTES.length)];
  }

}