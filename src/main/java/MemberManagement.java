import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamConstants;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

// Organises the members into a group that can be easily accessed
// Where the program execution occurs (main function)
public class MemberManagement {
    private ArrayList<Member> memberList = new ArrayList<Member>(); // List of all members
    private ArrayList<String> IDList = new ArrayList<String>(); // Stores list of all member IDs for easy access
    private final double twoProposalChance = 0.3; // Probability of two members sending proposal

    private final NavigableMap<Double, String>  memberWeightings = new TreeMap<Double, String>(); // Stores member and their dominance

    // Ordered maps containing the messengers and the socket for convenient communication
    private LinkedHashMap<Proposer, Socket> proposers = new LinkedHashMap<Proposer, Socket>();
    private LinkedHashMap<Acceptor, ServerSocket> acceptors = new LinkedHashMap<Acceptor, ServerSocket>();
    private LinkedHashMap<Learner, Socket> learners = new LinkedHashMap<Learner, Socket>();

    // Ordered map of output+input streams for each Acceptor connected to a Proposer
    // Each key (Socket) is a proposer socket for an acceptor
    private LinkedHashMap<Socket, ArrayList<ObjectStreamConstants>> propToAcceptorStreams = new LinkedHashMap();

    // Default constructor: creates all the members with their specific settings
    public MemberManagement() {
        memberList = new ArrayList<Member>();
        memberList.add(new Member("1", 2, 1.0, 1.0, "none")); // M1
        memberList.add(new Member("2", 1, 0.1, 0.1, "M2")); // M2
        memberList.add(new Member("3", 2, 0.5, 0.2, "M3")); // M3
        for (int i = 4; i < 10; ++i) { // Creates members M4 to M9 and adds them to list of members
            memberList.add(new Member(String.valueOf(i), 3, 0.5, 0.5, "none"));
        }
        double total = 0;
        for (int i = 0; i < memberList.size(); ++i) {
            IDList.add(memberList.get(i).getID()); // Adds all the ids in IDList
            if (memberList.get(i).getDominance().equals(1)) {
                total += 28.0; // 28% chance to be the one sending a proposal (Most dominant)
            } else if (memberList.get(i).getDominance().equals(2)) {
                total += 15.0; // 15% chance to be the one sending a proposal
            } else if (memberList.get(i).getDominance().equals(3)) {
                total += 7.0; // 7% chance to be the one sending a proposal
            }
            memberWeightings.put(total, memberList.get(i).getID()); // Adds the weighting
        }

        // Create the standard amount of proposers, acceptors and learners
        for (int i = 0; i < 3; ++i) { // 3 proposers, allowing for 1 backup proposer if two members need it
            Proposer tempProposer = new Proposer(i);
            proposers.put(tempProposer, tempProposer.getProposerSocket());
        }
        for (int i = 0; i < 5; ++i) { // 5 acceptors
            Acceptor tempAcceptor = new Acceptor(i);
            acceptors.put(tempAcceptor, tempAcceptor.getAcceptorSocket());
        }
        for (int i = 0; i < 9; ++i) { // 9 learners, 1 for each member
            Learner tempLearner = new Learner(i);
            learners.put(tempLearner, tempLearner.getLearnerSocket());
        }
    }

//    // Establish socket connections between a given proposer (p) and all the acceptors
//    public LinkedHashMap<Socket, ArrayList<ObjectStreamConstants>> acceptorsToProposer(Proposer p) {
//        try {
//            // Here, loop through each acceptor and let them accept the given proposer, storing the data
//            for (HashMap.Entry<Acceptor, ServerSocket> curr_acceptor : acceptors.entrySet()) { // Connect proposers
//                Socket acceptorSocket = new Socket("acceptor" + String.valueOf(curr_acceptor.getKey().getID()), 456 + curr_acceptor.getKey().getID()); // port must match acceptor port
//                Socket socketForAcceptor = curr_acceptor.getKey().getAcceptorSocket().accept();
//                ObjectOutputStream out = new ObjectOutputStream(socketForAcceptor.getOutputStream());
//                ObjectInputStream in = new ObjectInputStream(socketForAcceptor.getInputStream());
//                ArrayList<ObjectStreamConstants> streamEntryList = new ArrayList<ObjectStreamConstants>();
//                streamEntryList.add(out);
//                streamEntryList.add(in);
//                propToAcceptorStreams.put(socketForAcceptor, streamEntryList);
//            }
//        } catch (IOException ie) {
//            System.out.println(ie.getMessage());
//        }
//    }

    // Function to pick two random members
    // Returns: IDs of the members picked, stored in ArrayList (used for simultaneous case, so order doesn't matter)
    public ArrayList<String> pickTwoMembers() {
        Random rand = new Random();
        ArrayList<String> chosenMembers = new ArrayList<String>(); // String = ID of the member

        double probability = rand.nextDouble() * 100;
        chosenMembers.add(memberWeightings.higherEntry(probability).getValue());
        probability = rand.nextDouble() * 100;
        chosenMembers.add(memberWeightings.higherEntry(probability).getValue());
        return chosenMembers;
    }

    // Function to pick one random member
    // Returns: ID of the member that was picked
    public String pickAMember() {
        Random rand = new Random();
        ArrayList<String> chosenMembers = new ArrayList<String>(); // String = ID of the member

        double probability = rand.nextDouble() * 100;
        return memberWeightings.higherEntry(probability).getValue();
    }

    // Fetches a proposal from a randomly selected member
    // Special case: Random chance 2 members send proposals simultaneously
    public void fetchProposal() {
        Random rand = new Random();
        boolean doubleSent = rand.nextDouble() < twoProposalChance; // Determine outcome of random chance that two members send

        if (doubleSent) { // Picks 2 random members
            ArrayList<String> chosen = pickTwoMembers();

        } else {
            String chosen = pickAMember(); // Randomly pick 1 member
        }

        return;
    }

    public static void main(String[] args) {
        MemberManagement m = new MemberManagement(); // Create the list of the council members
//        System.out.println("Council members have been registered. Fetching proposal/(s) in 5 seconds. Standby...");
//        try {
//            Thread.sleep(5000);
//        } catch (InterruptedException ie) {
//            System.out.println(ie.getMessage());
//        }
        System.out.println("M" + m.pickAMember());

    }
}
