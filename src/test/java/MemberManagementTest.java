import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;

import static org.junit.jupiter.api.Assertions.*;

class MemberManagementTest {

    @Test
    // Small test of code snippet for finding highest frequency HashMap value
    void highestFrequencyValueTest() {
        LinkedHashMap<Integer, Integer> chosenIDFrequencies = new LinkedHashMap<Integer, Integer>(); // Key: chosenID, Value: frequency
        chosenIDFrequencies.put(1, 2);
        chosenIDFrequencies.put(2, 3);
        chosenIDFrequencies.put(1, 1);
        Integer chosenID = Collections.max(chosenIDFrequencies.entrySet(), HashMap.Entry.comparingByValue()).getKey();
        assertEquals(2, chosenID);
    }
}