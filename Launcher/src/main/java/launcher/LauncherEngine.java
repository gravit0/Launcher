package launcher;

import launcher.client.*;
import launcher.hasher.FileNameMatcher;
import launcher.hasher.HashedDir;
import launcher.hasher.HashedEntry;
import launcher.hasher.HashedFile;
import launcher.helper.*;
import launcher.profiles.ClientProfile;
import launcher.profiles.PlayerProfile;
import launcher.profiles.Texture;
import launcher.request.*;
import launcher.request.auth.AuthRequest;
import launcher.request.auth.CheckServerRequest;
import launcher.request.auth.JoinServerRequest;
import launcher.request.update.LauncherRequest;
import launcher.request.update.UpdateRequest;
import launcher.request.uuid.BatchProfileByUsernameRequest;
import launcher.request.uuid.ProfileByUUIDRequest;
import launcher.request.uuid.ProfileByUsernameRequest;
import launcher.serialize.HInput;
import launcher.serialize.HOutput;
import launcher.serialize.config.ConfigObject;
import launcher.serialize.config.TextConfigReader;
import launcher.serialize.config.TextConfigWriter;
import launcher.serialize.config.entry.*;
import launcher.serialize.signed.SignedBytesHolder;
import launcher.serialize.signed.SignedObjectHolder;
import launcher.serialize.stream.EnumSerializer;
import launcher.serialize.stream.StreamObject;

import javax.script.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

import static launcher.Launcher.INIT_SCRIPT_FILE;

public class LauncherEngine {
    private LauncherEngine() {
        setScriptBindings();
    }

    // Instance
    private final AtomicBoolean started = new AtomicBoolean(false);
    private final ScriptEngine engine = CommonHelper.newScriptEngine();

    private void setScriptBindings() {
        LogHelper.info("Setting up script engine bindings");
        Bindings bindings = engine.getBindings(ScriptContext.ENGINE_SCOPE);
        bindings.put("launcher", this);

        // Add launcher class bindings
        addLauncherClassBindings(bindings);
    }

    @LauncherAPI
    public Object loadScript(URL url) throws IOException, ScriptException {
        LogHelper.debug("Loading script: '%s'", url);
        try (BufferedReader reader = IOHelper.newReader(url)) {
            return engine.eval(reader);
        }
    }

    public static void main(String... args) throws Throwable {
        if(System.getProperty("log4j.configurationFile") == null)
            System.setProperty("log4j.configurationFile","launcher/log4j2.xml");
        LogHelper.logInit(false);
        JVMHelper.verifySystemProperties(Launcher.class, true);
        LogHelper.printVersion("Launcher");
        // Start Launcher
        Instant start = Instant.now();
        try {
            new LauncherEngine().start(args);
        } catch (Exception e) {
            LogHelper.error(e);
            return;
        }
        Instant end = Instant.now();
        LogHelper.debug("Launcher started in %dms", Duration.between(start, end).toMillis());
    }

    @LauncherAPI
    public void start(String... args) throws Throwable {
        Launcher.modulesManager = new ClientModuleManager(this);
        Launcher.modulesManager.preInitModules();
        Objects.requireNonNull(args, "args");
        if (started.getAndSet(true)) {
            throw new IllegalStateException("Launcher has been already started");
        }

        // Load init.js script
        loadScript(Launcher.getResourceURL(INIT_SCRIPT_FILE));
        LogHelper.info("Invoking start() function");
        Invocable invoker = (Invocable) engine;
        if (JVMHelper.OS_TYPE == JVMHelper.OS.MUSTDIE) {
            AvanguardStarter.start((Path) invoker.invokeFunction("getPathDirHelper"));
            AvanguardStarter.loadVared();
            AvanguardStarter.main(false);
        }
        invoker.invokeFunction("start", (Object) args);
    }

