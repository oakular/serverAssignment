
// ----- IMPORT STATEMENTS -----
import java.util.Scanner;

public class ClientMain {

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
