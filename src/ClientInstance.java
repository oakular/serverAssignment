// ----- IMPORT STATEMENTS -----
import java.io.*;
import java.net.*;

class ClientInstance implements Runnable {

    // ----- FIELDS ----- //
    /** {@link PrintWriter PrintWriter} to print output from the
     * {@link Server Server}. */
    private PrintWriter clientWriter;

    /** {@link InputStreamReader InputStreamReader} to read input from user. */
    private InputStreamReader clientStreamReader;

    /** {@link BufferedReader BufferedReader} to read input from
     * standard input. */
    private BufferedReader clientInputReader;

    /** Int field to store the port number of the {@link Server Server}. */
    private int portNum;

    /** {@link InetAddress InetAddress} field to store the IP address of
     * the {@link Server Server}. */
    private InetAddress ipAddr;

    /** Boolean field that is set to true when {@link Server Server} or Client
     * disconnects. */
    private boolean stop = false;

    /** Constructor to call within ClientMain class
     * and allows for thread to be started upon object
     * of this class. */
    public ClientInstance(String ipAddr, String portNum){
        this.portNum = Integer.parseInt(portNum);
        try{
            this.ipAddr = InetAddress.getByName(ipAddr);
        } catch (UnknownHostException e){
            System.err.println("Unknown Host");
            System.exit(-1);
        } // end of UnknownHostException catch
    } // end of CONSTRUCTOR

    /** Method override that connects to {@link Server Server} via a
     * {@link Socket Socket}. Method then starts a
     * {@link ServerListener ServerListener} thread and calls the
     * {@link #sendMessage sendMessage} method. */
    public void run(){
        try{
            Socket socketConnect = new Socket(ipAddr, portNum);

            // add I/O streams
            clientWriter = new PrintWriter(socketConnect.getOutputStream(), true);
            clientStreamReader = new InputStreamReader(socketConnect.getInputStream()); // used in ServerListener Class
            clientInputReader = new BufferedReader(new InputStreamReader(System.in));

            // start the ServerListener to listen for messages
            // from the Server
            ServerListener sListener = new ServerListener();
            sListener.start();

            sendMessage();

        } catch (UnknownHostException e){
            System.err.println("Unknown Host: " + portNum);
            System.exit(-1);
        } catch (IOException e){ // end of UnknownHostException catch
            System.err.println("Connection refused");
            e.printStackTrace();
        } // end of IOException catch
    } // end of run() method

    /** Method to send message to the server.
     * Method reads from Standard Input and writes to the {@link Socket Socket}
     * where the Client is connected to the {@link Server Server}.
     * Methd then flushes the buffers of {@link #clientWriter clientWriter}
     * to ensure message is sent to the {@link Server Server}. */
    private void sendMessage(){
        String msg;

        try{
            // --- while loop to ask for user input and send message to server
            while((msg = clientInputReader.readLine()) != null && stop == false){
                clientWriter.println(msg);
                clientWriter.flush();
            }
        } catch (IOException e){
            System.err.println("I/O Error whilst sending message");
            e.printStackTrace();
        } // end of IOException catch

        System.exit(0);
    } // end of sendMessage() method

    /** Class that extends the {@link Thread Thread} class and uses
     * a {@link BufferedReader BufferedReader} to listen for messages
     * from the {@link Server Server}. */
    class ServerListener extends Thread {

        // ----- FIELDS ----- //
        /** {@link BufferedReader BufferedReader} to read input from Server. */
        private BufferedReader clientReader;

        /** Empty Constructor to create ServerListener object. */
        public ServerListener(){

        } // end of CONSTRUCTOR

        /** Method override that uses the {@link #clientReader clientReader}
         * to continually listen for output from the {@link Server Server}
         * and display it on standard output. */
        public void run(){
            String serverMsg;

            try{
                clientReader = new BufferedReader(clientStreamReader);

                // --- while loop to read output from the server
                // and display on standard output
                while(!stop){
                    serverMsg = clientReader.readLine();

                    if(serverMsg != null){
                        System.out.println(serverMsg);
                    }else if(serverMsg == null){
                        System.err.println("System exiting...");
                        stop = true;
                    } // end of if statement

                } // end of while loop

                System.exit(0);
            } catch (IOException e){
                e.printStackTrace();
                System.err.println("Error when listening to server");
            } // end of IOException lncatch
        } // end of run() method

    } // end of ServerListener Class

} // end of ClientInstance Class
