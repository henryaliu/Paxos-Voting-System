import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class Learner {

    private ServerSocket learnerSocket;

    // Assumes only 1 member per learner
    private Socket referenceSocket; // Socket created from connection from incoming socket
    private ObjectOutputStream outputStream;
    private ObjectInputStream inputStream;

    private Integer port;
    private Member memberUsingThis;

    /*
        learnerID: To identify the learner
        member: Member who is using this Learner
     */
    public Learner(Integer learnerID, Member member) {
        this.port = 8000 + learnerID;
        this.memberUsingThis = member;
        try {
            learnerSocket = new ServerSocket(this.port);
        } catch (Exception e) {
            System.out.println("ERROR: " + e.getMessage());
        }
    }

    public void acceptConnection() {
        try {
            referenceSocket = learnerSocket.accept();
            return;
        } catch (Exception e) {
            System.out.println("ERROR: " + e.getMessage());
        }
    }

    public void constructStreams() {
        try {
            outputStream = new ObjectOutputStream(referenceSocket.getOutputStream());
            inputStream = new ObjectInputStream(referenceSocket.getInputStream());
            return;
        } catch (Exception e) {
            System.out.println("ERROR: " + e.getMessage());
        }
    }

    /*
        Assumes socket connection already established from acceptConnection()
        Reads the input and lets the Member using this Learner know
     */
    public void receive() {
        try {
            this.memberUsingThis.giveInsight((String) inputStream.readObject());
        } catch (Exception e) {
            System.out.println("ERROR: Learner failed to receive insight - " + e.getMessage());
        }
    }

    public ServerSocket getLearnerSocket() {
        return learnerSocket;
    }
    public ObjectOutputStream getOutputStream() {
        return outputStream;
    }
    public ObjectInputStream getInputStream() {
        return inputStream;
    }
    public Integer getPort() {
        return this.port;
    }
    public Member getMemberUsingThis() {
        return this.memberUsingThis;
    }
}
