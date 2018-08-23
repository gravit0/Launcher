package launchserver.manangers;

import launcher.LauncherAPI;
import launchserver.NeedGarbageCollection;
import launchserver.response.Client;

import java.util.Set;

public class SessionManager implements NeedGarbageCollection {
    @LauncherAPI public static final long SESSION_TIMEOUT = 10*60*1000; // 10 минут
    private Set<Client> clientSet;
    @LauncherAPI public boolean addClient(Client client)
    {
        clientSet.add(client);
        return true;
    }
    @LauncherAPI public Client getClient(long session)
    {
        for(Client c : clientSet)
        {
            if(c.session == session) return c;
        }
        return null;
    }
    @LauncherAPI public void updateClient(long session)
    {
        for(Client c : clientSet)
        {
            if(c.session == session) { c.up(); return; }
        }
    }
    @LauncherAPI public void garbageCollection()
    {
        long time = System.currentTimeMillis();
        clientSet.removeIf(c -> (c.timestamp + SESSION_TIMEOUT) < time);
    }
}
