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
        while(args.length != 1){
            System.err.println("Incorrect Port Number");
            System.out.println("enter a working port number"); // todo: needs Scanner setting up
        } // end of if statement

        final int PORT_NUM = Integer.parseInt(args[0]);

        try(ServerSocket serverSocket = new ServerSocket(PORT_NUM)){
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
            System.err.println("Unable to reach port " + PORT_NUM);
            System.exit(1);
        } // end of IOException catch
    } // end of main() method

    private class MultipleServer extends Thread {
        private Socket multiSocket;
        private Server server;
        private long clientChatroomTime;
        private PrintWriter serverWriter;
        private BufferedReader serverReader;

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
                serverWriter.println("--- Connected to Server ---");
                serverWriter.println("type \';h\' for help\n\n");
                serverWriter.flush();

                setUsrName();

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

                parseClientMsg(clientMsg);
                serverWriter.println();
            } // end of while loop
        } // end of readFromClient() method

        private void parseClientMsg(final String MSG){
            // check to see if user input is a specific request
            // i.e starts with ;
            if(MSG.charAt(0) == ';'){
                clientCommand(MSG);
            }else{
                serverWriter.print("\u2713");
                // code to broadcast messages
                serverWriter.print("\u2713");
            } // end of if statement

            serverWriter.flush();
        } // end of parseUsrMsg() method

        private void clientCommand(final String CMD){
            switch(CMD){
                case ";cut":
                case ";client_ut":
                    serverWriter.println("Time in chatroom: " + getClientChatroomTime() + " seconds");
                    break;
                case ";e": // user requested exit
                case ";exit":
                    serverWriter.println("Logging out...");
                    break;
                case ";h":
                case ";help":
                    getHelpCommands();
                    break;
                case ";ip":
                case ";ip_addr":
                    serverWriter.println("Server IP Address: " + getServerIP());
                    break;
                case ";ut": // user requested uptime status
                case ";uptime":
                    serverWriter.println("Server Uptime: " + getServerUptime() + " seconds");
                    break;
                case ";un": // user requested number of online users
                case ";usr_num":
                    serverWriter.println("Users online: " + getUsrNum());
                    break;
                default:
                    serverWriter.println("unknown command: type \';h\' for help");
            } // end of switch statement
        } // end of clientCommand() method

        // ---- GETTERS ----

        private double getClientChatroomTime(){
            return (System.currentTimeMillis() - clientChatroomTime) / 1000;
        } // end of getClientChatroomTime() method

        private void getHelpCommands(){
            serverWriter.println(";cut \t;client_ut \t get uptime of the client");
            serverWriter.println(";e \t;exit \t\t log out and exit from chatroom");
            serverWriter.println(";h \t;help \t\t print help commands to terminal");
            serverWriter.println(";ip \t;ip_addr \t get IP address of the server");
            serverWriter.println(";un \t;usr_num \t get number of online users");
            serverWriter.println(";ut \t;uptime \t get uptime of the server");

            serverWriter.flush();
        } // end of getHelpCommands() method

        /** Method to return server IP address.
         * Method creates {@link InetAddress InetAddress} object and
         * then uses {@link InetAddress#getHostAddress() getHostAddress}
         * from the {@link InetAddress InetAddress} class to get the
         * server's IP address.
         * @return The IP address of the server. */
        private String getServerIP(){
            final InetAddress IP;
            try{
                IP = InetAddress.getLocalHost();
                return IP.getHostAddress();
            } catch (UnknownHostException e){
                System.err.println("Unknown Host Error: No Server IP");
            } // end of UnknownHostException catch

            return "IP Address Unavailable";
        } // end of getServerIP() method

        /** Method to return the uptime of the server.
         * Method get the current system time and minuses the time set
         * when the Server constructor was called on server
         * startup to calculate running time.
         * @return The server uptime in milliseconds. */
        private double getServerUptime(){
            return (System.currentTimeMillis() - SERVER_START_TIME) / 1000;
        } // end of getServerUptime() method

        /** Method to get the number of users online.
         * Method returns the size of the HashSet holding each username.
         * @return Size of HashSet storing each username. */
        private int getUsrNum(){
            return usrNameSet.size();
        } // end of getUsrNum() method

        // ---- SETTERS ----

        /** Method to set a unique username for the client
         * attempting to joing the chatroom.
         * Method takes user input and checks that username
         * is unique against those already stored in the
         * {@link HashSet HashSet}. */
        private void setUsrName(){
            String clientUsrName = null;

            // --- do-while loop to ask for username and check that
            // it is unique against other entries in the HashSet
            do{
                serverWriter.println("Please Enter a Username:- ");
                System.out.println("Asking for username");
                serverWriter.flush();
                try{
                    clientUsrName = serverReader.readLine();
                }catch(IOException e){
                    System.err.println("I/O Error at Username Creation");
                    e.printStackTrace();
                } // end of IOException catch
            }while(Server.usrNameSet.add(clientUsrName) == false && clientUsrName != null);

            // adds system time for when client entered the chatroom
            clientChatroomTime = System.currentTimeMillis();
            serverWriter.println("--- Entered Chatroom ---\n");
        } // end of setUsrName() method
    } // end of MultipleServer Class
} // end of Server Class
