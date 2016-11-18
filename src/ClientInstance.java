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

            String msg = "";

            while((msg = clientReader.readLine()) != null){
                //sendMessage(msg);
                System.out.println(msg);
            } // end of while loop

        } catch (UnknownHostException e){
            System.err.println("Unknown Host: 4444");
            System.exit(-1);
        } catch (IOException e){ // end of UnknownHostException catch
            e.printStackTrace();
        } // end of IOException catch
    } // end of run() method

    private boolean sendMessage(String msg){
        clientWriter.println(msg);
        System.out.println(msg);
        return true;
    } // end of sendMessage() method
} // end of ClientInstance Class
