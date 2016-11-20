// ----- IMPORT STATEMENTS -----
import java.net.*;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.HashSet;
import java.io.*;

public class Server {
    private static HashSet<String> usrNameSet = new HashSet<String>();
    final private long SERVER_START_TIME;

    /** Empty Constructor to create Server object.
     * Only used to allow instantiation of
     * Multiple Server inner class. */
    public Server(){
        SERVER_START_TIME = System.currentTimeMillis();

    } // end of CONSTRUCTOR

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
                Server server = new Server(); // needed to create instance of inner class MultipleServer
                Server.MultipleServer multiServer = server.new MultipleServer(clientSocket, server);
                clientList.add(multiServer);
                clientList.get(clientList.size() - 1).start();
            } // end of while loop
        } catch (IOException e) {
            System.err.println("Unable to reach port " + portNum);
            System.exit(1);
        } // end of IOException catch
    } // end of main() method

    private class MultipleServer extends Thread {
        private Socket multiSocket;
        private Server server;
        private PrintWriter serverWriter;
        private BufferedReader serverReader;
        //private HashSet<String> usrNameSet = new HashSet<String>();

        public MultipleServer(Socket multiSocket, Server outerServer){
            this.multiSocket = multiSocket;
            this.server = outerServer;
        } // end of CONSTRUCTOR

        public void run(){
            try{
                // setup I/O streams to be able to send/receive data from client
                serverWriter = new PrintWriter(multiSocket.getOutputStream(), true);
                InputStreamReader serverStreamReader = new InputStreamReader(multiSocket.getInputStream());
                serverReader = new BufferedReader(serverStreamReader);

                System.out.println("Client Connected");
                serverWriter.println("--- Connected to Server ---\n");
                serverWriter.flush();

                createUsername();

                readFromClient();

            } catch (IOException e){
                e.printStackTrace();
            } // end of IOException catch
        } // end of run() method

        private void createUsername(){
            String clientUsrName = null;

            // --- do-while loop to ask for username and check that
            // it is unique against other entries in the HashSet
            do{
                serverWriter.println("Please Enter a Username:- ");
                serverWriter.flush();
                System.out.println("Asking for username");
                try{
                clientUsrName = serverReader.readLine();
                } catch(IOException e){
                    System.err.println("I/O Error at Username Creation");
                    e.printStackTrace();
                }
            }while(Server.usrNameSet.add(clientUsrName) == false && clientUsrName != null);
        } // end of createUsername() method

        /** Method to get the number of users online.
         * Method returns the size of the HashSet holding each username.
         * @return Size of HashSet storing each username. */
        private int getUsrNum(){
            return usrNameSet.size();
        } // end of getUsrNum() method

        /** Method to return the uptime of the server.
         * Method get the current system time and minuses the time set
         * when the Server constructor was called on server
         * startup to calculate running time.
         * @return The server uptime in milliseconds. */
        private long getServerUptime(){
            return System.currentTimeMillis() - SERVER_START_TIME;
        } // end of getServerUptime() method

        /** Method to return server IP address.
         * Method creates {@link InetAddress InetAddress} object and
         * then uses {@link InetAddress#getHostAddress() getHostAddress}
         * from the {@link InetAddress InetAddress} class to get the
         * server's IP address.
         * @return The IP address of the server. */
        private String getServerIP(){
            InetAddress ip = null;
            try{
                ip = InetAddress.getLocalHost();
            } catch (UnknownHostException e){
                System.err.println("Unknown Host Error: No Server IP");
            } // end of UnknownHostException catch

            return ip.getHostAddress();
        } // end of getServerIP() method

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

                // check to see if user input is a specific request
                // i.e starts with ;
                if(clientMsg.charAt(0) == ';'){
                    switch(clientMsg){
                        case ";e": // user requested exit
                        case ";exit":
                            serverWriter.println("Logging out...");
                            break;
                        case ";ip":
                        case ";ip_addr":
                            serverWriter.println("Server IP Address: " + getServerIP());
                        case ";ut": // user requested uptime status
                        case ";uptime":
                            serverWriter.println("Server Uptime: " + getServerUptime() + " milliseconds");
                            break;
                        case ";un": // user requested number of online users
                        case ";usr_num":
                            serverWriter.println("Users online: " + getUsrNum());
                            break;
                    } // end of switch statement
                }else{
                    System.out.println("Echoing back to client...");
                    serverWriter.println("Echo: " + clientMsg);
                }// end of if statement
                serverWriter.flush();
            } // end of while loop
        } // end of readFromClient() method
    } // end of MultipleServer Class
} // end of Server Class
