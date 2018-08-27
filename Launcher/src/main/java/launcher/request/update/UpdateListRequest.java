package launcher.request.update;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import launcher.LauncherConfig;
import launcher.LauncherAPI;
import launcher.helper.IOHelper;
import launcher.request.Request;
import launcher.request.RequestType;
import launcher.serialize.HInput;
import launcher.serialize.HOutput;

public final class UpdateListRequest extends Request<Set<String>> {
    @LauncherAPI
    public UpdateListRequest(LauncherConfig config) {
        super(config);
    }

    @LauncherAPI
    public UpdateListRequest() {
        this(null);
    }

    @Override
    public Integer getType() {
        return RequestType.UPDATE_LIST.getNumber();
    }

    @Override
    protected Set<String> requestDo(HInput input, HOutput output) throws IOException {
        int count = input.readLength(0);

        // Read all update dirs names
        Set<String> result = new HashSet<>(count);
        for (int i = 0; i < count; i++) {
            result.add(IOHelper.verifyFileName(input.readString(255)));
        }

        // We're done. Make it unmodifiable and return
        return Collections.unmodifiableSet(result);
    }
}
