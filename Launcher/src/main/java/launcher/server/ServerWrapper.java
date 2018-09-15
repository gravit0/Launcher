package launcher.server;


import java.io.InputStream;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.nio.file.Paths;

import launcher.Launcher;
import launcher.LauncherConfig;
import launcher.client.ClientLauncher;
import launcher.helper.IOHelper;
import launcher.helper.LogHelper;
import launcher.profiles.ClientProfile;
import launcher.request.update.ProfilesRequest;
import launcher.serialize.HInput;
import launcher.serialize.signed.SignedObjectHolder;

public class ServerWrapper {
    public static ModulesManager modulesManager;
    public static void main(String[] args) throws Throwable {
        if(System.getProperty("log4j.configurationFile") == null)
			try(InputStream stream = IOHelper.newInput(IOHelper.getResourceURL("log4j2.xml"))) {
                System.setProperty("log4j.configurationFile", "launcher/log4j2.xml,log4j2.xml");
            } catch (Exception e) {
                System.setProperty("log4j.configurationFile", "launcher/log4j2.xml");
            }
        ServerWrapper wrapper = new ServerWrapper();
        modulesManager = new ModulesManager(wrapper);
        modulesManager.autoload(Paths.get("modules"));
        Launcher.modulesManager = modulesManager;
        LauncherConfig cfg = new LauncherConfig(new HInput(IOHelper.newInput(IOHelper.getResourceURL(Launcher.CONFIG_FILE))));
        modulesManager.preInitModules();
        ProfilesRequest.Result result = new ProfilesRequest(cfg).request();
        for(SignedObjectHolder<ClientProfile> p : result.profiles)
        {
            LogHelper.debug("Get profile: %s",p.object.getTitle());
            if(p.object.getTitle().equals(ClientLauncher.title)) {
                wrapper.profile = p.object;
                LogHelper.debug("Found profile: %s",ClientLauncher.title);
                break;
            }
        }
        modulesManager.initModules();
        String classname = args[0];
        Class<?> mainClass = Class.forName(classname);
        MethodHandle mainMethod = MethodHandles.publicLookup().findStatic(mainClass, "main", MethodType.methodType(void.class, String[].class));
        String[] real_args = new String[args.length - 1];
        System.arraycopy(args,1,real_args,0,args.length - 1);
        modulesManager.postInitModules();
        mainMethod.invoke(real_args);
    }
    public ClientProfile profile;
}
