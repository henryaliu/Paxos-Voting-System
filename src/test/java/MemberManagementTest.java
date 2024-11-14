import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class MemberManagementTest {
    @Test
    // Testng probability of two proposals
    public void proposalChanceTest() {
        for (int i = 0; i < 10; ++i) {
            Random random = new Random();
            boolean doubleSent = random.nextDouble() < 0.3; // Decides if two members are sent at same time
            Integer numProposals = 0;
            if (doubleSent) { // If two proposals are sent simultaneously
                numProposals = 2;
            } else {
                numProposals = 1;
            }
            System.out.println(numProposals);
        }
    }

    @Test
    // Testing the random selection of members
    public void randomMemberTest() {
        for (int i = 0; i < 9; ++i) {
            Random random = new Random();
            Integer chosenMember = random.nextInt(9) + 1;
            System.out.println(chosenMember);
        }
    }
}