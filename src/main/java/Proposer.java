import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class Proposer {

    // Arguments passed from MemberManagement
    private Member memberUsingThis;
    private Integer nominee;
    private Integer currentProposalID;
    private ArrayList<Acceptor> acceptorDirectory = new ArrayList<Acceptor>();
    private ArrayList<Acceptor> participatingAcceptors;
    private ArrayList<Learner> participatingLearners;

    private LinkedHashMap<Integer, AbstractMap.SimpleEntry<Socket, AbstractMap.SimpleEntry<ObjectOutputStream, ObjectInputStream>>> streams = new LinkedHashMap<>();

    private AtomicInteger numPromises = new AtomicInteger(0); // Atomic for concurrent thread access
    private AtomicInteger numAccepted = new AtomicInteger(0); // Atomic for concurrent thread access
    private String response = "";

    public ArrayList<Acceptor> getParticipatingAcceptors() {
        return this.participatingAcceptors;
    }
    public Member getMemberUsingThis() {
        return memberUsingThis;
    }

    /*
        Turns the given 'member' into a proposer, and proposes the given 'nominee'
        ProposalID: A unique, increasing ID for proposals, determined in MemberManagement to identify the Proposal
     */
    public Proposer(Member member, Integer nominee, Integer proposalID, ArrayList<Acceptor> acceptorList) {
        this.memberUsingThis = member;
        this.nominee = nominee;
        this.currentProposalID = proposalID;
        this.acceptorDirectory = acceptorList;
    }

    // Connects the Proposer to the Acceptor on both ends, along with the ObjectOutputStream and ObjectInputStream
    public void connectAcceptor(Acceptor acceptor) {
        try {
            Socket socket = new Socket("localhost", acceptor.getPort()); // Connect socket from Proposer end
            ObjectOutputStream os = new ObjectOutputStream(socket.getOutputStream());

            acceptor.acceptConnection(); // Accept the connection on the Acceptor's end

            acceptor.constructStreams(); // Construct the streams for the Acceptor
            ObjectInputStream is = new ObjectInputStream(socket.getInputStream());

            AbstractMap.SimpleEntry<ObjectOutputStream, ObjectInputStream> temp = new AbstractMap.SimpleEntry<>(os, is);
            AbstractMap.SimpleEntry<Socket, AbstractMap.SimpleEntry<ObjectOutputStream, ObjectInputStream>> temp1 = new AbstractMap.SimpleEntry<>(socket, temp);

            streams.put(acceptor.getMemberUsingThis().getID(), temp1); // Stores the ID and streams in HashMap for reference

            return;
        } catch (Exception e) {
            System.out.println("Error connecting acceptor from Proposer: " + e.getMessage());
        }
    }

    // Connects the Proposer to the Learner on both ends, along with the ObjectOutputStream and ObjectInputStream
    public void connectLearner(Learner learner) {
        try {
            Socket socket = new Socket("localhost", learner.getPort()); // Connect socket from Proposer end
            ObjectOutputStream os = new ObjectOutputStream(socket.getOutputStream());

            learner.acceptConnection(); // Accept the connection on the Learner's end

            learner.constructStreams(); // Construct the streams for the Acceptor
            ObjectInputStream is = new ObjectInputStream(socket.getInputStream());

            AbstractMap.SimpleEntry<ObjectOutputStream, ObjectInputStream> temp = new AbstractMap.SimpleEntry<>(os, is);
            AbstractMap.SimpleEntry<Socket, AbstractMap.SimpleEntry<ObjectOutputStream, ObjectInputStream>> temp1 = new AbstractMap.SimpleEntry<>(socket, temp);

            streams.put(learner.getMemberUsingThis().getID(), temp1); // Stores the ID and streams in HashMap for reference

            return;
        } catch (Exception e) {
            System.out.println("Error connecting learner from Proposer: " + e.getMessage());
        }
    }


    /*
        General sending function
        Retrieves the stream belonging to the given 'acceptor', and sends the given 'message' through the output stream
        acceptor: The acceptor we send to
        message: The message we send to the acceptor
     */
    public void send(Acceptor acceptor, String message) {
        try {
            AbstractMap.SimpleEntry<Socket, AbstractMap.SimpleEntry<ObjectOutputStream, ObjectInputStream>> map = streams.get(acceptor.getMemberUsingThis().getID());

            map.getValue().getKey().writeObject(message);
            map.getValue().getKey().flush();
            return;
        } catch (Exception e) {
            System.out.println("Error sending to Acceptor: " + e.getMessage());
        }
    }

    /*
        Receives and returns the message sent by the given Acceptor
        Assumes: That a message was already sent through and is waiting to be read/received
     */
    public String receive(Acceptor acceptor) {
        String received = "";
        try {
            AbstractMap.SimpleEntry<Socket, AbstractMap.SimpleEntry<ObjectOutputStream, ObjectInputStream>> map = streams.get(acceptor.getMemberUsingThis().getID());

            received = (String) map.getValue().getValue().readObject(); // Reads what was sent to the input stream
            return received;
        } catch (Exception e) {
            System.out.println("Error receiving from Acceptor: " + e.getMessage());
        }
        return received;
    }

    /*
        Notifies the Learner of the voting outcome
        learner: The specific learner we are notifying
        winningProposal: the ID of the winning proposal
        winner: the ID of the member nominated in the winning proposal
     */
    public void inform(Learner learner, Integer winningProposal, Integer winner) {
        try {
            AbstractMap.SimpleEntry<Socket, AbstractMap.SimpleEntry<ObjectOutputStream, ObjectInputStream>> map = streams.get(learner.getMemberUsingThis().getID());

            String message = "Result " + winningProposal + " " + winner; // Constructs the Result message

            map.getValue().getKey().writeObject(message);
            map.getValue().getKey().flush();
        } catch (Exception e) {
            System.out.println("Error informing learner: " + e.getMessage());
        }
    }


    // Checks that the response is a Promise and updates numPromises if it is, as well as the nominee and proposal id
    public void checkPromise(int index) {
        if (response.startsWith("Promise")) {
            numPromises.incrementAndGet(); // Update atomic tracker
            participatingAcceptors.add(acceptorDirectory.get(index)); // Add to the list keeping track of promised acceptors
            String[] keywords = response.split(" ");
            if (currentProposalID < Integer.parseInt(keywords[1].trim())) { // Highest propID < curr propID
                currentProposalID = Integer.parseInt(keywords[1].trim()); // Updates current highest propID
                nominee = Integer.parseInt(keywords[2].trim()); // Updates nominee associated with currentProposalID
            }
        }
    }

    // Sends the "Accept" message, needs a majority to respond Promise first
    // numMembers: Number of members participating
    public boolean sendAccept() {
        if (currentProposalID != 0) {
            for (int i = 1; i < participatingAcceptors.size() + 1; ++i) { // Sends Accept to agreeing Acceptors
                connectAcceptor(participatingAcceptors.get(i - 1)); // Connects this Proposer to the given Acceptor
                this.send(participatingAcceptors.get(i - 1), "Accept " + currentProposalID + " " + nominee);
            }
            return true;
        } else {
            return false;
        }
    }

    // Threaded (non-blocking) function: For receiving the response to Prepare sent by the Proposer
    // index: Index to search for in the list of all acceptors
    // Non-blocked to allow Paxos to proceed to the next Acceptor regardless of delays of the previous Acceptor
    public void getPrepareResponse(int index) {
        Thread prepareResponse = new Thread(() -> {
            acceptorDirectory.get(index).respond(this); // Get the Acceptor to respond
            response = receive(acceptorDirectory.get(index)); // Receive the Acceptor's response on Proposer end

            checkPromise(index); // Verify that this is a Promise
        });
        prepareResponse.start();
    }

    /*
        To cast the vote, and returns whether phase1 was successful
        Note: phase1 comprises of sending Prepare, waiting for Promise, and sending Accept successfully
        numMembers: Number of total members
        acceptorList: List of total acceptors
     */
    public boolean phase1(Integer numMembers, ArrayList<Acceptor> acceptorList) {
        // Resetting key data
        participatingAcceptors = new ArrayList<Acceptor>();
        this.acceptorDirectory = acceptorList;
        response = "";
        numPromises = new AtomicInteger(0);

        for (int i = 1; i < acceptorDirectory.size() + 1; ++i) { // Send Prepare to all Acceptors without
            connectAcceptor(acceptorDirectory.get(i - 1));
            this.send(acceptorDirectory.get(i - 1), "Prepare " + currentProposalID + " " + nominee);

            int index = i - 1;
            getPrepareResponse(index); // Creates a thread in background to wait for each Acceptor's response
        }

        long startTime = System.currentTimeMillis();
        while (true) { // Loop to allow all threads (Acceptor responses) to be received within 10s timeframe
            if ((System.currentTimeMillis() - startTime) > 10000) { // For sake of liveness, we assume no response if delay > 10s
                return false;
            }
            if (numPromises.get() >= ((numMembers / 2) + 1)) { // Checks that majority sent Promise
                return sendAccept();
            }
        }
    }


    // Checks that Accepted is received in the response
    public void checkAccepted(int index, ArrayList<Learner> learnerList) {
        if (response.startsWith("Accepted")) {
            numAccepted.incrementAndGet(); // Update atomic tracker
            participatingAcceptors.add(acceptorDirectory.get(index)); // Add the acceptor and learner to lists
            participatingLearners.add(learnerList.get(index)); // Assumes equal number of Acceptors & Learners, added in same order
            String[] keywords = response.split(" ");
            if (currentProposalID < Integer.parseInt(keywords[1].trim())) { // Highest propID < curr propID
                currentProposalID = Integer.parseInt(keywords[1].trim()); // Update current highest propID
                nominee = Integer.parseInt(keywords[2].trim()); // Update nominee associated with curr highest propID
            }
        }
    }


    // Connects the Proposer to each Learner and informs them
    public boolean sendLearn() {
        if (currentProposalID != 0) {
            for (int i = 1; i < participatingLearners.size() + 1; ++i) { // Notify all the Learners
                connectLearner(participatingLearners.get(i - 1)); // Connect Proposer to the Learner
                this.inform(participatingLearners.get(i - 1), this.currentProposalID, this.nominee); // Sends to Learner
                participatingLearners.get(i - 1).receive(); // Receive the message on the Learner's end
            }
            return true;
        } else {
            return false;
        }
    }


    // Non-blocked threaded function to check for Accepted response from Acceptor
    public void checkAcceptResponse(int index, ArrayList<Learner> learnearList) {
        Thread checkResponse = new Thread(() -> {
            acceptorDirectory.get(index).respond(this); // Gets the right Acceptor to respond
            response = this.receive(acceptorDirectory.get(index)); // Receives the response on Proposer end

            checkAccepted(index, learnearList); // Check the response message for "Accepted"
        });
        checkResponse.start();
    }

    /*
        Collect "Accepted messages", checks for majority consensus and then broadcasts to Learners
        Assumes: Accept has been sent to all agreeing Acceptors, equal number of Acceptors & Learners, added in same order
     */
    public boolean phase2(Integer newNumMembers, ArrayList<Learner> learnerList) {
        // Resets key data
        participatingAcceptors = new ArrayList<Acceptor>();
        participatingLearners = new ArrayList<Learner>();
        String response = "";
        numAccepted = new AtomicInteger(0);

        for (int i = 1; i < acceptorDirectory.size() + 1; ++i) { // Collects the "Accepted" from Acceptor simultaneously
            checkAcceptResponse(i - 1, learnerList); // Thread call (runs in background) to avoid blocking
        }

        long startTime = System.currentTimeMillis();
        while (true) { // Loop to allow all threads (Acceptor responses) to be received within 10s timeframe
            if ((System.currentTimeMillis() - startTime) > 10000) { // If 10s and not enough responses, assume Member's never responded
                return false;
            }

            if (numAccepted.get() >= ((newNumMembers / 2) + 1)) { // If majority send "Accepted", notify all Learners
                sendLearn();
                return true;
            }
        }
    }
}