package launchserver.auth;

import java.util.HashMap;

public class AuthLimiter {
    public static int rateLimit;
    public static int rateLimitMilis;
    static private HashMap<String,AuthEntry> map;
    static {
        map = new HashMap<>();
    }
    public static boolean isLimit(String ip)
    {
        if(map.containsKey(ip))
        {
            AuthEntry rate = map.get(ip);
            long currenttime = System.currentTimeMillis();
            if(rate.ts + rateLimitMilis < currenttime) rate.value = 0;
            if(rate.value >= rateLimit && rateLimit > 0) {
                rate.value++;
                rate.ts = currenttime;
                return true;
            }
            rate.value++;
            rate.ts = currenttime;
            return false;
        }
        else {map.put(ip,new AuthEntry(1,System.currentTimeMillis())); return false;}
    }
    static class AuthEntry
    {
        public int value;
        public long ts;

        public AuthEntry(int i, long l) {
            value = i;
            ts = l;
        }
    }
}
