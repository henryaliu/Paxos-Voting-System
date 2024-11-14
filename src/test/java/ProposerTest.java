import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;

class ProposerTest {

    @Test
    // Testing that we can connect to ServerSockets, connect to different SS, and reconnect later on
    public void socketReconnectTest() {
        try {
            ServerSocket ss1 = new ServerSocket(4567);
            ServerSocket ss2 = new ServerSocket(4568);
            Socket s = new Socket();
            s = new Socket("localhost", 4567);
            s.close();
            s = new Socket("localhost", 4568);
        } catch (Exception e) {
            System.out.println("OORAH" + e.getMessage());
        }
    }



}