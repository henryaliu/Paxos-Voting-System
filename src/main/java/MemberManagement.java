import java.util.ArrayList;
import java.util.Random;

// Organises the members into a group that can be easily accessed
// Where the program execution occurs (main function)
public class MemberManagement {
    private ArrayList<Member> memberList; // List of all members
    private ArrayList<String> IDList; // Stores list of all member IDs for easy access
    private final double twoProposalChance = 0.3; // Probability of two members sending proposal

    // Default constructor: creates all the members with their specific settings
    public MemberManagement() {
        memberList = new ArrayList<Member>();
        memberList.add(new Member("1", 2, 1.0, 1.0, "none")); // M1
        memberList.add(new Member("2", 1, 0.1, 0.1, "M2")); // M2
        memberList.add(new Member("3", 2, 0.5, 0.2, "M3")); // M3
        for (int i = 4; i < 10; ++i) { // Create members M4 to M9
            memberList.add(new Member(String.valueOf(i), 3, 0.5, 0.5, "none"));
        }
        for (int i = 0; i < memberList.size(); ++i) {
            IDList.add(memberList.get(i).getID()); // Add all the ids in IDList
        }
    }

    // Fetches a proposal from a randomly selected member
    // Special case: Random chance 2 members send proposals simultaneously
    public void fetchProposal() {
        Random rand = new Random();
        boolean doubleSent = rand.nextDouble() < twoProposalChance; // Determine outcome of random chance that two members send

        if (doubleSent) {
            rand = new Random(); // Reset randomness
            int index = 0;
            while (index < 2) {

            }
            // Randomly pick 2 members
        } else {
            // Randomly pick 1 member
        }
        return;
    }

    public static void main(String[] args) {
        MemberManagement m = new MemberManagement(); // Create the list of the council members
        System.out.println("Council members have been registered. Fetching proposal/(s) in 5 seconds. Standby...");
        try {
            Thread.sleep(5000);
        } catch (InterruptedException ie) {
            System.out.println(ie.getMessage());
        }
        System.out.println("OO");

    }
}
