import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

// Proposer class: implements the "Acceptor" messaging
public class Acceptor {

    private ServerSocket acceptorSocket; // Server socket where the proposers send Proposals to
    private Integer ID;

    private Member currentMemberUsingThis;
    private Integer currentProposalID = null; // For storing the ID of the current Proposal that has been accepted

    // Constructor
    // Instantiates the server socket
    // acceptorID: identification for the acceptor (e.g. 1,2,3,4,5)
    public Acceptor(Integer acceptorID) {
        try {
            acceptorSocket = new ServerSocket(456 + acceptorID); // Setup the port: port = acceptor+ID (e.g. 4569)
            this.ID = acceptorID;
            this.currentProposalID = null;
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

    // Function to read in a Prepare re message from a specified Proposer, and send PROMISE if conditions are met
    // Else, it will send its own current Proposal ID
    // Condition to return PROMISE: If currentProposalID < latest sent Proposal ID
    public boolean receivePrepare(Proposer p, Socket connectedSocket, Member memberToVoteFor) {
        try {
            ObjectOutputStream output = new ObjectOutputStream(connectedSocket.getOutputStream());
            ObjectInputStream input = new ObjectInputStream(connectedSocket.getInputStream());
            String message = (String) input.readObject();
            String[] keywords = message.split(" ", 2); // Split the message into two keywords
            if (keywords[0].equals("PREPARE")) {
                if (this.currentProposalID < Integer.parseInt(keywords[1])) { // If Acceptor has not already promised to a Proposal with higher number
                    this.currentProposalID = Integer.parseInt(keywords[1]); // Commit to the new proposal ID
                    output.writeObject("PROMISE" + " " + this.currentProposalID + " " + memberToVoteFor.getID());
                    output.flush();
                } else { // Else, respond without "PROMISE", instead sending only the currentProposalID
                    output.writeObject(this.currentProposalID);
                    output.flush();
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            System.out.println(e.getMessage());
            return false;
        }
        return true;
    }

}
