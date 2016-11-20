// ----- IMPORT STATEMENTS -----
import java.io.*;
import java.net.*;

class ClientInstance extends Thread {

    // ----- FIELDS ----- //
    /** PrintWriter to print output from the Server */
    PrintWriter clientWriter;
    /** StreamReader to read input from user */
    InputStreamReader clientStreamReader;
    /** BufferedReader to read input from user */
    BufferedReader clientReader;

    BufferedReader clientInputReader;

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
            System.out.println("Connected to Server");

            // add I/O streams
            clientWriter = new PrintWriter(socketConnect.getOutputStream(), true);
            clientStreamReader = new InputStreamReader(socketConnect.getInputStream());
            clientReader = new BufferedReader(clientStreamReader);
            clientInputReader = new BufferedReader(new InputStreamReader(System.in));

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
            // --- while loop to ask for user input and send message to server
            while((msg = clientInputReader.readLine()) != null){
                clientWriter.println(msg);
                clientWriter.flush();
                System.out.println(msg);
            }
        } catch (IOException e){
            System.err.println("I/O Error!");
            e.printStackTrace();
        } // end of IOException catch
    } // end of sendMessage() method
} // end of ClientInstance Class
