package launcher;

import java.io.IOException;
import java.util.Objects;

import launcher.helper.IOHelper;

public class LauncherVersion {
    public static int MAJOR = 4;
    public static int MINOR = 0;
    public static int PATCH = 0;
    public static int BUILD = readBuildNumber();

    public static LauncherVersion getVersion() {
        return new LauncherVersion(MAJOR,MINOR,PATCH,BUILD);
    }
    static int readBuildNumber() {
        try {
            return Integer.valueOf(IOHelper.request(IOHelper.getResourceURL("buildnumber")));
        } catch (IOException ignored) {
            return 0; // Maybe dev env?
        }
    }
    public final int major;
    public final int minor;

    public final int patch;

    public final int build;

    public LauncherVersion(int major, int minor, int patch) {
        this.major = major;
        this.minor = minor;
        this.patch = patch;
        build = 0;
    }

    public LauncherVersion(int major, int minor, int patch,int build) {
        this.major = major;
        this.minor = minor;
        this.patch = patch;
        this.build = build;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LauncherVersion that = (LauncherVersion) o;
        return major == that.major &&
                minor == that.minor &&
                patch == that.patch &&
                build == that.build;
    }
    
    public String getVersionString() {
    	return String.format("%d.%d.%d", major, minor, patch);
    }

    @Override
    public int hashCode() {
        return Objects.hash(major, minor, patch, build);
    }

    @Override
    public String toString() {
        return "LauncherVersion{" +
                "major=" + major +
                ", minor=" + minor +
                ", patch=" + patch +
                ", build=" + build +
                '}';
    }
}
