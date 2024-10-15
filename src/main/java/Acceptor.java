import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;

// Proposer class: implements the "Acceptor" messaging
public class Acceptor {

    private ServerSocket acceptorSocket; // Server socket where the proposers send Proposals to
    private Integer ID;

    // Constructor
    // Instantiates the server socket
    // acceptorID: identification for the acceptor (e.g. 1,2,3,4,5)
    public Acceptor(Integer acceptorID) {
        try {
            acceptorSocket = new ServerSocket(456 + acceptorID); // port = acceptor+ID (e.g. 4569)
            this.ID = acceptorID;
        } catch (IOException ie) {
            System.out.println(ie.getMessage());
        }
    }

    // Getters
    public ServerSocket getAcceptorSocket() {
        return this.acceptorSocket;
    }
    public Integer getID() {
        return this.ID;
    }

}
