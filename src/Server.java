// ----- IMPORT STATEMENTS -----
import java.net.*;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.io.*;

public class Server {

    public static void main(String[] args) throws IOException {
        ArrayList<MultipleServer> clientList = new ArrayList<MultipleServer>();

        // if statement to check that port number is valid
        if (args.length != 1) {
            System.err.println("Usage: java Server <port number>");
            System.exit(1);
        } // end of if statement

        int portNum = Integer.parseInt(args[0]);

        try(ServerSocket serverSocket = new ServerSocket(portNum)){
            // --- while loop to listen to connection requests and
            // start them as new threads
            while(true){
                Socket clientSocket = serverSocket.accept();
                clientList.add(new MultipleServer(clientSocket));
                clientList.get(clientList.size() - 1).start();
            } // end of while loop
        } catch (IOException e) {
            System.err.println("Unable to reach port " + portNum);
            System.exit(1);
        } // end of IOException catch
    } // end of main() method

}

class MultipleServer extends Thread {
    private Socket multiSocket;
    private PrintWriter serverWriter;
    private BufferedReader serverReader;

    public MultipleServer(Socket multiSocket){
        this.multiSocket = multiSocket;
    } // end of CONSTRUCTOR

    public void run(){
        try{
            // setup I/O streams to be able to send/receive data from client
            serverWriter = new PrintWriter(multiSocket.getOutputStream(), true);
            InputStreamReader serverStreamReader = new InputStreamReader(multiSocket.getInputStream());
            serverReader = new BufferedReader(serverStreamReader);

            String test = "Connected to Server";

            readFromClient();

        } catch (IOException e){
            e.printStackTrace();
        } // end of IOException catch
    } // end of run() method

    private void readFromClient(){
        boolean finished = false;
        String clientMsg = "";

        // --- while loop to read input from client
        // continually
        while(!finished){
            try{
                clientMsg = serverReader.readLine();
                System.out.println("Reading from client...");
            } catch (IOException e){
                System.err.println("I/O Error on Server!");
                e.printStackTrace();
            } // end of IOException catch

            // -- if statement to detect if client is
            // submitting message or wanting to quit
            if(clientMsg != null){
                System.out.println("Echoing back to client...");
                serverWriter.println("Echo: " + clientMsg);
            }else{
                finished = true;

            } // end of if-else statement
        } // end of while loop
    } // end of readFromClient() method
} // end of MultipleServer Class
