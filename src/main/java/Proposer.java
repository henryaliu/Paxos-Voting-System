import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.concurrent.ConcurrentHashMap;

public class Proposer {

    public Integer receivedPromises;

    // Arguments passed from MemberManagement
    private Member memberUsingThis;
    private Integer nominee;
    private Integer currentProposalID;
    private ArrayList<Acceptor> acceptorDirectory = new ArrayList<Acceptor>();
    private ArrayList<Acceptor> participatingAcceptors;

    private LinkedHashMap<Integer, AbstractMap.SimpleEntry<Socket, AbstractMap.SimpleEntry<ObjectOutputStream, ObjectInputStream>>> streams = new LinkedHashMap<>();

    public ArrayList<Acceptor> getParticipatingAcceptors() {
        return this.participatingAcceptors;
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

    public Member getMemberUsingThis() {
        return memberUsingThis;
    }

    public void connectAcceptor(Acceptor acceptor) {
        try {
            Socket socket = new Socket("localhost", acceptor.getPort());
            ObjectOutputStream os = new ObjectOutputStream(socket.getOutputStream());

            acceptor.acceptConnection();

            acceptor.constructStreams();
            ObjectInputStream is = new ObjectInputStream(socket.getInputStream());

            AbstractMap.SimpleEntry<ObjectOutputStream, ObjectInputStream> temp = new AbstractMap.SimpleEntry<>(os, is);
            AbstractMap.SimpleEntry<Socket, AbstractMap.SimpleEntry<ObjectOutputStream, ObjectInputStream>> temp1 = new AbstractMap.SimpleEntry<>(socket, temp);

            streams.put(acceptor.getMemberUsingThis().getID(), temp1);

            return;
        } catch (Exception e) {
            System.out.println("Error connecting acceptor from Proposer: " + e.getMessage());
        }
    }

    public void connectLearner(Learner learner) {
        try {
            Socket socket = new Socket("localhost", learner.getPort());
            ObjectOutputStream os = new ObjectOutputStream(socket.getOutputStream());

            learner.acceptConnection();

            learner.constructStreams();
            ObjectInputStream is = new ObjectInputStream(socket.getInputStream());

            AbstractMap.SimpleEntry<ObjectOutputStream, ObjectInputStream> temp = new AbstractMap.SimpleEntry<>(os, is);
            AbstractMap.SimpleEntry<Socket, AbstractMap.SimpleEntry<ObjectOutputStream, ObjectInputStream>> temp1 = new AbstractMap.SimpleEntry<>(socket, temp);

            streams.put(learner.getMemberUsingThis().getID(), temp1);

            return;
        } catch (Exception e) {
            System.out.println("Error connecting learner from Proposer: " + e.getMessage());
        }
    }

    /*
        General sending function
        Creates and connect socket to member socket, aligns streams to this socket
        acceptor: The Acceptor it will send the message to
        message: The message
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
        Receives the message sent by Acceptor
     */
    public String receive(Acceptor acceptor) {
        String received = "";
        try {
            AbstractMap.SimpleEntry<Socket, AbstractMap.SimpleEntry<ObjectOutputStream, ObjectInputStream>> map = streams.get(acceptor.getMemberUsingThis().getID());

            received = (String) map.getValue().getValue().readObject();
            return received;
        } catch (Exception e) {
            System.out.println("Error receiving from Acceptor: " + e.getMessage());
        }
        return received;
    }

    /*
        Notifies the Learner of the voting outcome
     */
    public void inform(Learner learner, Integer winningProposal, Integer winner) {
        try {
            AbstractMap.SimpleEntry<Socket, AbstractMap.SimpleEntry<ObjectOutputStream, ObjectInputStream>> map = streams.get(learner.getMemberUsingThis().getID());

            String message = "Result " + winningProposal + " " + winner;

            map.getValue().getKey().writeObject(message);
            map.getValue().getKey().flush();
        } catch (Exception e) {
            System.out.println("Error informing learner: " + e.getMessage());
        }
    }

    // Checks if 'message' is a Promise
    public boolean isAPromise(String message) {
        if (message != null && !message.isEmpty()) {
            return message.startsWith("Promise");
        }
        return false;
    }

    /*
        To cast the vote, and returns whether phase1 was successful
        Note: phase1 comprises of sending Prepare, waiting for Promise, and sending Accept successfully
     */
    public boolean phase1(Integer numMembers, ArrayList<Acceptor> acceptorList) {
        participatingAcceptors = new ArrayList<Acceptor>();
        this.acceptorDirectory = acceptorList;

        Integer chosenValue;
        Integer numPromises = 0;
        String response;
        for (int i = 1; i < acceptorDirectory.size() + 1; ++i) { // Sends to all Acceptors simultaneously
            connectAcceptor(acceptorDirectory.get(i - 1));
            this.send(acceptorDirectory.get(i - 1), "Prepare " + currentProposalID + " " + nominee);



            acceptorDirectory.get(i - 1).respond(this); // Get the Acceptors to respond



            response = receive(acceptorDirectory.get(i - 1)); // Receive the Acceptor's response on Proposer end
            if (isAPromise(response)) {
                numPromises++;
                participatingAcceptors.add(acceptorDirectory.get(i - 1));
                String[] keywords = response.split(" ");
                if (currentProposalID < Integer.parseInt(keywords[1].trim())) { // Highest propID < curr propID
                    currentProposalID = Integer.parseInt(keywords[1].trim()); // Update current highest propID
                    nominee = Integer.parseInt(keywords[2].trim()); // Update nominee associated with curr highest propID
                }
            }
        }
        if (numPromises >= ((numMembers / 2) + 1)) {
            if (currentProposalID != 0) {
                for (int i = 1; i < participatingAcceptors.size() + 1; ++i) { // Sends Accept to agreeing Acceptors
                    connectAcceptor(participatingAcceptors.get(i - 1));
                    this.send(participatingAcceptors.get(i - 1), "Accept " + currentProposalID + " " + nominee);
                }
                return true;
            }
        } else {
            return false;
        }
        return false;
    }

    /*
        Collect "Accepted messages", checks for majority consensus and then broadcasts to Learners
        Assumes: Accept has been sent to all agreeing Acceptors, equal number of Acceptors & Learners, added in same order
     */
    public boolean phase2(Integer newNumMembers, ArrayList<Learner> learnerList) {
        participatingAcceptors = new ArrayList<Acceptor>();
        ArrayList<Learner> participatingLearners = new ArrayList<Learner>();
        String response = "";
        Integer numAccepted = 0;
        for (int i = 1; i < acceptorDirectory.size() + 1; ++i) { // Collect the "Accepted" from Acceptor simultaneously
            acceptorDirectory.get(i - 1).respond(this);
            response = this.receive(acceptorDirectory.get(i - 1));

            if (response.startsWith("Accepted")) {
                numAccepted++;
                participatingAcceptors.add(acceptorDirectory.get(i - 1));
                participatingLearners.add(learnerList.get(i - 1)); // Assumes equal number of Acceptors & Learners, added in same order
                String[] keywords = response.split(" ");
                if (currentProposalID < Integer.parseInt(keywords[1].trim())) { // Highest propID < curr propID
                    currentProposalID = Integer.parseInt(keywords[1].trim()); // Update current highest propID
                    nominee = Integer.parseInt(keywords[2].trim()); // Update nominee associated with curr highest propID
                }
            }
        }

        if (numAccepted >= ((newNumMembers / 2) + 1)) { // If majority send "Accepted", notify all Learners
            if (currentProposalID != 0) {
                for (int i = 1; i < participatingLearners.size() + 1; ++i) { // Notify all the Learners
                    connectLearner(participatingLearners.get(i - 1));
                    this.inform(participatingLearners.get(i - 1), this.currentProposalID, this.nominee); // Sends to Learner
                    participatingLearners.get(i - 1).receive();
                }
                return true;
            }
        } else {
            return false;
        }

        return false;
    }
}