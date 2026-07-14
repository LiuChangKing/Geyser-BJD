package org.geysermc.geyser.session.auth;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;
import org.junit.jupiter.api.Test;

public class AuthDataTest {
    @Test
    public void validatesNeteaseBedrockUidRange() {
        assertFalse(authData(0x7fffffffL).hasValidNeteaseUid());
        assertTrue(authData(0x80000000L).hasValidNeteaseUid());
        assertTrue(authData(0xffffffffL).hasValidNeteaseUid());
        assertFalse(authData(0x100000000L).hasValidNeteaseUid());
    }

    private static AuthData authData(long uid) {
        return new AuthData("player", UUID.randomUUID(), "xuid", uid, 0L);
    }
}
