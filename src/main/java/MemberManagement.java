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
    private ArrayList<Integer> IDList = new ArrayList<Integer>(); // Stores list of all member IDs for easy access
    private final double twoProposalChance = 0.3; // Probability of two members sending proposal

    // Key: dominance, Value: ID
    private final NavigableMap<Double, Integer>  memberWeightings = new TreeMap<Double, Integer>();

    // Ordered maps containing the messengers and the socket for convenient communication
    private ArrayList<Proposer> proposers = new ArrayList<Proposer>();
    private ArrayList<Acceptor> acceptors = new ArrayList<Acceptor>();
    private ArrayList<Learner> learners = new ArrayList<Learner>();

    // Ordered map of output+input streams for each Acceptor connected to a Proposer
    // Each key (Socket) is a proposer socket for an acceptor
    private LinkedHashMap<Socket, ArrayList<ObjectStreamConstants>> propToAcceptorStreams = new LinkedHashMap();

    // The outcome of a vote is stored here for universal access across all methods
    private Integer chosenID, chosenMember;

    // Default constructor: creates all the members with their specific settings
    public MemberManagement() {
        memberList = new ArrayList<Member>();
        memberList.add(new Member(1, 2, "immediate", "none")); // M1
        memberList.add(new Member(2, 1, "large delay", "M2")); // M2
        memberList.add(new Member(3, 2, "small delay", "M3")); // M3
        for (int i = 4; i < 10; ++i) { // Creates members M4 to M9 and adds them to list of members
            memberList.add(new Member(i, 3, "small delay", "none"));
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
            proposers.add(tempProposer);
        }
        for (int i = 0; i < 5; ++i) { // 5 acceptors
            Acceptor tempAcceptor = new Acceptor(i);
            acceptors.add(tempAcceptor);
        }
        for (int i = 0; i < 9; ++i) { // 9 learners, 1 for each member
            Learner tempLearner = new Learner(i);
            learners.add(tempLearner);
        }
    }

    // Function to pick two random members
    // Returns: IDs of the members picked, stored in ArrayList (used for simultaneous case, so order doesn't matter)
    public ArrayList<Integer> pickTwoMembers() {
        Random rand = new Random();
        ArrayList<Integer> chosenMembers = new ArrayList<Integer>(); // String = ID of the member

        double probability = rand.nextDouble() * 100;
        chosenMembers.add(memberWeightings.higherEntry(probability).getValue());
        probability = rand.nextDouble() * 100;
        chosenMembers.add(memberWeightings.higherEntry(probability).getValue());
        return chosenMembers;
    }

    // Function to pick one random member
    // Returns: ID of the member that was picked
    public Integer pickAMember() {
        Random rand = new Random();
        ArrayList<String> chosenMembers = new ArrayList<String>(); // String = ID of the member

        double probability = rand.nextDouble() * 100;
        return memberWeightings.higherEntry(probability).getValue();
    }

    // Fetches a proposal from a randomly selected member
    // Special case: Random chance 2 members send proposals simultaneously
    // Gets
    public void castVote() throws IOException {
        Random rand = new Random();
        boolean doubleSent = rand.nextDouble() < twoProposalChance; // Determine outcome of random chance that two members send
        ArrayList<Integer> chosenIDAndMember = new ArrayList<Integer>();
        if (doubleSent) { // Picks 2 random members
            // Designates the fair members (M4 to M9) as the acceptors
            for (int i = 1; i < 3; ++i) { // Sends 2 proposals (uses 1st and 2nd one, 3rd is only for backup)
                ArrayList<Integer> chosenMembers = pickTwoMembers(); // Chosen IDs of members
                ArrayList<Integer> chosenIDs = new ArrayList<Integer>(); // Chosen IDs of members
                ArrayList<Integer> associatedMemberIDs = new ArrayList<Integer>();
                LinkedHashMap<Integer, Integer> chosenIDFrequencies = new LinkedHashMap<Integer, Integer>(); // Key: chosenID, Value: frequency
                for (Member curr_member : memberList) { // Send proposal to each member
                    if (proposers.get(i).sendPrepare(memberList.get(chosenMembers.get(i)), acceptors.get(i), curr_member)) { // If sendPrepare worked
                        chosenIDs.add(proposers.get(i).getChosenProposalID()); // Adds the chosenID after Prepare was sent
                        proposers.get(i).getProposerSocket().close(); // Close the socket in case this proposer's socket is used for another acceptor
                        continue;
                    } else {
                        System.out.println("Sending Prepare to Member M" + curr_member.getID() + " failed. Continuing on...");
                        continue;
                    }
                }
                for (Integer id : chosenIDs) { // Puts the chosenIDs into frequency map
                    if (chosenIDFrequencies.containsKey(id)) {
                        chosenIDFrequencies.put(id, chosenIDFrequencies.get(id) + 1);
                    } else {
                        chosenIDFrequencies.put(id, 1);
                    }
                }
                // Gets highest frequency value in the chosenIDFrequencies HashMap
                chosenIDAndMember.add(Collections.max(chosenIDFrequencies.entrySet(), HashMap.Entry.comparingByValue()).getKey());
                chosenIDAndMember.add(memberList.get(chosenMembers.get(i)).getID());
            }
        } else {
            Integer chosen = pickAMember(); // Randomly pick 1 member
            ArrayList<Integer> chosenMembers = pickTwoMembers(); // Chosen IDs of members
            ArrayList<Integer> chosenIDs = new ArrayList<Integer>(); // Chosen IDs of members
            LinkedHashMap<Integer, Integer> chosenIDFrequencies = new LinkedHashMap<Integer, Integer>(); // Key: chosenID, Value: frequency
            for (Member curr_member : memberList) { // Send proposal to each member
                if (curr_member.getID() < 4) { // Members with ID from 0 to 4
                    if (proposers.get(0).sendPrepare(memberList.get(chosenMembers.get(0)), acceptors.get(curr_member.getID()), curr_member)) { // If sendPrepare worked (NOTE: only uses first 2 acceptors)
                        chosenIDs.add(proposers.get(0).getChosenProposalID()); // Adds the chosenID after Prepare was sent
                        continue;
                    } else {
                        System.out.println("Failed to send Prepare message to an acceptor, proposal failed.");
                        return;
                    }
                } else {
                    Integer reIndex = curr_member.getID() - 5; // for acceptors.get(...) (offsets value back by 5)
                    if (proposers.get(0).sendPrepare(memberList.get(chosenMembers.get(0)), acceptors.get(reIndex), curr_member)) { // If sendPrepare worked (NOTE: only uses first 2 acceptors)
                        chosenIDs.add(proposers.get(0).getChosenProposalID()); // Adds the chosenID after Prepare was sent
                        continue;
                    } else {
                        System.out.println("Failed to send Prepare message to an acceptor, proposal failed.");
                        return;
                    }
                }
            }
            for (Integer id : chosenIDs) { // Puts the chosenIDs into frequency map
                if (chosenIDFrequencies.containsKey(id)) {
                    chosenIDFrequencies.put(id, chosenIDFrequencies.get(id) + 1);
                } else {
                    chosenIDFrequencies.put(id, 1);
                }
            }
            // Gets highest frequency value in the chosenIDFrequencies HashMap
            chosenIDAndMember.add(Collections.max(chosenIDFrequencies.entrySet(), HashMap.Entry.comparingByValue()).getKey());
            chosenIDAndMember.add(memberList.get(chosenMembers.get(0)).getID());
        }
        Integer chosenID, chosenMember;
        // Check which member sent the highest proposal ID
        if (chosenIDAndMember.get(0) > chosenIDAndMember.get(2)) {
            chosenID = chosenIDAndMember.get(0);
            chosenMember = chosenIDAndMember.get(1);
        } else {
            chosenID = chosenIDAndMember.get(2);
            chosenMember = chosenIDAndMember.get(3);
        }
        chosenMember = this.chosenMember; // Assign chosenMember from this vote to class-wide storage
        chosenID = this.chosenID; // Assign chosen proposal ID from this vote to class-wide storage
        return;
    }

    // For sending a Propose to all acceptors (once the consensus on Proposal ID has been found from castVote())
    // memberID: member who will be elected President, proposalID: Chosen proposal's ID
    public void castPropose(Integer memberID, Integer proposalID) {
        int proposerToUser = 0; // Index of proposers array
        for (Proposer p : proposers) { // Checks for first working proposer to use
            if (proposerToUser == 2) { // If we went through all proposers
                System.out.println("No proposers are available! Sending Propose message failed.");
                return;
            }
            if (!p.getProposerSocket().isConnected() || !p.getProposerSocket().isBound()) {
                proposerToUser++;
                continue;
            } else {
                break;
            }
        }

        for (Member curr_member : memberList) {
            if (curr_member.getID() < 4) { // Members with ID from 0 to 4
                if (proposers.get(0).sendPropose(proposalID, memberList.get(memberID), acceptors.get(curr_member.getID()))) { // If sendPrepare worked (NOTE: only uses first 2 acceptors)
                    continue;
                } else {
                    System.out.println("Failed to send Prepare message to an acceptor, proposal failed.");
                    return;
                }
            } else {
                Integer reIndex = curr_member.getID() - 5; // for acceptors.get(...) (offsets value back by 5)
                if (proposers.get(0).sendPropose(proposalID, memberList.get(memberID), acceptors.get(reIndex))) { // If sendPrepare worked (NOTE: only uses first 2 acceptors)
                    continue;
                } else {
                    System.out.println("Failed to send Prepare message to an acceptor, proposal failed.");
                    return;
                }
            }
        }
    }



    public static void main(String[] args) {
        MemberManagement m = new MemberManagement(); // Create the list of the council members
//        System.out.println("Council members have been registered. Fetching proposal/(s) in 5 seconds. Standby...");
//        try {
//            Thread.sleep(5000);
//        } catch (InterruptedException ie) {
//            System.out.println(ie.getMessage());
//        }
        m.castVote();
        m.casePropose();

    }
}
