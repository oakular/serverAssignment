// ----- IMPORT STATEMENTS -----
import java.io.*;
import java.net.*;

class ClientInstance implements Runnable {

    // ----- FIELDS ----- //
    /** PrintWriter to print output from the Server */
    private PrintWriter clientWriter;
    /** StreamReader to read input from user */
    private InputStreamReader clientStreamReader;
    /** BufferedReader to read input from standard input */
    private BufferedReader clientInputReader;

    /** Constructor to call in ClientMain class
     * and allows for thread to be started upon object
     * of this class. */
    public ClientInstance(){

    } // end of CONSTRUCTOR

    /** Method override that connects to Server via a socket and sets up the
     * I/O streams. Method then waits for commands to be passed to carry out
     * other functions. */
    public void run(){
        try{
            Socket socketConnect = new Socket("localhost", 4444);

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
            System.err.println("Unknown Host: 4444");
            System.exit(-1);
        } catch (IOException e){ // end of UnknownHostException catch
            e.printStackTrace();
        } // end of IOException catch
    } // end of run() method

    /** Method to send message to the server.
     * Method reads from Standard Input and writes to the socket
     * where client is connected to server. Flushes buffers to ensure
     * message is written to output stream. */
    private void sendMessage(){
        String msg;

        try{
            System.out.print(">");

            // --- while loop to ask for user input and send message to server
            while((msg = clientInputReader.readLine()) != null){
                clientWriter.println(msg);
                clientWriter.flush();
                System.out.print(">");
            }
        } catch (IOException e){
            System.err.println("I/O Error!");
            e.printStackTrace();
        } // end of IOException catch
    } // end of sendMessage() method

    class ServerListener extends Thread {

        /** BufferedReader to read input from Server */
        private BufferedReader clientReader;

        public ServerListener(){

        } // end of CONSTRUCTOR

        /** Method override that continually listens for output from
         * the server and displays it on standard output. */
        public void run(){
            String serverMsg;

            try{
                clientReader = new BufferedReader(clientStreamReader);

                // --- while loop to read output from the server
                // and display on standard output
                while(true){
                    serverMsg = clientReader.readLine();
                    System.out.println("Line read from Server");

                    if(serverMsg != null){
                        System.out.println(serverMsg);
                    } // end of if statement
                } // end of while loop
            } catch (IOException e){
                e.printStackTrace();
                System.err.println("Error when listening to server");
            } // end of IOException catch
        } // end of run() method
    } // end of ServerListener Class
} // end of ClientInstance Class
