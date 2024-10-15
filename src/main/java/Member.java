
// Council Member class
// Represents an individual council member, with customisable properties that can be specified
public class Member {

    private final String id; // Identification for the council member (ID = M(ID) (e.g. M1, ID = 1))
    private final Integer dominance; // Levels 1,2,3 of how badly they want to be president (1 = very, 2 = wants, 3 = indifferent)
    private final double responsiveness; // Scale between 0 and 1 of how responsive the Member is, (0 = no response, 1 = instant)
    private final double reliability; // Scale between 0 and 1 of whether emails get delivered (0 = never, 1 = always)
    private final String condition; // for specifying varying behaviour of M2 and M3

    // Special constructor
    // Arguments: inputID = id, dom = dominance, res = responsiveness, rel = reliability
    public Member(String inputID, Integer dom, double res, double rel, String c) {
        this.id = inputID;
        this.dominance = dom;
        this.responsiveness = res;
        this.reliability = rel;
        this.condition = c;
    }

    // Getters
    public String getID() {
        return this.id;
    }
    public Integer getDominance() {
        return this.dominance;
    }
    public double getResponsiveness() {
        return this.responsiveness;
    }
    public double getReliability() {
        return this.reliability;
    }
    public String getSpecial() {
        return this.condition;
    }

}
