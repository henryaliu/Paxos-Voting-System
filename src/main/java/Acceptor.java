import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.AbstractMap;
import java.util.LinkedHashMap;
import java.util.concurrent.CompletableFuture;

// Proposer class: implements the "Acceptor" messaging
// Designed to suit 1 member per Acceptor
public class Acceptor {

    private String response = "";

    private ServerSocket acceptorSocket; // Server socket where the proposers send Proposals to

    // Assumes only 1 member per acceptor
    private Socket referenceSocket;
    private ObjectOutputStream outputStream;
    private ObjectInputStream inputStream;

    private Integer port;

    private Integer currentProposalID = null; // For storing the ID of the current Proposal that has been accepted
    private Member memberUsingThis;

    /*
        acceptorID: To identify the acceptor
        member: The member using this Acceptor
     */
    public Acceptor(Integer acceptorID, Member member) {
        this.port = 5000 + acceptorID; // Port = 5000 + ID
        this.memberUsingThis = member;
        try {
            acceptorSocket = new ServerSocket(this.port);
        } catch (Exception e) {
            System.out.println("ERROR: " + e.getMessage());
        }
    }

    // Accepts incoming connections from sockets (Proposers)
    public void acceptConnection() {
        try {
            referenceSocket = acceptorSocket.accept();
            return;
        } catch (Exception e) {
            System.out.println("ERROR: " + e.getMessage());
        }
    }

    // Construct streams based off the established connection
    // Assumes: a socket is connected to this Server Socket already
    public void constructStreams() {
        try {
            outputStream = new ObjectOutputStream(referenceSocket.getOutputStream());
            inputStream = new ObjectInputStream(referenceSocket.getInputStream());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }


    /*
        Checks for message and responds
        Assumes that a message has been sent to this Acceptor
     */
    public void respond(Proposer proposer) {
        try {
            String received = (String) inputStream.readObject();
            if (!received.isEmpty()) {
                String[] keywords = received.split(" ");
                int receivedProposalID = Integer.parseInt(keywords[1].trim());
                if (currentProposalID == null || receivedProposalID >= currentProposalID) { // If no previous proposal
                    currentProposalID = receivedProposalID;
                } else { // (received < current) == reject
                    response = "Reject " + currentProposalID + " " + keywords[2].trim();
                }
                if (received.startsWith("Prepare")) { // If we received "Prepare", respond with "Promise"
                    response = "Promise " + currentProposalID + " " + keywords[2].trim();
                } else if (received.startsWith("Accept")) { // If we received "Accept", respond with "Accepted"
                    response = "Accepted " + currentProposalID + " " + keywords[2].trim();
                } else {
                    return;
                }

                if (getMemberUsingThis().respondOrNot()) { // Determines if the node responds at all
                    long delay = getMemberUsingThis().decideResponseTime(); // If responded, determine the delay
                    Thread.sleep(delay); // Simulate this delay for the Member acting as this Acceptor
                } else {
                    response = "Reject 0 0"; // No response = send back Reject
                }

                outputStream.writeObject(response);
                outputStream.flush();
            }
            return;
        } catch (Exception e) {
            System.out.println("Error connecting to Acceptor: " + e.getMessage());
        }
    }

    public ServerSocket getAcceptorSocket() {
        return acceptorSocket;
    }
    public Integer getPort() {
        return this.port;
    }
    public Member getMemberUsingThis() {
        return this.memberUsingThis;
    }
}