    @LauncherAPI
    public static void addLauncherClassBindings(Map<String, Object> bindings) {
        bindings.put("LauncherClass", Launcher.class);
        bindings.put("LauncherConfigClass", LauncherConfig.class);
        bindings.put("HTTPRequestClass", HTTPRequest.class);

        // Set client class bindings
        bindings.put("PlayerProfileClass", PlayerProfile.class);
        bindings.put("PlayerProfileTextureClass", Texture.class);
        bindings.put("ClientProfileClass", ClientProfile.class);
        bindings.put("ClientProfileVersionClass", ClientProfile.Version.class);
        bindings.put("ClientLauncherClass", ClientLauncher.class);
        bindings.put("ClientLauncherParamsClass", ClientLauncher.Params.class);
        bindings.put("ServerPingerClass", ServerPinger.class);

        // Set request class bindings
        bindings.put("RequestClass", Request.class);
        bindings.put("RequestTypeClass", RequestType.class);
        bindings.put("RequestExceptionClass", RequestException.class);
        bindings.put("CustomRequestClass", CustomRequest.class);
        bindings.put("PingRequestClass", PingRequest.class);
        bindings.put("AuthRequestClass", AuthRequest.class);
        bindings.put("JoinServerRequestClass", JoinServerRequest.class);
        bindings.put("CheckServerRequestClass", CheckServerRequest.class);
        bindings.put("UpdateRequestClass", UpdateRequest.class);
        bindings.put("LauncherRequestClass", LauncherRequest.class);
        bindings.put("ProfileByUsernameRequestClass", ProfileByUsernameRequest.class);
        bindings.put("ProfileByUUIDRequestClass", ProfileByUUIDRequest.class);
        bindings.put("BatchProfileByUsernameRequestClass", BatchProfileByUsernameRequest.class);

        // Set hasher class bindings
        bindings.put("FileNameMatcherClass", FileNameMatcher.class);
        bindings.put("HashedDirClass", HashedDir.class);
        bindings.put("HashedFileClass", HashedFile.class);
        bindings.put("HashedEntryTypeClass", HashedEntry.Type.class);

        // Set serialization class bindings
        bindings.put("HInputClass", HInput.class);
        bindings.put("HOutputClass", HOutput.class);
        bindings.put("StreamObjectClass", StreamObject.class);
        bindings.put("StreamObjectAdapterClass", StreamObject.Adapter.class);
        bindings.put("SignedBytesHolderClass", SignedBytesHolder.class);
        bindings.put("SignedObjectHolderClass", SignedObjectHolder.class);
        bindings.put("EnumSerializerClass", EnumSerializer.class);

        // Set config serialization class bindings
        bindings.put("ConfigObjectClass", ConfigObject.class);
        bindings.put("ConfigObjectAdapterClass", ConfigObject.Adapter.class);
        bindings.put("BlockConfigEntryClass", BlockConfigEntry.class);
        bindings.put("BooleanConfigEntryClass", BooleanConfigEntry.class);
        bindings.put("IntegerConfigEntryClass", IntegerConfigEntry.class);
        bindings.put("ListConfigEntryClass", ListConfigEntry.class);
        bindings.put("StringConfigEntryClass", StringConfigEntry.class);
        bindings.put("ConfigEntryTypeClass", ConfigEntry.Type.class);
        bindings.put("TextConfigReaderClass", TextConfigReader.class);
        bindings.put("TextConfigWriterClass", TextConfigWriter.class);

        // Set helper class bindings
        bindings.put("CommonHelperClass", CommonHelper.class);
        bindings.put("IOHelperClass", IOHelper.class);
        bindings.put("EnvHelperClass", EnvHelper.class);
        bindings.put("JVMHelperClass", JVMHelper.class);
        bindings.put("JVMHelperOSClass", JVMHelper.OS.class);
        bindings.put("LogHelperClass", LogHelper.class);
        bindings.put("LogHelperOutputClass", LogHelper.Output.class);
        bindings.put("SecurityHelperClass", SecurityHelper.class);
        bindings.put("DigestAlgorithmClass", SecurityHelper.DigestAlgorithm.class);
        bindings.put("VerifyHelperClass", VerifyHelper.class);

        // Load JS API if available
        try {
            Class.forName("javafx.application.Application");
            bindings.put("JSApplicationClass", JSApplication.class);
        } catch (ClassNotFoundException ignored) {
            LogHelper.warning("JavaFX API isn't available");
        }
    }
}
