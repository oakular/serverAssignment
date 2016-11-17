// ----- IMPORT STATEMENTS -----
import java.io.*;
import java.net.*;

class ClientInstance extends Thread {
    // ----- FIELDS ----- //
    PrintWriter clientWriter;
    InputStreamReader clientStreamReader;
    BufferedReader clientReader;
    public ClientInstance(){

    } // end of CONSTRUCTOR

    public void run(){
        try{
            Socket socketConnect = new Socket("localhost", 4444);
            System.out.println("Connected to Server");

            // add I/O streams
            clientWriter = new PrintWriter(socketConnect.getOutputStream(), true);
            clientStreamReader = new InputStreamReader(socketConnect.getInputStream());
            clientReader = new BufferedReader(clientStreamReader);

            while(true){
                sendMessage();
            } // end of while loop

        } catch (UnknownHostException e){
            System.err.println("Unknown Host: 4444");
            System.exit(-1);
        } catch (IOException e){ // end of UnknownHostException catch
            e.printStackTrace();
        } // end of IOException catch
    } // end of run() method

    private boolean sendMessage(){
        try{
            String msg = clientReader.readLine();

            if(msg != null)
                System.out.print(msg);
        } catch (IOException e){
            e.printStackTrace();
        } // end of IOException catch

        return true;
    } // end of sendMessage() method
} // end of ClientInstance Class
