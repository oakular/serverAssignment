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
     * new {@link MultipleServer MultipleServer} threads and starts them.
     * @param args - command line arguments passed to the Server on
     * program start. */
    public static void main(String[] args) {
        final int PORT_NUM;

        PORT_NUM = Integer.parseInt(args[0]);

        try(ServerSocket serverSocket = new ServerSocket(PORT_NUM)){
            // --- while loop to listen to connection requests and
            // start them as new threads
            while(true){
                Socket clientSocket = serverSocket.accept();
                Server server = new Server(); // needed to create instance of inner class MultipleServer
                Server.MultipleServer multiServer = server.new MultipleServer(clientSocket);
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

    /** Class that extends {@link Thread Thread} class and contains
     * methods to read from ,{@link ClientInstance Clients} handle message
     * parsing and set usernames for {@link ClientInstance Clients}. */
    private class MultipleServer extends Thread {

        /** {@link Socket Socket} constant that stores the socket value
         * of the {@link ClientInstance Client} that is connecting
         * to the {@link Server Server}. */
        private final Socket MULTISOCKET;

        /** Long field that is used to store the system time when the
         * {@link ClientInstance Client} enters the chatroom.
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

        /** Constructor to instantiate a MultipleServer object to handle
         * message parsing and client entering and exiting the chatroom.
         * @param multiSocket - the socket that the Client is connecting
         * through. */
        public MultipleServer(Socket multiSocket){
            this.MULTISOCKET = multiSocket;
        } // end of CONSTRUCTOR

        /** Method override of the {@link Thread#run() run} method in the
         * {@link Thread Thread} class. Method sets up the I/O streams via
         * the {@link PrintWriter PrintWriter} and
         * {@link BufferedReader BufferedReader} classes and then calls
         * the {@link #setUsrName() setUsrName} and
         * {@link #readFromClient() readFromClient} methods. */
         public void run(){
            try{
                // setup I/O streams to be able to send/receive data from client
                serverWriter = new PrintWriter(MULTISOCKET.getOutputStream(), true);
                InputStreamReader serverStreamReader = new InputStreamReader(MULTISOCKET.getInputStream());
                serverReader = new BufferedReader(serverStreamReader);

                System.out.println("New client connected");
                serverWriter.println("----- Connected to Server ---");
                serverWriter.flush();

                // set unique username
                setUsrName();

                // continually read from client
                readFromClient();

            } catch (IOException e){
                e.printStackTrace();
            } // end of IOException catch
        } // end of run() method

        // ----------------
        // MESSAGE HANDLING
        // ----------------

        /** Method to read from clients output stream and then pass
         * the received message onto the message parsing methods.
         * Method reads from {@link ClientInstance Client} via the
         * {@link #serverReader serverReader} and only passes not-null
         * messages onto the {@link #parseClientMsg(String) parseClientMsg}
         * method. If the message is null then the {@link #logOut() logOut}
         * method is called */
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

        /** Method to take message as a parameter and detect if it is
         * a broadcast message or a server command. Method looks at the
         * first character of the message and if it is a command message calls
         * the {@link #parseClientCommand parseClientCommand} method.
         * Otherwise it calls the {@link #broadcastMessage(String,
         * MultipleServer) broadcastMessage} method.
         * @param MSG - The message to be parsed. */
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

        /** Method to print message to output stream of
         * {@link ClientInstance Clients}.
         * @param MSG - The message to be printed.
         * @param BCASTER - The Client sending the message. */
        private synchronized void printMessage(
                final String MSG, final MultipleServer BCASTER){
            serverWriter.println(BCASTER.getUsrName() + ": " + MSG);
            serverWriter.flush();
        } // end of printMessage() method

        /** Method to parse the command message passed as a parameter
         * and call methods to respond to the {@link ClientInstance Client's}
         * request. Method uses a switch statement to see which command the
         * command message parameter matches with before calling the method
         * nested within the matching case.
         * @param CMD - The command message to be parsed. */
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
        } // end of parseClientCommand() method

        // ----------------
        //  LOGGING OUT
        // ----------------

        /** Method to close I/O streams and stop continual listening
         * for input from {@link ClientInstance Client}. Method uses the
         * {@link PrintWriter#close() close} method in the
         * {@link PrintWriter PrintWriter}
         * and {@link BufferedReader BufferedReader} classes to close the I/O
         * streams before closing the {@link #MULTISOCKET MULTISOCKET} and
         * removing the {@link ClientInstance Client's} username from the
         * {@link Server#usrNameSet usrNameSet} in the {@link Server Server}
         * class. */
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

        /** Method to calculate the length of time the
         * {@link ClientInstance Client} has been connected to the
         * chatroom for. Method takes the current system time via the
         * {@link System#currentTimeMillis() currentTimeMillis} method
         * and substracts the {@link #clientChatroomTime clientChatroomTime}.
         * It then divides the resulting number by 1000 to format the time into
         * seconds for readability.
         * @return The length of time the Client has spent in the chatroom */
        private double getClientChatroomTime(){
            return (System.currentTimeMillis() - clientChatroomTime) / 1000;
        } // end of getClientChatroomTime() method

        /** Method to print out the available commands for the client whilst
         * connected to the server.
         * Method uses the {@link PrintWriter#println(String) println} method
         * from the {@link PrintWriter PrintWriter} class to output the
         * available commands to the client. */
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

        /** Method to get the user name of the {@link ClientInstance Client}
         * that calls the method. Method returns the instance of the
         * username for that client.
         * @return username of the client */
        private String getUsrName(){
            return usrName;
        } // end of getUsrName() method

        /** Method to get the number of users online.
         * Method returns the size of the {@link #usrNameSet usrNameSet}
         * @return Size of HashSet storing each username. */
        private int getUsrNum(){
            return usrNameSet.size();
        } // end of getUsrNum() method

        // ----------------
        //     SETTERS
        // ----------------

        /** Method to set a unique username for the client
         * attempting to joing the chatroom.
         * Method takes input through the {@link #serverReader serverReader}
         * and checks that username is unique against those already
         * stored in the {@link Server#usrNameSet usrNameSet}. It then
        * {@link #broadcastMessage(String, MultipleServer) broadcasts a message}
         * to alert all users that a new user is online and stores the system time
         * in {@link #clientChatroomTime clientChatroomTime} */
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
