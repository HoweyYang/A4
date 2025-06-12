import java.io.*;
import java.net.*;
import java.util.Base64;
import java.util.Random;
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
            System.out.println("Server running on port:" + " " +port);
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
    private static void handleRequest(DatagramSocket welcomeSocket,InetAddress addr, int port, String fileName) {
        File file = new File(fileName); // Create File object for requested file
        if (!file.exists()) { // Check if file exists on server
            sendResponse(welcomeSocket, addr, port, "ERR " + fileName + " NOT_FOUND"); // Send error response
            return; // Exit method if file not found
        }

        try {
            int dataPort = 50000 + new Random().nextInt(1001); // Assign random port between 50000-51000 for data transfer
            try (DatagramSocket dataSocket = new DatagramSocket(dataPort)) { // Create socket for data transfer
                long fileSize = file.length(); // Get size of the file
                String response = "OK " + fileName + " SIZE " + fileSize + " PORT " + dataPort; // Build OK response
                sendResponse(welcomeSocket, addr, port, response); // Send response to client

                System.out.println("Serving " + fileName + " to " + addr + ":" + port); // Log client request
                try (RandomAccessFile raf = new RandomAccessFile(file, "r")) { // Open file for reading
                    handleDataRequests(dataSocket, addr, raf, fileSize); // Process data requests from client
                }
            }
        } catch (IOException e) {
            System.out.println("Request error: " + e.getMessage()); // Log errors during request handling
        }
    }
    //Processes data requests(GET/CLOSE)from the client
    private static void handleDataRequests(DatagramSocket socket, InetAddress addr, RandomAccessFile raf, long fileSize) throws SocketException, IOException{
        socket.setSoTimeout(5000); // Set 5-second timeout to detect inactive clients
        while (true) { // Loop to handle multiple requests
            byte[] buffer = new byte[4096]; // Buffer for incoming requests
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length); // Packet for receiving data
            try {
                socket.receive(packet); // Wait for client request
            } catch (SocketTimeoutException e) {
                continue; // Continue waiting if timeout occurs
            }

            String request = new String(packet.getData(), 0, packet.getLength()).trim(); // Extract request message
            String[] parts = request.split(" "); // Split request into components
            int clientPort = packet.getPort(); // Get client port

            // Handle CLOSE request to terminate connection
            if (parts.length == 3 && parts[2].equals("CLOSE")) {
                sendResponse(socket, addr, clientPort, "FILE " + parts[1] + " CLOSE_OK"); // Confirm closure
                break; // Exit loop and terminate thread
            }
            // Handle GET request for file data
            else if (parts.length == 7 && parts[2].equals("GET")) {
                long start = Long.parseLong(parts[4]); // Parse start byte
                long end = Long.parseLong(parts[6]); // Parse end byte
                if (start < 0 || end >= fileSize || start > end) { // Validate byte range
                    sendResponse(socket, addr, clientPort, "ERR Invalid range"); // Send error for invalid range
                    continue; // Skip to next request
                }
                raf.seek(start); // Move file pointer to start position
                int length = (int) (end - start + 1); // Calculate length of data chunk
                byte[] data = new byte[length]; // Buffer for file data
                raf.readFully(data); // Read requested data from file
                String encoded = Base64.getEncoder().encodeToString(data); // Encode data to Base64
                String response = "FILE " + parts[1] + " OK START " + start + " END " + end + " DATA " + encoded; // Build response
                sendResponse(socket, addr, clientPort, response); // Send data to client
            } 
        }
        System.out.println("Client " + addr + " disconnected"); // Log client disconnection
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
