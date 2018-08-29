package launcher.helper;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import launcher.LauncherAPI;
import launcher.LauncherVersion;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.ConfigurationFactory;
import org.apache.logging.log4j.core.config.composite.CompositeConfiguration;
import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.Ansi.Attribute;
import org.fusesource.jansi.Ansi.Color;
import org.fusesource.jansi.AnsiConsole;
import org.fusesource.jansi.AnsiOutputStream;

public final class LogHelper {
    private static Logger logger = LogManager.getLogger("LogHelper");
    @LauncherAPI
    public static final String DEBUG_PROPERTY = "launcher.debug";
    @LauncherAPI
    public static final String NO_JANSI_PROPERTY = "launcher.noJAnsi";
    @LauncherAPI
    public static final boolean JANSI;

    // Output settings
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm:ss", Locale.US);
    private static final AtomicBoolean DEBUG_ENABLED = new AtomicBoolean(Boolean.getBoolean(DEBUG_PROPERTY));
    private static final Set<Output> OUTPUTS = Collections.newSetFromMap(new ConcurrentHashMap<>(2));
    private static final Output STD_OUTPUT;

    private LogHelper() {
    }

    @LauncherAPI
    public static void addOutput(Output output) {
        OUTPUTS.add(Objects.requireNonNull(output, "output"));
    }

    @LauncherAPI
    public static void addOutput(Path file) throws IOException {
        if (JANSI) {
            addOutput(new JAnsiOutput(IOHelper.newOutput(file, true)));
        } else {
            addOutput(IOHelper.newWriter(file, true));
        }
    }

    @LauncherAPI
    public static void addOutput(Writer writer) {
        addOutput(new WriterOutput(writer));
    }

    @LauncherAPI
    public static void debug(String message) {
        if (isDebugEnabled()) {
            log(Level.DEBUG, message, false);
        }
    }

    @LauncherAPI
    public static void debug(String format, Object... args) {
        debug(String.format(format, args));
    }

    @LauncherAPI
    public static void error(Throwable exc) {
        error(isDebugEnabled() ? toString(exc) : exc.toString());
    }

    @LauncherAPI
    public static void error(String message) {
        log(Level.ERROR, message, false);
    }

    @LauncherAPI
    public static void error(String format, Object... args) {
        error(String.format(format, args));
    }

    @LauncherAPI
    public static void info(String message) {
        log(Level.INFO, message, false);
    }

    @LauncherAPI
    public static void info(String format, Object... args) {
        info(String.format(format, args));
    }

    @LauncherAPI
    public static boolean isDebugEnabled() {
        return DEBUG_ENABLED.get();
    }

    @LauncherAPI
    public static void setDebugEnabled(boolean debugEnabled) {
        DEBUG_ENABLED.set(debugEnabled);
    }

    @LauncherAPI
    public static void log(Level level, String message, boolean sub) {
        logger.log(level,message);
    }

    public static void logInit()
    {

    }

    @LauncherAPI
    public static void printVersion(String product) {
        println(JANSI ? ansiFormatVersion(product) : formatVersion(product));
    }

    @LauncherAPI
    public static synchronized void println(String message) {
        for (Output output : OUTPUTS) {
            output.println(message);
        }
    }

    @LauncherAPI
    public static boolean removeOutput(Output output) {
        return OUTPUTS.remove(output);
    }

    @LauncherAPI
    public static boolean removeStdOutput() {
        return removeOutput(STD_OUTPUT);
    }

    @LauncherAPI
    public static void subDebug(String message) {
        if (isDebugEnabled()) {
            log(Level.DEBUG, message, true);
        }
    }

    @LauncherAPI
    public static void subDebug(String format, Object... args) {
        subDebug(String.format(format, args));
    }

    @LauncherAPI
    public static void subInfo(String message) {
        log(Level.INFO, message, true);
    }

    @LauncherAPI
    public static void subInfo(String format, Object... args) {
        subInfo(String.format(format, args));
    }

    @LauncherAPI
    public static void subWarning(String message) {
        log(Level.WARN, message, true);
    }

    @LauncherAPI
    public static void subWarning(String format, Object... args) {
        subWarning(String.format(format, args));
    }

    @LauncherAPI
    public static String toString(Throwable exc) {
        try (StringWriter sw = new StringWriter()) {
            try (PrintWriter pw = new PrintWriter(sw)) {
                exc.printStackTrace(pw);
            }
            return sw.toString();
        } catch (IOException e) {
            throw new InternalError(e);
        }
    }

    @LauncherAPI
    public static void warning(String message) {
        log(Level.WARN, message, false);
    }

    @LauncherAPI
    public static void warning(String format, Object... args) {
        warning(String.format(format, args));
    }

    private static String ansiFormatVersion(String product) {
        return new Ansi().bold(). // Setup
                fgBright(Color.MAGENTA).a("sashok724's "). // sashok724's
                fgBright(Color.CYAN).a(product). // Product
                fgBright(Color.WHITE).a(" v").fgBright(Color.BLUE).a(Integer.toString(LauncherVersion.BUILD)). // Version
                fgBright(Color.WHITE).a(" (build #").fgBright(Color.RED).a(LauncherVersion.getVersion().getVersionString()).fgBright(Color.WHITE).a(')'). // Build#
                fgBright(Color.WHITE).a(" mod by ").fgBright(Color.RED).a("Gravit").
                reset().toString(); // To string
    }

    private static String formatVersion(String product) {
        return String.format("sashok724's %s v%s (build #%s) mod by Gravit", product, LauncherVersion.getVersion().getVersionString(), Integer.toString(LauncherVersion.BUILD));
    }

    static {
        // Use JAnsi if available
        boolean jansi;
        try {
            if (Boolean.getBoolean(NO_JANSI_PROPERTY)) {
                jansi = false;
            } else {
                Class.forName("org.fusesource.jansi.Ansi");
                AnsiConsole.systemInstall();
                jansi = true;
            }
        } catch (ClassNotFoundException ignored) {
            jansi = false;
        }
        JANSI = jansi;

        // Add std writer
        STD_OUTPUT = System.out::println;
        addOutput(STD_OUTPUT);

        // Add file log writer
        String logFile = System.getProperty("launcher.logFile");
        if (logFile != null) {
            try {
                addOutput(IOHelper.toPath(logFile));
            } catch (IOException e) {
                error(e);
            }
        }
    }

    @LauncherAPI
    @FunctionalInterface
    public interface Output {
        void println(String message);
    }

    private static final class JAnsiOutput extends WriterOutput {
        private JAnsiOutput(OutputStream output) {
            super(IOHelper.newWriter(new AnsiOutputStream(output)));
        }
    }

    private static class WriterOutput implements Output, AutoCloseable {
        private final Writer writer;

        private WriterOutput(Writer writer) {
            this.writer = writer;
        }

        @Override
        public void close() throws IOException {
            writer.close();
        }

        @Override
        public void println(String message) {
            try {
                writer.write(message + System.lineSeparator());
                writer.flush();
            } catch (IOException ignored) {
                // Do nothing?
            }
        }
    }
}
