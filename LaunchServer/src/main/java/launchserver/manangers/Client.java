package launchserver.manangers;

public class Client {
    public Client(long session)
    {
        this.session = session;
        this.timestamp = System.currentTimeMillis();
    }
    long session;
    long timestamp;
    public void up()
    {
        this.timestamp = System.currentTimeMillis();
    }
}
