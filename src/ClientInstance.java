// ----- IMPORT STATEMENTS -----
import java.io.*;
import java.net.*;

class ClientInstance extends Thread {

    public ClientInstance(){

    } // end of CONSTRUCTOR

    public void run(){
        try{
            Socket socketConnect = new Socket(4444);
            System.out.println("Connected to Server");

            // add I/O streams
            PrintWriter clientWriter = new PrintWriter(socketConnect.getOutputStream(), true);
            InputStreamReader clientStreamReader = new InputStreamReader(socketConnect.getInputStream());
            BufferedReader clientReader = new BufferedReader(clientStreamReader);

        } catch (UnknownHostException e){
            System.err.println("Unknown Host: 4444");
            System.exit(-1);
        } // end of UnknownHostException catch
    } // end of run() method
} // end of ClientInstance Class
