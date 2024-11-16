import java.util.Random;

// Council Member class
// Represents an individual council member, with customisable properties that can be specified
public class Member {

    private final Integer id; // Identification for the council member (ID = M(ID) (e.g. M1, ID = 1))
    private final Integer dominance; // Likelihood of the Member nominating themselves (1-3, 1 = most wants to be, 3 = neutral)
    private final Integer responsiveness; // Response time factor (varies based on decideResponseTime()) (1-4, 1 = most responsive, 4=random)
    private final boolean reliable; // true = always responds eventually, false = occasionally fails to respond

    private Random rand;
    private Boolean disconnected = false;

    private String insight; // Outcome of an election, empty if the Member doesn't know

    // Special constructor
    // Arguments: inputID = id, dom = dominance, res = responsiveness, rel = reliability
    public Member(Integer inputID, Integer dom, Integer res, boolean reliability) {
        this.id = inputID;
        this.dominance = dom;
        this.responsiveness = res;
        this.reliable = reliability;
    }

    // Getters/Setters
    public Integer getID() {
        return this.id;
    }
    public Integer getDominance() {
        return this.dominance;
    }
    public Integer getResponsiveness() {
        return this.responsiveness;
    }
    public boolean getReliability() { return this.reliable; }
    public String getInsight() { return this.insight; }
    public void giveInsight(String data) { this.insight = data; } // For giving the Member knowledge of election result

    // Method for deciding response time of specified member
    // Based on Member's responsiveness (how bad they want to be president)
    // Used for Thread.sleep() to simulate responsiveness
    public long decideResponseTime() {
        if (this.getResponsiveness().equals(1)) {
            return 0; // No delay
        } else if (this.getResponsiveness().equals(2)) {
            return 8000; // 1s delay
        } else if (this.getResponsiveness().equals(3)) {
            return 3000; // 3.5s delay
        } else { // Random time within 5 and 1.5 seconds
            Random random = new Random();
            return random.nextInt(5000 - 2000 + 1) + 2000;
        }
    }

    /*
        Simulates if the Member will actually respond during a vote, regardless of the response time
        true = respond, false = no response
     */
    public boolean respondOrNot() {
        Random random = new Random();
        if (reliable) { return true; } // Reliable members always respond
        return random.nextBoolean(); // For members 2 and 3 who are unreliable in responding
    }

    /*
        Simulate the person the member will nominate
        Returns Integer (member ID) of the nominee
        numMembers: Range for random nominee ID (1 to numMembers)
     */
    public Integer nominateWho(Integer numMembers) {
        Random random = new Random();
        switch (dominance) {
            case 1: // Most dominant always nominates itself
                return this.id;
            case 2: // 80% chance of nominating itself, might nominate on rare occasion a truly golden candidate
                double probability = random.nextDouble();
                if (probability < 0.8) {
                    return this.id;
                } else {
                    return random.nextInt(numMembers) + 1; // 5% chance to return random member as nominee
                }
            case 3: // Neutral, picks a random member
                return random.nextInt(numMembers) + 1;
            default:
                return random.nextInt(numMembers) + 1; // Default is unbiased proposal
        }
    }

}
