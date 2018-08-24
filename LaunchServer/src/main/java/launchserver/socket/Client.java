package launchserver.socket;

public class Client {
    public Client(long session) {
        this.session = session;
        this.timestamp = System.currentTimeMillis();
    }

    public long session;
    public long timestamp;

    public void up() {
        this.timestamp = System.currentTimeMillis();
    }
}
