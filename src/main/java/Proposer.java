import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamConstants;
import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedHashMap;

// Proposer class: implements the "Propose" messaging
// I think of this as a computer/device that council members get access to in order to send Propose messages
public class Proposer {

    private Socket proposerSocket; // Socket

    private Integer ID; // Proposer id

    private ArrayList<Acceptor>

    // Constructor
    public Proposer(Integer proposerID) {
        this.ID = proposerID;
        try {
            proposerSocket = new Socket("proposer" + String.valueOf(ID), 456 + ID); // Redirects socket to the member it needs to send to
        } catch (IOException ie) {
            System.out.println(ie.getMessage());
        }
    }

    // Getter
    public Socket getProposerSocket() {
        return this.proposerSocket;
    }
    public Integer getID() {
        return this.ID;
    }


    // Function that does all the dirty work of sending to acceptors
    public void configureRecipient() {

    }

    // To send prepare messages (a type of email) to given list of acceptors (acceptors)
    // member: Member from which the Prepare message was sent from, gets responsiveness and reliability from this
    // Returns: true if Prepare was sent without issues, false if it wasn't sent successfully
    // Note: This is the first time a Proposer makes contact with an Acceptor during an election cycle, so instantiates the sockets
    public boolean sendPrepare(Member member, ArrayList<Acceptor> acceptors) {

    }

    // Sends the "Propose" message (a type of email) to given list of acceptors (acceptors)
    // member: Member who sent the proposal, gets responsiveness and reliability from this
    // Returns: true if Propose was sent without issues, false if it wasn't sent successfully
    public boolean sendPropose(Member member, ArrayList<Acceptor> acceptors) {

    }
}
