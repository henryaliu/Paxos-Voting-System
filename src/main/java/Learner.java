import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class Learner {

    private Socket learnerSocket;
    private ObjectOutputStream output;
    private ObjectInputStream input;

    private Integer ID;

    // Constructor: connects socket and streams
    public Learner(Integer learnerID) {
        this.ID = learnerID;
    }

    // Getters
    public Socket getLearnerSocket() {
        return learnerSocket;
    }
    public Integer getID() {
        return this.ID;
    }

    // Checks socket/stream for notice sent by Acceptor if specified member's proposal was accepted
    public boolean receiveNotice(Member member) {

        return false;
    }

}
