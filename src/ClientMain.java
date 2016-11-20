public class ClientMain {

    public static void main(String[] args){

        ClientInstance client = new ClientInstance();
        Thread clientThread = new Thread(client);
        clientThread.start();

    } // end of main() method
} // end of ClientMain Class
