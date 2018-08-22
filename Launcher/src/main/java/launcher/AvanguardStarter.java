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
    	if (init) GuardBind.init();
    	GuardBind.avnRegisterThreatNotifier((int threatType) -> {
            System.err.println("Threat " + GuardBind.ThreatType.getThreat(threatType).name());
			LogHelper.error("Cheating == crash!");
            System.exit(12);
            return false;
        });
    	// нужно делать до пуска таймера!
    	GuardBind.setCheckTime(3000);
        GuardBind.avnStartDefence();
        CommonHelper.newThread("Security Thread",true,new SecurityThread()).start();
    }
    
    public static void start (Path path1) {
    	Path path = path1.resolve("guard");
    	processArched(
    			handle(path.resolve("Avanguard32.dll"), "Avanguard32.dll"),
    			handle(path.resolve("Avanguard64.dll"), "Avanguard64.dll"));
    }
    
	public static void loadVared() {
    	if (JVMHelper.JVM_BITS == 32) GuardBind.startAbs(System.getProperty("avn32"));
    	else if (JVMHelper.JVM_BITS == 64) GuardBind.startAbs(System.getProperty("avn64"));
	}
    
    private static void processArched(Path arch32, Path arch64) {
    	System.setProperty("avn32", arch32.normalize().toAbsolutePath().toFile().getAbsolutePath());
    	System.setProperty("avn64", arch64.normalize().toAbsolutePath().toFile().getAbsolutePath());
    }
    
    private static Path handle(Path mustdiedll, String resource) {
		try {
			InputStream in = IOHelper.newInput(IOHelper.getResourceURL(resource));
			if (IOHelper.exists(mustdiedll)) {
				if (!matches(mustdiedll, in)) {
					in.reset();
					transfer(in, mustdiedll);
				}
			} else {
				transfer(in, mustdiedll);
			}
		} catch (Exception e) {
			if (e instanceof RuntimeException) throw (RuntimeException) e;
			else throw new RuntimeException(e);
		}
		return mustdiedll;
	}

	private static boolean matches(Path mustdiedll, InputStream in) {
		try {
			return Arrays.equals(SecurityHelper.digest(DigestAlgorithm.MD5, in), SecurityHelper.digest(DigestAlgorithm.MD5, mustdiedll));
		} catch (IOException e) {
			return false;
		}
	}

	private static void transfer(InputStream in, Path mustdiedll) throws IOException {
		IOHelper.createParentDirs(mustdiedll);
		mustdiedll.toFile().createNewFile();
		IOHelper.transfer(in, mustdiedll, false);
	}
    
    static class SecurityThread implements Runnable
    {
        @Override
        public void run() {
            while (true) {
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
