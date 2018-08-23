package launcher.helper;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;

import launcher.Launcher;
import launcher.LauncherAPI;

public final class CommonHelper {
	@LauncherAPI
    public static final String VERSIONREPLACE = "$VERSION$";
    @LauncherAPI
    public static final String BUILDREPLACE = "$BUILDNUMBER$";
    @LauncherAPI
    public static final String[] repArray = genReps();
    @LauncherAPI
    public static final ScriptEngineManager scriptManager = new ScriptEngineManager();
    @LauncherAPI
    public static final ScriptEngineFactory nashornFactory = getEngineFactories(scriptManager);

	private static ScriptEngineFactory getEngineFactories(ScriptEngineManager manager) {
		// Метод похож на костыль но таковым не является, ибо единоразовое получение фактории быстрее, чем её переполучение на ходу.
    	for (ScriptEngineFactory fact :  manager.getEngineFactories()) {
			if (fact.getNames().contains("nashorn") || fact.getNames().contains("Nashorn")) return fact;
		}
    	return null;
	}
    
    private CommonHelper() {
    }
    
	@LauncherAPI
    public static String low(String s) {
        return s.toLowerCase(Locale.US);
    }
    
    @LauncherAPI
    public static ScriptEngine newScriptEngine() {
    	return nashornFactory.getScriptEngine();
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
    public static String multiReplace(Pattern[] pattern, String from, String replace) {
    	Matcher m;
    	String tmp = null;
    	for (Pattern p : pattern) {
    		m = p.matcher(from);
    		if (m.matches()) tmp = m.replaceAll(replace);
    	}
    	return tmp != null ? tmp : from;
    }
    
    @LauncherAPI
    public static boolean multiMatches(Pattern[] pattern, String from) {
    	for (Pattern p : pattern) {
    		if (p.matcher(from).matches()) return true;
    	}
    	return false;
    }
    
    @LauncherAPI
    public static String formatVars(String in) {
        return replace(in, repArray);
    }

    private static final class Replace {
        private final String search;
        private final String replacement;

        private Replace(String search, String replacement) {
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
