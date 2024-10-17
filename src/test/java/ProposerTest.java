import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Array;
import java.util.ArrayList;
import java.util.regex.PatternSyntaxException;

import static org.junit.jupiter.api.Assertions.*;

class ProposerTest {

    @Test
    // Unit test for sending Prepare message
    // Testing that the socket co
    void sendPrepare() {
        Proposer p = new Proposer(1);
        Member m1 = new Member(1, 1, 1, 1, "none");
        Acceptor a1 = new Acceptor(1);
        assertDoesNotThrow(() -> p.sendPrepare(m1, a1, ));
    }

    @Test
    // To test out whether I can use split to determine if PROMISE was sent in the message by acceptor or not
    void promiseOrNoPromiseTest() {
        String withPromise = "PROMISE 3";
        String withoutPromise = "3";

        assertNotNull(withPromise.split(" ", 2));
        assertNotNull(withoutPromise.split(" ", 2));
    }

    @Test
    // Little test to check what I can do with ServerSocket
    void isBoundTest() throws IOException {
        ServerSocket ss = new ServerSocket(4567);
        Socket s = new Socket("localhost", 4567);
        Socket temp = ss.accept();
    }

    @Test
    void sendPropose() {
    }
}