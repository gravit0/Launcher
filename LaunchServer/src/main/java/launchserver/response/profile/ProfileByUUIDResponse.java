package launchserver.response.profile;

import java.io.IOException;
import java.util.UUID;

import launcher.profiles.PlayerProfile;
import launcher.profiles.Texture;
import launcher.helper.LogHelper;
import launcher.serialize.HInput;
import launcher.serialize.HOutput;
import launcher.serialize.SerializeLimits;
import launchserver.LaunchServer;
import launchserver.response.Response;

public final class ProfileByUUIDResponse extends Response {

    public ProfileByUUIDResponse(LaunchServer server, long session, HInput input, HOutput output, String ip) {
        super(server, session, input, output, ip);
    }

    @Override
    public void reply() throws IOException {
        UUID uuid = input.readUUID();
        debug("UUID: " + uuid);
        String client = input.readString(SerializeLimits.MAX_CLIENT);
        // Verify has such profile
        String username = server.config.authHandler.uuidToUsername(uuid);
        if (username == null) {
            output.writeBoolean(false);
            return;
        }

        // Write profile
        output.writeBoolean(true);
        getProfile(server, uuid, username, client).write(output);
    }

    public static PlayerProfile getProfile(LaunchServer server, UUID uuid, String username, String client) {
        // Get skin texture
        Texture skin;
        try {
            skin = server.config.textureProvider.getSkinTexture(uuid, username, client);
        } catch (IOException e) {
            LogHelper.error(new IOException(String.format("Can't get skin texture: '%s'", username), e));
            skin = null;
        }

        // Get cloak texture
        Texture cloak;
        try {
            cloak = server.config.textureProvider.getCloakTexture(uuid, username, client);
        } catch (IOException e) {
            LogHelper.error(new IOException(String.format("Can't get cloak texture: '%s'", username), e));
            cloak = null;
        }

        // Return combined profile
        return new PlayerProfile(uuid, username, skin, cloak);
    }
}
