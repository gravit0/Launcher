package launcher.request.uuid;

import java.io.IOException;
import java.util.Objects;
import java.util.UUID;

import launcher.LauncherConfig;
import launcher.LauncherAPI;
import launcher.client.ClientLauncher;
import launcher.profiles.PlayerProfile;
import launcher.request.Request;
import launcher.serialize.HInput;
import launcher.serialize.HOutput;
import launcher.serialize.SerializeLimits;

public final class ProfileByUUIDRequest extends Request<PlayerProfile> {
    private final UUID uuid;

    @LauncherAPI
    public ProfileByUUIDRequest(LauncherConfig config, UUID uuid) {
        super(config);
        this.uuid = Objects.requireNonNull(uuid, "uuid");
    }

    @LauncherAPI
    public ProfileByUUIDRequest(UUID uuid) {
        this(null, uuid);
    }

    @Override
    public Integer getType() {
        return Type.PROFILE_BY_UUID.getNumber();
    }

    @Override
    protected PlayerProfile requestDo(HInput input, HOutput output) throws IOException {
        output.writeUUID(uuid);
        output.writeString(ClientLauncher.title,SerializeLimits.MAX_CLIENT);
        output.flush();

        // Return profile
        return input.readBoolean() ? new PlayerProfile(input) : null;
    }
}
