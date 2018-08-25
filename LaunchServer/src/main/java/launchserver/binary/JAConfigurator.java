package launchserver.binary;

import javassist.*;

import java.io.IOException;

public class JAConfigurator {
    ClassPool pool = ClassPool.getDefault();
    CtClass ctClass;
    CtConstructor ctConstructor;
    String classname;
    StringBuilder body;
    public JAConfigurator(Class configclass) throws NotFoundException {
        classname = configclass.getName();
        ctClass = pool.get(classname);
        ctConstructor = ctClass.getDeclaredConstructor(null);
        body = new StringBuilder("{");
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
    public String getZipEntryPath()
    {
        return classname.replace('.','/').concat(".class");
    }
    public byte[] getBytecode() throws IOException, CannotCompileException {
        ctConstructor.setBody(body.toString());
        return ctClass.toBytecode();
    }
}
