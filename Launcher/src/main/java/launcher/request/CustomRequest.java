package launcher.request;

import launcher.LauncherAPI;
import launcher.LauncherConfig;
import launcher.helper.VerifyHelper;
import launcher.serialize.HInput;
import launcher.serialize.HOutput;

public abstract class CustomRequest<T> extends Request<T> {
    @LauncherAPI
    public CustomRequest() {
        this(null);
    }

    @LauncherAPI
    public CustomRequest(LauncherConfig config) {
        super(config);
    }

    @LauncherAPI
    public abstract String getName();

    @Override
    public final Integer getType() {
        return 255;
    }

    @Override
    protected final T requestDo(HInput input, HOutput output) throws Exception {
        output.writeASCII(VerifyHelper.verifyIDName(getName()), 255);
        output.flush();

        // Custom request redirect
        return requestDoCustom(input, output);
    }

    @LauncherAPI
    protected abstract T requestDoCustom(HInput input, HOutput output);
}
