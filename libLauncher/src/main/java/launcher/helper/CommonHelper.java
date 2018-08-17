package launcher.helper;

import java.util.Locale;
import javax.script.ScriptEngine;

import jdk.nashorn.api.scripting.NashornScriptEngineFactory;
import launcher.Launcher;
import launcher.LauncherAPI;

public final class CommonHelper {
    @LauncherAPI
    public static final String VERSIONREPLACE = "$VERSION$";
    @LauncherAPI
    public static final String BUILDREPLACE = "$BUILDNUMBER$";
    @LauncherAPI
    public static final String[] repArray = genReps();
    private static final String[] SCRIPT_ENGINE_ARGS = { "-strict" };

    private CommonHelper() {
    }

    @LauncherAPI
    public static String low(String s) {
        return s.toLowerCase(Locale.US);
    }
    
    @LauncherAPI
    public static ScriptEngine newScriptEngine() {
        return new NashornScriptEngineFactory().getScriptEngine(SCRIPT_ENGINE_ARGS);
    }

    @LauncherAPI
    public static Thread newThread(String name, boolean daemon, Runnable runnable) {
        Thread thread = new Thread(runnable);
        thread.setDaemon(daemon);
        if (name != null) {
            thread.setName(name);
        }
        return thread;
    }

    @LauncherAPI
    public static String replace(String source, String... params) {
        for (int i = 0; i < params.length; i += 2) {
            source = source.replace('%' + params[i] + '%', params[i + 1]);
        }
        return source;
    }

    private static String[] genReps() {
        Replace[] replace = new Replace[] { new Replace(VERSIONREPLACE, Launcher.VERSION),
                new Replace(BUILDREPLACE, Launcher.BUILD), };
        String[] repArray = new String[replace.length * 2];
        int i = 0;
        for (Replace r : replace) {
            repArray[i] = r.getSearch();
            repArray[i + 1] = r.getReplacement();
            i += 2;
        }
        return repArray;
    }

    @LauncherAPI
    public static String formatVars(String in) {
        return replace(in, repArray);
    }

    public static final class Replace {
        private final String search;
        private final String replacement;

        public Replace(String search, String replacement) {
            this.search = search;
            this.replacement = replacement;
        }

        public String getReplacement() {
            return replacement;
        }

        public String getSearch() {
            return search;
        }
    }
}
