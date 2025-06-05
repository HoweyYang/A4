
import java.net.*;
import java.util.List;

public class Client{
    // Initial timeout in milliseconds for socket operations
    private static final int TIMEOUT = 1000;
    // Maximum number of retries for failed requests
    private static final int MAX_RETRIES = 5;
    public static void main(String[] args){

    }
    // Reads the list of filenames from the specified file
    private static List<String> readFileList(String path){
        return null;

    }
    // Sends a request to the server with retry logic and receives a response
    private static String retrySendReceive(DatagramSocket socket, InetAddress addr, int port, String msg){
        return null;
    }
    // Downloads a file from the server in chunks
    private static void downloadFile(DatagramSocket socket,int port,String fileName, long fileSize){

    }

}