package launchserver.texture;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.UUID;

import launcher.Launcher;
import launcher.profiles.Texture;
import launcher.helper.CommonHelper;
import launcher.helper.IOHelper;
import launcher.helper.LogHelper;
import launcher.serialize.config.entry.BlockConfigEntry;
import launcher.serialize.config.entry.StringConfigEntry;

public final class RequestTextureProvider extends TextureProvider {
    private static final UUID ZERO_UUID = new UUID(0, 0);

    // Instance
    private final String skinURL;
    private final String cloakURL;

    public RequestTextureProvider(BlockConfigEntry block) {
        super(block);
        skinURL = block.getEntryValue("skinsURL", StringConfigEntry.class);
        cloakURL = block.getEntryValue("cloaksURL", StringConfigEntry.class);

        // Verify
        IOHelper.verifyURL(getTextureURL(skinURL, ZERO_UUID, "skinUsername", ""));
        IOHelper.verifyURL(getTextureURL(cloakURL, ZERO_UUID, "cloakUsername", ""));
    }

    @Override
    public void close() {
        // Do nothing
    }

    @Override
    public Texture getCloakTexture(UUID uuid, String username, String client) throws IOException {
        return getTexture(getTextureURL(cloakURL, uuid, username, client), true);
    }

    @Override
    public Texture getSkinTexture(UUID uuid, String username, String client) throws IOException {
        return getTexture(getTextureURL(skinURL, uuid, username, client), false);
    }

    private static Texture getTexture(String url, boolean cloak) throws IOException {
        LogHelper.debug("Getting texture: '%s'", url);
        try {
            return new Texture(url, cloak);
        } catch (FileNotFoundException ignored) {
            LogHelper.subDebug("Texture not found :(");
            return null; // Simply not found
        }
    }

    private static String getTextureURL(String url, UUID uuid, String username, String client) {
        return CommonHelper.replace(url, "username", IOHelper.urlEncode(username),
                "uuid", IOHelper.urlEncode(uuid.toString()), "hash", IOHelper.urlEncode(Launcher.toHash(uuid)),
                "client", IOHelper.urlEncode(client));
    }
}
