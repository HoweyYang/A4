import java.io.*;
import java.net.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
public class Server{
    public static void main(String[] args){
         if (args.length != 1) {
            System.out.println("Usage: java Server <port>");
            System.exit(1); // Exit with error code if port is not provided
        }
        // Parse port number from command line
        int port = Integer.parseInt(args[0]); 
        // Create thread pool for concurrent request handling
        ExecutorService pool = Executors.newCachedThreadPool(); 

        try (DatagramSocket welcomeSocket = new DatagramSocket(port)) {
            //Create UDP socket on specified port

            //Log server startup
            System.out.println("Server running on port" + port);
            while(true){
                byte[] buffer = new byte[4096]; // Buffer for incoming data
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length); // Packet to receive data
                welcomeSocket.receive(packet); // Block until a packet is received
                String request = new String(packet.getData(), 0, packet.getLength()).trim(); // Extract request message
                InetAddress clientAddr = packet.getAddress(); // Get client IP address
                int clientPort = packet.getPort(); // Get client port number

                //process DOWNLOAD request
                if (request.startsWith("DOWNLOAD")) {
                     String[] parts = request.split(" ", 2); // Split into command and filename
                    if (parts.length == 2) { // Ensure filename is provided
                        String filename = parts[1]; // Extract filename
                        // Submit request handling to thread pool
                        pool.submit(() -> handleRequest(welcomeSocket, clientAddr, clientPort, filename));
                    }
                }
            }
        }catch (IOException e) {
            System.out.println("Server error: " + e.getMessage()); // Log any IO exceptions
        }
        pool.shutdown(); // Shutdown thread pool when server stops
    }
    // Handles a client's download request in a seperate thread
    private static void handleRequest(DatagramSocket welcomeSocket,InetAddress addr, int port, String filename) {

    }
    //Processes data requests(GET/CLOSE)from the client
    private static void handleDataRequests(DatagramSocket socket, InetAddress addr, RandomAccessFile raf, long fileSize){

    }
    //Sends a response to the client
    private static void sendResponse(DatagramSocket socket, InetAddress addr, int port, String msg){
        try {
            byte[] data = msg.getBytes(); // Convert message to byte array
            DatagramPacket packet = new DatagramPacket(data, data.length, addr, port); // Create response packet
            socket.send(packet); // Send packet to client
        } catch (IOException e) {
            System.out.println("Send error: " + e.getMessage()); // Log send errors
        }
    }
}
