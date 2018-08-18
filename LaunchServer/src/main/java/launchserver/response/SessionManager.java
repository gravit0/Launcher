package launchserver.response;

import launcher.LauncherAPI;

import java.util.Set;

public class SessionManager {
    @LauncherAPI public static final long SESSION_TIMEOUT = 10*60*1000; // 10 минут
    private static Set<Client> clientSet;
    @LauncherAPI public static boolean addClient(Client client)
    {
        clientSet.add(client);
        return true;
    }
    @LauncherAPI public static Client getClient(long session)
    {
        for(Client c : clientSet)
        {
            if(c.session == session) return c;
        }
        return null;
    }
    @LauncherAPI public static void garbareCollection()
    {
        long time = System.currentTimeMillis();
        clientSet.removeIf(c -> (c.timestamp + SESSION_TIMEOUT) < time);
    }
}
