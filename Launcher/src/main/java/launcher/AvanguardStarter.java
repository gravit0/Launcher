package launcher;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Arrays;

import launcher.helper.CommonHelper;
import launcher.helper.IOHelper;
import launcher.helper.JVMHelper;
import launcher.helper.LogHelper;
import launcher.helper.SecurityHelper;
import launcher.helper.SecurityHelper.DigestAlgorithm;
import ru.zaxar163.GuardBind;

public class AvanguardStarter {
    public static void main(boolean init) {
        if (init)
            GuardBind.init();
        GuardBind.avnRegisterThreatNotifier((int threatType) -> {
            System.err.println("Threat " + GuardBind.ThreatType.getThreat(threatType).name());
            LogHelper.error("Cheating == crash!");
            System.exit(12);
            return false;
        });
        // нужно делать до пуска таймера!
        GuardBind.setCheckTime(3000);
        GuardBind.avnStartDefence();
        CommonHelper.newThread("Security Thread", true, new SecurityThread()).start();
    }

    public static void start(Path path1) {
        Path path = path1.resolve("guard");
        processArched(handle(path.resolve("Avanguard32.dll"), "Avanguard32.dll"),
                handle(path.resolve("Avanguard64.dll"), "Avanguard64.dll"),
                handle(path.resolve("wrapper32.exe"), System.getProperty("lauunchergravitd") + "32.exe"),
                handle(path.resolve("wrapper64.exe"), System.getProperty("lauunchergravitd") + "64.exe"), System.getProperty("lauunchergravitd"));
    }

    public static void loadVared() {
        if (JVMHelper.JVM_BITS == 32)
            GuardBind.startAbs(System.getProperty("avn32"));
        else if (JVMHelper.JVM_BITS == 64)
            GuardBind.startAbs(System.getProperty("avn64"));
    }

    private static void processArched(Path arch32, Path arch64, Path wrapper32, Path wrapper64, String path) {
        System.setProperty("avn32", IOHelper.toAbs(arch32));
        System.setProperty("avn64", IOHelper.toAbs(arch64));
        System.setProperty(System.getProperty("lauunchergravitd") + "32", IOHelper.toAbs(wrapper32));
        System.setProperty(System.getProperty("lauunchergravitd") + "64", IOHelper.toAbs(wrapper64));
    }

    private static Path handle(Path mustdiedll, String resource) {
        try {
            InputStream in = IOHelper.newInput(IOHelper.getResourceURL(resource));
            byte[] orig = IOHelper.toByteArray(in);
            in.close();
            if (IOHelper.exists(mustdiedll)) {
                if (!matches(mustdiedll, orig)) {
                    transfer(orig, mustdiedll);
                }
            } else {
                transfer(orig, mustdiedll);
            }
        } catch (Exception e) {
            if (e instanceof RuntimeException)
                throw (RuntimeException) e;
            else
                throw new RuntimeException(e);
        }
        return mustdiedll;
    }

    private static boolean matches(Path mustdiedll, byte[] in) {
        try {
            return Arrays.equals(SecurityHelper.digest(DigestAlgorithm.MD5, in),
                    SecurityHelper.digest(DigestAlgorithm.MD5, mustdiedll));
        } catch (IOException e) {
            return false;
        }
    }

    private static void transfer(byte[] orig, Path mustdiedll) throws IOException {
        IOHelper.createParentDirs(mustdiedll);
        if (!IOHelper.exists(mustdiedll))
            mustdiedll.toFile().createNewFile();
        IOHelper.transfer(orig, mustdiedll, false);
    }

    static class SecurityThread implements Runnable {
        @Override
        public void run() {
            while (!Thread.interrupted()) {
                try {
                    if (!GuardBind.avnIsStarted()) {
                        LogHelper.error("Avanguard stopped! Process stopped");
                        System.exit(5);
                    }
                } catch (NullPointerException e) {
                    LogHelper.error("Avanguard unloaded! Process stopped");
                    System.exit(6);
                }
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    if (!GuardBind.avnIsStarted()) {
                        LogHelper.error("Thread stopped! Process stopped");
                        System.exit(7);
                    }
                }
            }
        }
    }
}
