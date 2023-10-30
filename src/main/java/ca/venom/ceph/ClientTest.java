package ca.venom.ceph;

public class ClientTest {
    public static void main(String[] args) throws Exception {
        CephNettyClient client = new CephNettyClient(args[0], Integer.parseInt(args[1]), args[2], args[3]);
        client.start();
    }
}
