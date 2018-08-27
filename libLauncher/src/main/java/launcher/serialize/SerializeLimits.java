package launcher.serialize;

import launcher.LauncherAPI;

public class SerializeLimits {
    @LauncherAPI
    public static final int MAX_LOGIN = 1024;
    @LauncherAPI
    public static final int MAX_CLIENT = 128;
    @LauncherAPI
    public static final int MAX_SERVERID = 128;
    @LauncherAPI
    public static final int MAX_QUEUE_SIZE = 128;
    @LauncherAPI
    public static final int MAX_BATCH_SIZE = 128;
    @LauncherAPI
    public static final byte EXPECTED_BYTE = 0b01010101;
}
