package launcher.server;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.nio.file.Paths;

public class ServerWrapper {
    public static ModulesManager modulesManager;
    public static void main(String[] args) throws Throwable {
        modulesManager = new ModulesManager();
        modulesManager.autoload(Paths.get("modules"));
        String classname = args[0];
        Class mainClass = Class.forName(classname);
        MethodHandle mainMethod = MethodHandles.publicLookup().findStatic(mainClass, "main", MethodType.methodType(void.class, String[].class));
        String[] real_args = new String[args.length - 1];
        System.arraycopy(args,1,real_args,0,args.length - 1);
        mainMethod.invoke(real_args);
    }
}
