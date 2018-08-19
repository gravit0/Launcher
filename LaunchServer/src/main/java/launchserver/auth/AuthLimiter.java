package launchserver.auth;

import java.util.HashMap;

import launchserver.LaunchServer;

public class AuthLimiter {
    public final int rateLimit;
    public final int rateLimitMilis;
    private HashMap<String,AuthEntry> map;
    public AuthLimiter(LaunchServer srv) {
        map = new HashMap<>();
        rateLimit = srv.config.authRateLimit;
        rateLimitMilis = srv.config.authRateLimitMilis;
    }
    public boolean isLimit(String ip)
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
        @Override
		public String toString() {
			return String.format("AuthEntry {value=%s, ts=%s}", value, ts);
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + (int) (ts ^ (ts >>> 32));
			result = prime * result + value;
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (!(obj instanceof AuthEntry)) {
				return false;
			}
			AuthEntry other = (AuthEntry) obj;
			if (ts != other.ts) {
				return false;
			}
			if (value != other.value) {
				return false;
			}
			return true;
		}

		public int value;
        public long ts;

        public AuthEntry(int i, long l) {
            value = i;
            ts = l;
        }
    }
}
