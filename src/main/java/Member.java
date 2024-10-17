import java.util.Random;

// Council Member class
// Represents an individual council member, with customisable properties that can be specified
public class Member {

    private final Integer id; // Identification for the council member (ID = M(ID) (e.g. M1, ID = 1))
    private final Integer dominance; // Levels 1,2,3 of how badly they want to be president (1 = very, 2 = wants, 3 = indifferent)
    private final String responsiveness; // "instant, small delay, large delay, no response"
    private final String condition; // for specifying varying behaviour of M2 and M3

    // Special constructor
    // Arguments: inputID = id, dom = dominance, res = responsiveness, rel = reliability
    public Member(Integer inputID, Integer dom, String res, String c) {
        this.id = inputID;
        this.dominance = dom;
        this.responsiveness = res;
        this.condition = c;
    }

    // Getters
    public Integer getID() {
        return this.id;
    }
    public Integer getDominance() {
        return this.dominance;
    }
    public String getResponsiveness() {
        return this.responsiveness;
    }
    public String getSpecial() {
        return this.condition;
    }

    // Method for deciding response time of specified member
    // Based on Member's responsiveness (how bad they want to be president)
    // Used for Thread.sleep() to simulate responsiveness
    public long decideMemberResponseTime() {
        if (this.getResponsiveness().equals("instant")) { // Most Dominant
            return 0; // No delay
        } else if (this.getResponsiveness().equals("small delay")) {
            return 1000; // 1s delay
        } else if (this.getResponsiveness().equals("large delay")) {
            return 3500; // 3.5s delay
        } else { // No response
            return 30000; // Filter this with if condition before Thread.sleep() to not respond in methods that use this method
        }
    }

}
