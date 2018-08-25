package launchserver.binary;

import javassist.*;

import java.io.IOException;

public class JAConfigurator {
    ClassPool pool = ClassPool.getDefault();
    CtClass ctClass;
    CtConstructor ctConstructor;
    String classname;
    StringBuilder body;
    int autoincrement;
    public JAConfigurator(Class configclass) throws NotFoundException {
        classname = configclass.getName();
        ctClass = pool.get(classname);
        ctConstructor = ctClass.getDeclaredConstructor(null);
        body = new StringBuilder("{");
        autoincrement = 0;
    }
    public void setAddress(String address)
    {
        body.append("this.address = \"");
        body.append(address);
        body.append("\";");
    }
    public void setPort(int port)
    {
        body.append("this.port = ");
        body.append(port);
        body.append(";");
    }
    public void addModuleClass(String fullName)
    {
        body.append("launcher.modules.Module mod");
        body.append(autoincrement);
        body.append(" = new ");
        body.append(fullName);
        body.append("();");
        body.append("launcher.client.ClientModuleManager.registerModule( mod");
        body.append(autoincrement);
        body.append(" );");
        autoincrement++;
    }
    public String getZipEntryPath()
    {
        return classname.replace('.','/').concat(".class");
    }
    public byte[] getBytecode() throws IOException, CannotCompileException {
        body.append("}");
        ctConstructor.setBody(body.toString());
        return ctClass.toBytecode();
    }
}
