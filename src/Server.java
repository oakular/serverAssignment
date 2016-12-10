// ----- IMPORT STATEMENTS -----
import java.net.*;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.HashSet;
import java.io.*;

/** Server class that allows for clients to connect to the central server.
 * Each new connection is started as a new thread via the nested class:
 * {@link MultipleServer MultipleServer}. The Server class has data structures
 * to store usernames and a reference to each client via the {@link HashSet HashSet}
 * and {@link ArrayList ArrayList} classes respectively. */
public class Server {
    /** {@link HashSet HashSet} to to the usernames of each
     * client; ensuring they are unique */
    private static HashSet<String> usrNameSet = new HashSet<String>();

    /** {@link ArrayList ArrayList} of each client for use in
     * message broadcasting */
    private static ArrayList<MultipleServer> clientList = new ArrayList<MultipleServer>();

    /** Long constant that stores the system time upon Server startup via the
     * {@link System#currentTimeMillis() currentTimeMillis()} method in the
     * {@link System System} class. Used to calculate Server uptime. */
    final static private long SERVER_START_TIME = System.currentTimeMillis();

    /** Empty Constructor to create Server object.
     * Only used to allow instantiation of
     * Multiple Server inner class. */
    public Server(){

    } // end of CONSTRUCTOR

    /** Main method that throws {@link IOException IOException}.
     * Method sets up a {@link ServerSocket ServerSocket} for new
     * connections to the Server and upon new connections instantiates
     * new {@link MultipleServer MultipleServer} threads and starts them. */
    public static void main(String[] args) throws IOException {
        final int PORT_NUM;

        PORT_NUM = Integer.parseInt(args[0]);

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
            System.exit(-1);
        } // end of IOException catch
    } // end of main() method

    /** Method to cycle through online clients and broadcast messages.
     * The method iterates through the {@link #clientList clientList} and
     * does not broadcast the message to the {@link ClientInstance ClientInstance}
     * that was passed as a parameter.
     * @param MSG - Message to be broadcast to other clients
     * @param BCASTER - The client that is sending the message (usually
     * the client calling the method) */
    private static void broadcastMessage(
                String MSG, final MultipleServer BCASTER){
        // --- for loop to iterate through all other users
        // online
        for(int i=0; i < clientList.size(); i++){
            if(clientList.get(i) != BCASTER){
                clientList.get(i).printMessage(MSG, BCASTER);
            } // end of if statement
        } // end of for loop
    } // end of broadcastMessage() method

    private class MultipleServer extends Thread {

        /** {@link Socket Socket} constant that stores the socket value
         * of the {@link ClientInstance ClientInstance} that is connecting
         * to the {@link Server Server}. */
        private final Socket MULTISOCKET;

        /** Long field that is used to store the system time when the
         * {@link ClientInstance ClientInstance} enters the chatroom.
         * System time is calculated via the
         * {@link System#currentTimeMillis() currentTimeMillis} in the
         * {@link System System} class. */
        private long clientChatroomTime;

        /** {@link PrintWriter PrintWriter} that is used to write messages
         * to clients. */
        private PrintWriter serverWriter;

        /** {@link BufferedReader BufferedReader} that is used to read messages
         * from clients. */
        private BufferedReader serverReader;

        /** {@link String String} field that stores the username of the client. */
        private String usrName;

        /** Boolean to be used to allow closing of I/O streams and allow
         * for graceful disconnect from {@link Server Server}. */
        private boolean finished = false;

        public MultipleServer(Socket multiSocket, Server outerServer){
            this.MULTISOCKET = multiSocket;
        } // end of CONSTRUCTOR

        public void run(){
            try{
                // setup I/O streams to be able to send/receive data from client
                serverWriter = new PrintWriter(MULTISOCKET.getOutputStream(), true);
                InputStreamReader serverStreamReader = new InputStreamReader(MULTISOCKET.getInputStream());
                serverReader = new BufferedReader(serverStreamReader);

                System.out.println("New client connected");
                serverWriter.println("----- Connected to Server ---");
                serverWriter.flush();

                setUsrName();

                readFromClient();

            } catch (IOException e){
                e.printStackTrace();
            } // end of IOException catch
        } // end of run() method

        // ----------------
        // MESSAGE HANDLING
        // ----------------

        private void readFromClient(){
            String clientMsg = "";

            // --- while loop to read input from client
            // continually
            while(!finished){
                try{
                    clientMsg = serverReader.readLine();
                    System.out.println("Reading from " + this.usrName + "...");
                } catch (IOException e){
                    System.err.println("I/O Error on Server!");
                    e.printStackTrace();
                } // end of IOException catch

                // handle abrupt disconnect by client
                if(clientMsg != null){
                    parseClientMsg(clientMsg);
                }else{
                    System.out.println("Abrupt disconnect by " + this.usrName);
                    this.logOut();
                    broadcastMessage("has logged off", this);
                } // end of if statement

                serverWriter.println();
            } // end of while loop
        } // end of readFromClient() method

        private void parseClientMsg(final String MSG){
            // ensures only parsing of non-empty strings
            if(MSG.length() != 0){
                // check to see if user input is a specific request
                // i.e starts with ;
                if(MSG.charAt(0) == ';'){
                    parseClientCommand(MSG);
                }else{
                    serverWriter.print("\u2713");
                    broadcastMessage(MSG, this);
                    serverWriter.print("\u2713");
                } // end of if statement
            } // end of if statement

            serverWriter.flush();
        } // end of parseUsrMsg() method

        private synchronized void printMessage(
                final String MSG, final MultipleServer BCASTER){
            serverWriter.println(BCASTER.getUsrName() + ": " + MSG);
            serverWriter.flush();
        } // end of printMessage() method

        private void parseClientCommand(final String CMD){
            switch(CMD){
                case ";cut": // show client uptime
                case ";client_ut":
                    serverWriter.println("Time in chatroom: "
                            + getClientChatroomTime() + " seconds");
                    break;
                case ";e": // user requested exit
                case ";exit":
                    System.out.println(this.usrName + " is logging out");
                    // send message to alert other users
                    broadcastMessage("has logged off", this);
                    this.logOut();
                    break;
                case ";h": // list help commands
                case ";help":
                    getHelpCommands();
                    break;
                case ";ip": // show ip address of server
                case ";ip_addr":
                    serverWriter.println("Server IP Address: " + getServerIP());
                    break;
                case ";ut": // user requested uptime status
                case ";uptime":
                    serverWriter.println("Server Uptime: "
                            + getServerUptime() + " seconds");
                    break;
                case ";un": // user requested number of online users
                case ";usr_num":
                    serverWriter.println("Users online: " + getUsrNum());
                    break;
                default:
                    serverWriter.println("unknown command: type \';h\' for help");
            } // end of switch statement
        } // end of clientCommand() method

        // ----------------
        //  LOGGING OUT
        // ----------------

        private void logOut(){
            // close the I/0 streams
            try{
                serverReader.close();
                finished = true;
            } catch (IOException e){
                e.printStackTrace();
                System.err.println("I/O error on logout");
            } // end of IOException catch

            serverWriter.println("Logging off...");
            serverWriter.close();

            try{
                MULTISOCKET.close();
            } catch (IOException e){
                e.printStackTrace();
            }

            System.out.println(this.usrName +  " logged out");
            // remove username from HashSet
            usrNameSet.remove(this.getUsrName());
        } // end of laogOut() method

        // ----------------
        //     GETTERS
        // ----------------

        private double getClientChatroomTime(){
            return (System.currentTimeMillis() - clientChatroomTime) / 1000;
        } // end of getClientChatroomTime() method

        /** Method to print out the available commands for the client whilst
         * connected to the server.
         * Method uses the {@link PrintWriter#println() println} method from the
         * {@link PrintWriter PrintWriter} class to output the available commands
         * to the client. */
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

        /** Method to get the user name of the client that calls the method.
         * Method returns the instance of the username for that client.
         * @return username of the client */
        private String getUsrName(){
            return usrName;
        } // end of getUsrName() method

        /** Method to get the number of users online.
         * Method returns the size of the HashSet holding each username.
         * @return Size of HashSet storing each username. */
        private int getUsrNum(){
            return usrNameSet.size();
        } // end of getUsrNum() method

        // ----------------
        //     SETTERS
        // ----------------

        /** Method to set a unique username for the client
         * attempting to joing the chatroom.
         * Method takes user input and checks that username
         * is unique against those already stored in the
         * {@link HashSet HashSet}. */
        private void setUsrName(){

            // --- do-while loop to ask for username and check that
            // it is unique against other entries in the HashSet
            do{
                serverWriter.println("Please enter a unique username:- ");
                System.out.println("Requesting username");
                serverWriter.flush();
                try{
                    usrName = serverReader.readLine();
                }catch(IOException e){
                    System.err.println("I/O Error at Username Creation");
                    e.printStackTrace();
                } // end of IOException catch
            }while(Server.usrNameSet.add(usrName) == false && usrName != null);

            broadcastMessage("is online",this);
            System.out.println("User added: " + this.usrName);

            // adds system time for when client entered the chatroom
            clientChatroomTime = System.currentTimeMillis();

            serverWriter.println("--- Entered Chatroom ---\n");
            serverWriter.println("type \';h\' for help\n\n");
        } // end of setUsrName() method
    } // end of MultipleServer Class
} // end of Server Class
