import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MemberTest {

    @Test
    public void respondOrNotTest() {
        Member m = new Member(1, 2, 1, false);
        for (int i = 0; i < 10; ++i) {
            System.out.println(m.respondOrNot());
        }
    }

}