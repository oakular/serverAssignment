
// ----- IMPORT STATEMENTS -----
import java.util.Scanner;

/** Class containing a main method to take user input
 * for the IP address and port number of the {@link Server Server}
 * and instantiate a new {@link ClientInstance Client} and then
 * starting the thread. */
public class ClientMain {

    /** Main method that instantiates a {@link ClientInstance Client} and
     * starts its thread. The method also uses the {@link Scanner Scanner}
     * class to take user input for the IP address and port number of the
     * {@link Server Server} and passes this to the
     * {@link ClientInstance ClientInstance} constructor.
     * @param args - command line aguments passed upon program start. */
    public static void main(String[] args){
        Scanner scan = new Scanner(System.in);

        // ---- while loop to get user input for the IP address and
        // port number of the Server
        while(args.length == 0){
            args = new String[2];

            System.out.print("Please enter the IP address of the server: ");
            args[0] = scan.nextLine();
            System.out.print("Enter the port number of the server: ");
            args[1] = scan.nextLine();
        } // end of while loop

        ClientInstance client = new ClientInstance(args[0], args[1]);
        Thread clientThread = new Thread(client);
        clientThread.start();

    } // end of main() method
} // end of ClientMain Class
