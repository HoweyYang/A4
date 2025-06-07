import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

public class Client{
    // Initial timeout in milliseconds for socket operations
    private static final int TIMEOUT = 1000;
    // Maximum number of retries for failed requests
    private static final int MAX_RETRIES = 5;
    public static void main(String[] args){
        //validate command line arguments
        if(args.length != 3){
            System.out.println("Usage: java Client <server_ip> <server_port> <file_list.txt>");
            System.exit(1);// Exit if arguments are invalid
        }
        String serverIP = args[0];// Server IP address from command line
        int serverPort = Integer.parseInt(args[1]);// Server port from command line 
        String fileListPath = args[2];// Path to the file list from command line

        //Create UDP socket with auto-closing
        try (DatagramSocket socket = new DatagramSocket()){
            socket.setSoTimeout(TIMEOUT);// Set the initial socket timeout
            InetAddress serverAddr = InetAddress.getByName(serverIP);//Convert server IP to InetAddress
            List<String> files = readFileList(fileListPath);// Read the list of files to download

            //iterate through each file to request and download
            for(String fileName : files){
                System.out.println("Requesting: " + fileName);//Log the file request
                String request = "DOWNLOAD " + fileName; // Create the download request message
                String response = retrySendReceive(socket, serverAddr, serverPort, request); // Send request and get response

                //Process server response
                if (response.startsWith("ERR")){
                    System.out.println("Error:" + response);
                    continue; // Skip to next file if error occurs
                }
                String[] parts = response.split("\\s+");// Split the response into parts
                if(!parts[0].equals("OK")){
                    System.out.println("Unexpected response: " + response);
                    continue; // Skip to next file if response is not OK
                }
                long filesSize = Long.parseLong(parts[3]); // Parse the file size from response
                int dataPort = Integer.parseInt(parts[5]); // Parse the data port from response
                downloadFile(socket, dataPort, fileName, filesSize);
        }
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage()); // Log any IO exceptions
        }
    }
    // Reads the list of filenames from the specified file
    private static List<String> readFileList(String path){
       List<String> files = new ArrayList<>(); // List to store filenames
        try (BufferedReader reader = new BufferedReader(new FileReader(path))) { // Open file for reading
            String line;
            while ((line = reader.readLine()) != null) { // Read each line
                if (!line.trim().isEmpty()) { // Ignore empty lines
                    files.add(line.trim()); // Add trimmed filename to list
                }
            }
        } catch (IOException e) {
            System.out.println("File list error: " + e.getMessage()); // Log file reading errors
        }
        return files; // Return list of filenames
    }
    // Sends a request to the server with retry logic and receives a response
    private static String retrySendReceive(DatagramSocket socket, InetAddress addr, int port, String msg){
        int timeout = TIMEOUT; // Start with initial timeout
        for(int i = 0; i < MAX_RETRIES; i++){
            try {
                byte[] sendData = msg.getBytes(); // Convert request message to byte array
                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, addr, port); // Create packet
                socket.send(sendPacket); // Send request to server

                byte[] receiveData = new byte[4096]; // Buffer for response data
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length); // Packet for response
                socket.receive(receivePacket); // Wait for server response
                return new String(receivePacket.getData(), 0, receivePacket.getLength()).trim(); // Return trimmed response
            } catch (SocketTimeoutException e) {
                System.out.println("Timeout, retry " + (i + 1)); // Log timeout and retry attempt
                timeout *= 2; // Exponentially increase timeout for next attempt
                try {
                    socket.setSoTimeout(timeout); // Update socket timeout
                } catch (SocketException ignored) {
                    // Ignore exception while setting timeout
                }
            } catch (IOException e) {
                System.err.println("Error: " + e.getMessage()); // Log any IO exceptions
            }
        }
        throw new RuntimeException("Failed after" + MAX_RETRIES + " retries" + msg); // Throw exception if all retries fail
    }
    // Downloads a file from the server in chunks
    private static void downloadFile(DatagramSocket socket,int port,String fileName, long fileSize){

    }

}