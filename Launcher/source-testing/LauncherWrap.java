package launcher;

public final class LauncherWrap {
    private LauncherWrap() {
    }

    public static void main(String... args) throws Throwable {
        LauncherEngine.main(args); // Just for test runtime
    }
}
