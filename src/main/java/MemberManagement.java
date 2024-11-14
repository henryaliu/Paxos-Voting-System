import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

public class MemberManagement {

    private final double twoProposalChance = 0.3; // Probability of two members sending proposal
    private static Integer proposalIDCounter = 0;

    // String = name (e.g. m1, m2, etc)
    ArrayList<Member> members = new ArrayList<Member>();

    // 3 Proposers, 9 Acceptors/Learners for availability + non-blocking + fault tolerance

    ArrayList<Proposer> proposers = new ArrayList<Proposer>(); // 3 proposers total
    ArrayList<Acceptor> acceptors = new ArrayList<Acceptor>(); // 1 per member
    ArrayList<Learner> learners = new ArrayList<Learner>(); // 1 per member

    /*
        Fills up the members, acceptors and learners lists with the required number of Members + Acceptors/Learners
        Uses the specified Member profiles for responsiveness and desire to be President
        Proposers are created later when we decide number of proposals to be sent
     */
    public MemberManagement() {
        members.add(new Member(1, 2, 1, true)); // Adds member 1, always responds, wants Presidency
        members.add(new Member(2, 1, 3, false)); // Adds member 2, wants Presidency the most
        members.add(new Member(3, 2, 2, false)); // Adds member 3, wants Presidency
        for (int i = 4; i < 10; ++i) { // For members 4-9
            members.add(new Member(i, 3, 4, true)); // Neutral nominee pick, random responsiveness, always responds
        }

        for (int i = 1; i < members.size() + 1; ++i) {
            acceptors.add(new Acceptor(i, members.get(i - 1)));
            learners.add(new Learner(i, members.get(i - 1)));
        }

        return;
    }

    /*
        For testing the case where all 9 members respond instantly
        specialCase: true = run the case where all members immediately respond, false = default Member profiles
     */
    public MemberManagement(boolean specialCase) {
        if (!specialCase) {
            MemberManagement m = new MemberManagement();
            return;
        } else { // Else, gives all members instant responsivenes
            members.add(new Member(1, 2, 1, true)); // Adds member 1, always responds, wants Presidency
            members.add(new Member(2, 1, 1, true)); // Adds member 2, wants Presidency the most
            members.add(new Member(3, 2, 1, true)); // Adds member 3, wants Presidency
            for (int i = 4; i < 10; ++i) { // For members 4-9
                members.add(new Member(i, 3, 1, true)); // Neutral nominee pick, random responsiveness, always responds
            }

            for (int i = 1; i < 10; ++i) {
                acceptors.add(new Acceptor(i, members.get(i - 1)));
                learners.add(new Learner(i, members.get(i - 1)));
            }

            return;
        }
    }


    /*
        Coordinates the election process
     */
    public void elect() {
        Random random = new Random();
        boolean doubleSent = random.nextDouble() < twoProposalChance; // Decides if two members are sent at same time
        Integer numProposals = 0;
        if (doubleSent) { // If two proposals are sent simultaneously
            numProposals = 2;
        } else {
            numProposals = 1;
        }

        for (int i = 0; i < numProposals; ++i) { // Generates the proposer/(s)
            random = new Random();
            Integer chosenMember = random.nextInt(9) + 1;
            proposalIDCounter++;
            proposers.add(new Proposer(members.get(chosenMember - 1), members.get(chosenMember - 1).nominateWho(9), proposalIDCounter, acceptors));
        }
        Integer finished = 0;
        for (int i = 1; i < proposers.size() + 1; ++i) { // Do the election for both
            if (proposers.get(i - 1).phase1(9, acceptors)) { // stops here???
                if (proposers.get(i - 1).phase2(proposers.get(i - 1).getParticipatingAcceptors().size(), learners)) {
                    finished++;
                }
            }
        }
        if (finished > 0) {
            System.out.println("Election(s) finished.");
        } else {
            System.out.println("Election(s) failed");
        }

        System.out.println("The results are: ");
        String[] keywords = learners.get(0).getMemberUsingThis().getInsight().split(" ");
        System.out.println("Winning Proposal ID: " + keywords[1] + ", Winning Nominee: Member " + keywords[2]);
        return;
    }

    public static void main(String[] args) {
        MemberManagement m = new MemberManagement(); // Create the list of the council members
        m.elect();
    }
}
