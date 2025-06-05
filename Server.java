import java.io.*;
import java.net.*;

public class Server{
    public static void main(String[] args){

    }
    // Handles a client's download request in a seperate thread
    private static void handleRequest(DatagramSocket welcomeSocket,InetAddress addr, int port, String filename) {

    }
    //Processes data requests(GET/CLOSE)from the client
    private static void handleDataRequests(DatagramSocket socket, InetAddress addr, RandomAccessFile raf, long fileSize){

    }
    //Sends a response to the client
    private static void sendResponse(DatagramSocket socket, InetAddress addr, int port, String msg){

    }
}
