import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamConstants;
import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedHashMap;

// Proposer class: implements the "Propose" messaging
// I think of this as a computer/app that council members get access to in order to send Propose messages
public class Proposer {

    private Socket proposerSocket; // Socket
    private ObjectOutputStream output;
    private ObjectInputStream input;
    private Socket tempSocket; // Proposer socket, but for the Acceptor end

    private Integer ID; // Proposer id

    private static Integer proposalIDCounter = 0; // Proposal IDs began at 0
    private Integer proposalID; // Non static for passing to specific Proposal

    private Member proposedPresident; // The "client" of this proposer. Who this proposer makes a proposal for
    private Integer proposedPresidentID;

    private Integer chosenProposalID; // chosen ProposalID after sendPrepare() is called
    private Integer highestReceivedPropID; // Highest received proposalID from Acceptor (if no PROMISE)

    private static Integer numberOfPromises = 0; // Initially, we start with 0 promises

    // Constructor
    public Proposer(Integer proposerID) {
        proposalID = 0;
        this.ID = proposerID;
    }

    // Getters
    public Socket getProposerSocket() {
        return this.proposerSocket;
    }
    public Integer getID() {
        return this.ID;
    }
    public Integer getChosenProposalID() {
        return this.chosenProposalID;
    }

    // Generates a unique and ordered ID for the proposal
    public Integer generateProposalID() {
        proposalIDCounter++;
        proposalID = proposalIDCounter;
        return proposalID;
    }

    // Resets number of promises to 0
    // Call this before sending a new Prepare/Proposal
    public void resetNumberOfPromises() {
        numberOfPromises = 0;
        return;
    }

    // To send prepare messages (a type of email) to given list of acceptors (acceptors)
    // member: the Member which is proposed to be president (not necessarily the one who sent it)
    // Returns: true if Prepare was sent without issues, false if it wasn't sent successfully
    // Note: This is the first time a Proposer makes contact with an Acceptor during an election cycle, so instantiates the sockets
    public boolean sendPrepare(Member proposedPresident, Acceptor a, Member acceptorOperator) {
        ArrayList<Integer> proposalIDs = new ArrayList<Integer>();
        Integer thisProposalID = this.generateProposalID();
        this.proposedPresident = proposedPresident;
        try {
            proposerSocket =  new Socket("proposer" + a.getID(), 456 + a.getID());
            tempSocket = a.getAcceptorSocket().accept(); // Connect the Proposer to the Acceptor
            output = new ObjectOutputStream(proposerSocket.getOutputStream()); // Establishes Proposer streams (NOT Acceptor stream)
            input = new ObjectInputStream(proposerSocket.getInputStream());
            output.writeObject("PREPARE" + " " + thisProposalID); // Sends an email to the Acceptor ("Prepare")
            output.flush();

            // Wait for response from the acceptor
            long responseTime = acceptorOperator.decideMemberResponseTime();
            if (responseTime < 30000) {
                Thread.sleep(responseTime);
            } else { // Else, no response so terminate sending the Prepare
                System.out.println("Error: No response received from the member operating the acceptor.");
                return false;
            }
            // We call Acceptor's receivePrepare here
            if (a.receivePrepare(this, tempSocket, this.proposedPresident)) { // If Acceptor's receivePrepare was successful
                String message = (String) input.readObject();
                String[] keywords = message.split(" ", 3);
                if (keywords[0].equals("PROMISE")) { // If promise was sent by acceptor
                    numberOfPromises++; // Update number of promises by 1
                } else if ((!keywords[0].isEmpty())) { // If no promise was sent by acceptor, but only proposalID was sent
                    proposalIDs.add(Integer.parseInt(keywords[0]));
                    proposedPresidentID = Integer.parseInt(keywords[1]); // Its possible the Acceptor sent back new President, so update
                } else { // Promise wasn't received, and message is empty, so something went wrong
                    return false;
                }
            }
        } catch (IOException | ClassNotFoundException | InterruptedException e) {
            System.out.println("Something went wrong when trying to send Prepare: " + e.getMessage());
            return false;
        }

        if (numberOfPromises == 0) { // If no promises were sent through
            // Loops through all the alternatively-suggested proposalIDs and look for the highest to declare as chosen
            this.highestReceivedPropID = proposalIDs.get(0);
            for (int i = 0; i < proposalIDs.size(); ++i) {
                if (proposalIDs.get(i) > this.highestReceivedPropID) {
                    this.highestReceivedPropID = proposalIDs.get(i);
                }
            }
            chosenProposalID = highestReceivedPropID;
        } else { // Else we see if the majority of acceptors sent PROMISE (5 or more)
            if (numberOfPromises >= 5) {
                chosenProposalID = thisProposalID;
            } else { // If most acceptors didn't agree on the proposal, go through proposalIDs like above and search for highest
                // Loops through all the alternatively-suggested proposalIDs and look for the highest to declare as chosen
                this.highestReceivedPropID = proposalIDs.get(0);
                for (int i = 0; i < proposalIDs.size(); ++i) {
                    if (proposalIDs.get(i) > this.highestReceivedPropID) {
                        this.highestReceivedPropID = proposalIDs.get(i);
                    }
                }
                chosenProposalID = highestReceivedPropID; // Ensures the result of Prepare-Promise process is stored
            }
        }
        return true;
    }

    // Sends the chosen Proposal to given list of acceptors (acceptors)
    // chosenProposalID: may not be the same as highestReceivedPropID
    // member: chosen Member who will be elected president
    // Returns: true if Propose was sent without issues, false if it wasn't sent successfully
    // Assumes: streams and sockets are still connected from sendPrepare
    public boolean sendPropose(Integer chosenProposalID, Member member, Acceptor a) {
        try {
            output.writeObject("PROPOSE" + " " + chosenProposalID + " " + member.getID());
            output.flush();

            // Get acceptor to receive it
            a.receivePropose(this, )


        } catch (IOException e) {
            System.out.println("Failed to send propose: " + e.getMessage());
            return false;
        }
        return true;
    }
}
