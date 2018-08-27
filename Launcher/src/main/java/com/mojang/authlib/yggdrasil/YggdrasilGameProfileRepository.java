package com.mojang.authlib.yggdrasil;

import java.util.Arrays;
import java.util.UUID;

import com.mojang.authlib.Agent;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.GameProfileRepository;
import com.mojang.authlib.ProfileLookupCallback;
import launcher.profiles.PlayerProfile;
import launcher.helper.LogHelper;
import launcher.helper.VerifyHelper;
import launcher.request.uuid.BatchProfileByUsernameRequest;
import launcher.serialize.SerializeLimits;

public final class YggdrasilGameProfileRepository implements GameProfileRepository {
    private static final long BUSY_WAIT_MS = VerifyHelper.verifyLong(
            Long.parseLong(System.getProperty("launcher.authlib.busyWait", Long.toString(100L))),
            VerifyHelper.L_NOT_NEGATIVE, "launcher.authlib.busyWait can't be < 0");
    private static final long ERROR_BUSY_WAIT_MS = VerifyHelper.verifyLong(
            Long.parseLong(System.getProperty("launcher.authlib.errorBusyWait", Long.toString(500L))),
            VerifyHelper.L_NOT_NEGATIVE, "launcher.authlib.errorBusyWait can't be < 0");

    public YggdrasilGameProfileRepository() {
        LogHelper.debug("Patched GameProfileRepository created");
    }

    @Override
    public void findProfilesByNames(String[] usernames, Agent agent, ProfileLookupCallback callback) {
        int offset = 0;
        while (offset < usernames.length) {
            String[] sliceUsernames = Arrays.copyOfRange(usernames, offset, Math.min(offset + SerializeLimits.MAX_BATCH_SIZE, usernames.length));
            offset += SerializeLimits.MAX_BATCH_SIZE;

            // Batch Username-To-UUID request
            PlayerProfile[] sliceProfiles;
            try {
                sliceProfiles = new BatchProfileByUsernameRequest(sliceUsernames).request();
            } catch (Exception e) {
                for (String username : sliceUsernames) {
                    LogHelper.debug("Couldn't find profile '%s': %s", username, e);
                    callback.onProfileLookupFailed(new GameProfile((UUID) null, username), e);
                }

                // Busy wait, like in standard authlib
                busyWait(ERROR_BUSY_WAIT_MS);
                continue;
            }

            // Request succeeded!
            for (int i = 0; i < sliceProfiles.length; i++) {
                PlayerProfile pp = sliceProfiles[i];
                if (pp == null) {
                    String username = sliceUsernames[i];
                    LogHelper.debug("Couldn't find profile '%s'", username);
                    callback.onProfileLookupFailed(new GameProfile((UUID) null, username), new ProfileNotFoundException("Server did not find the requested profile"));
                    continue;
                }

                // Report as looked up
                LogHelper.debug("Successfully looked up profile '%s'", pp.username);
                callback.onProfileLookupSucceeded(YggdrasilMinecraftSessionService.toGameProfile(pp));
            }

            // Busy wait, like in standard authlib
            busyWait(BUSY_WAIT_MS);
        }
    }

    private static void busyWait(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            LogHelper.error(e);
        }
    }
}
